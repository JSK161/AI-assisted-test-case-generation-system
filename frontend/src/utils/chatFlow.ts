import type { ChatAnswer, ClarificationQuestion, GeneratedPlan, OptionItem } from '@/types/chat'

export const fallbackClarificationQuestions: ClarificationQuestion[] = appendConfirmQuestion([
  {
    id: 'business_flow',
    title: '这个模块最核心的业务流程是什么？',
    type: 'multiple',
    options: [
      { label: '正常提交成功', value: '正常提交成功', description: '覆盖主链路' },
      { label: '数据校验失败', value: '数据校验失败', description: '覆盖异常输入' },
      { label: '状态回滚或重试', value: '状态回滚或重试', description: '覆盖失败恢复' }
    ],
    allowCustom: true
  },
  {
    id: 'risk_focus',
    title: '你更关注哪些测试风险？',
    type: 'multiple',
    options: [
      { label: '权限与越权', value: '权限与越权', description: '验证不同角色边界' },
      { label: '并发与重复提交', value: '并发与重复提交', description: '验证重复请求处理' },
      { label: '弱网与超时', value: '弱网与超时', description: '验证异常环境' }
    ],
    allowCustom: true
  }
])

export const clarificationQuestions = fallbackClarificationQuestions

export function appendConfirmQuestion(questions: ClarificationQuestion[]): ClarificationQuestion[] {
  if (questions.some((question) => question.id === 'confirm')) {
    return questions
  }
  return [
    ...questions,
    {
      id: 'confirm',
      title: '总览并确认',
      type: 'confirm',
      options: [{ label: '确认生成', value: 'confirmed', description: '使用以上信息生成测试用例方案' }]
    }
  ]
}

export function getNextQuestion(questions: ClarificationQuestion[], answers: ChatAnswer[]): ClarificationQuestion | null {
  const answeredIds = new Set(answers.map((answer) => answer.questionId))
  return questions.find((question) => !answeredIds.has(question.id)) ?? null
}

export function getOptionLabel(questions: ClarificationQuestion[], value: string): string {
  return createOptionLookup(questions).get(value)?.label ?? value
}

export function buildAnswerSummary(questions: ClarificationQuestion[], answers: ChatAnswer[]): string {
  return answers
    .filter((answer) => answer.questionId !== 'confirm')
    .map((answer) => {
      const question = questions.find((item) => item.id === answer.questionId)
      const labels = answer.values.map((value) => getOptionLabel(questions, value))
      if (answer.customText?.trim()) {
        labels.push(answer.customText.trim())
      }
      return `${question?.title ?? answer.questionId}：${labels.join('、') || '未选择'}`
    })
    .join('\n')
}

export function createMockGeneratedPlan(
  requirement: string,
  questions: ClarificationQuestion[],
  answers: ChatAnswer[]
): GeneratedPlan {
  const summary = buildAnswerSummary(questions, answers)
  const moduleName = extractModuleName(requirement)
  const platform = inferPlatform(answers)

  return {
    title: `${moduleName}测试用例方案`,
    summary: `${requirement}。已结合补充信息生成结构化测试方案，覆盖正常流程、异常输入、边界值、安全与体验类场景。`,
    scope: [
      `被测模块：${moduleName}`,
      `目标平台：${platform}`,
      '测试类型：功能测试、安全测试、兼容性测试、边界值测试、异常流程测试',
      summary || '用户未补充更多信息，系统按通用测试模型生成'
    ],
    risks: [
      '账号、验证码、Session、Cookie 等状态类逻辑需要重点验证。',
      '错误提示需要清晰且不能暴露敏感信息。',
      '多端登录、弱网、刷新和返回等操作容易出现状态不一致问题。'
    ],
    testCases: [
      {
        id: 'TC-001',
        title: `${moduleName}正常流程验证`,
        priority: 'P0',
        category: '正常场景',
        precondition: '测试环境可访问，测试账号或测试数据已准备完成。',
        steps: ['打开被测页面或入口', '输入合法数据', '提交操作', '观察页面跳转、状态变化和接口返回'],
        expectedResult: '系统处理成功，页面、接口返回和业务状态均符合需求预期。'
      },
      {
        id: 'TC-002',
        title: `${moduleName}必填项为空校验`,
        priority: 'P0',
        category: '异常场景',
        precondition: '用户进入被测功能页面。',
        steps: ['清空一个或多个必填项', '点击提交按钮', '观察表单校验提示'],
        expectedResult: '系统阻止提交，并在对应字段附近提示必填或格式错误。'
      },
      {
        id: 'TC-003',
        title: `${moduleName}边界值与特殊字符校验`,
        priority: 'P1',
        category: '边界场景',
        precondition: '已明确字段长度、格式和允许字符范围。',
        steps: ['输入最小长度、最大长度和超长数据', '输入空格、中文、Unicode、特殊符号', '分别提交并观察结果'],
        expectedResult: '系统按照字段规则处理，非法数据被拦截，合法边界值可以正常通过。'
      },
      {
        id: 'TC-004',
        title: `${moduleName}安全与会话状态验证`,
        priority: 'P1',
        category: '安全场景',
        precondition: '准备正常账号、异常账号及多个浏览器标签页。',
        steps: ['尝试重复提交、刷新页面、回退浏览器', '模拟 Session 过期或 Cookie 失效', '观察权限与状态是否正确'],
        expectedResult: '系统不会出现越权、重复提交、状态错乱或敏感信息泄露。'
      }
    ]
  }
}

function createOptionLookup(questions: ClarificationQuestion[]): Map<string, OptionItem> {
  const lookup = new Map<string, OptionItem>()
  for (const question of questions) {
    for (const option of question.options) {
      lookup.set(option.value, option)
    }
  }
  return lookup
}

function inferPlatform(answers: ChatAnswer[]): string {
  const joined = answers.flatMap((answer) => answer.values).join('、')
  if (joined.includes('App') || joined.includes('移动')) {
    return '移动端 App'
  }
  if (joined.includes('接口')) {
    return '接口服务'
  }
  if (joined.includes('后台')) {
    return '后台管理系统'
  }
  return joined.includes('Web') || joined.includes('浏览器') ? 'Web 浏览器' : '待确认'
}

function extractModuleName(requirement: string): string {
  const normalized = requirement.replace(/[，。,.]/g, ' ').trim()
  const match = normalized.match(/(?:测试|验证|生成)([^\s的]+(?:模块|功能|接口|页面)?)/)
  if (match?.[1]) {
    return match[1].replace(/^的/, '')
  }
  if (normalized.includes('登录')) {
    return '登录模块'
  }
  if (normalized.includes('支付')) {
    return '支付模块'
  }
  if (normalized.includes('退款')) {
    return '退款模块'
  }
  return normalized.slice(0, 16) || '目标模块'
}
