<template>
  <main class="ai-page">
    <header class="ai-header">
      <div class="brand-lockup" aria-label="AI辅助测试用例生成系统">
        <div class="brand-icon">
          <Sparkles :size="20" />
        </div>
        <strong>测例 AI</strong>
        <span>AI辅助测试用例生成系统</span>
      </div>

      <div class="header-actions">
        <button class="credit-pill" type="button">
          <Sparkle :size="16" />
          <span>100</span>
        </button>
        <button class="ghost-button" type="button" @click="resetSession">
          <RefreshCw :size="16" />
          新会话
        </button>
      </div>
    </header>

    <section v-if="stage === 'home'" class="home-stage">
      <p class="super-label">产品团队 <span>超级智能体</span></p>
      <h1>下午好，我能如何帮助您</h1>

      <section class="hero-composer" aria-label="测试需求输入框">
        <div class="composer-tag">
          测试用例生成
          <button type="button" aria-label="移除标签">
            <X :size="13" />
          </button>
        </div>
        <textarea
          v-model="requirementInput"
          class="hero-textarea"
          rows="5"
          placeholder="请描述功能需求或模块，例如：我想测试登录模块，需要覆盖账号密码、验证码和异常提示..."
          @keydown.enter.exact.prevent="startConversation"
        />
        <div class="composer-toolbar">
          <button class="mode-button" type="button">
            <Zap :size="18" />
            极速
            <ChevronDown :size="15" />
          </button>
          <label class="url-field">
            <LinkIcon :size="17" />
            <input v-model="referenceUrl" type="url" placeholder="可选：粘贴需求页面或案例 URL" />
          </label>
          <button class="send-button" type="button" :disabled="!requirementInput.trim()" @click="startConversation">
            <ArrowUp :size="21" />
          </button>
        </div>
      </section>

      <nav class="category-row" aria-label="AI能力入口">
        <button
          v-for="item in categories"
          :key="item.label"
          class="category-item"
          :class="{ active: selectedCategory === item.label }"
          type="button"
          @click="applyCategory(item)"
        >
          <span :style="{ '--tile-color': item.color }">
            <component :is="item.icon" :size="22" />
          </span>
          {{ item.label }}
        </button>
      </nav>
    </section>

    <section v-else class="chat-stage" :class="{ 'with-result': stage === 'result' }">
      <aside class="conversation-panel">
        <div class="session-title">
          <FileText :size="17" />
          <strong>{{ sessionTitle }}</strong>
          <span>V1·最新</span>
        </div>

        <div class="coverage-card">
          <span>预计生成</span>
          <strong>{{ generatedPlan?.testCases.length || 0 }}</strong>
          <span>条用例</span>
        </div>

        <div class="summary-block">
          <h2>覆盖的核心场景</h2>
          <p v-if="answerSummary">{{ answerSummary }}</p>
          <p v-else>AI 将根据您的补充选择自动归纳测试范围。</p>
        </div>

        <div v-if="generatedPlan" class="mini-actions">
          <button type="button" @click="copyResult">
            <Copy :size="16" />
            复制方案
          </button>
          <button type="button" @click="downloadMarkdown">
            <Download :size="16" />
            导出 Markdown
          </button>
        </div>

        <section class="side-composer" aria-label="继续对话">
          <textarea v-model="followUpInput" rows="4" placeholder="告诉 AI 如何继续..." />
          <div>
            <button class="mode-button compact" type="button">
              <Zap :size="16" />
              极速
            </button>
            <button class="send-button small" type="button" :disabled="!followUpInput.trim()" @click="sendFollowUp">
              <ArrowUp :size="18" />
            </button>
          </div>
        </section>
      </aside>

      <section class="chat-main">
        <div class="message-stream">
          <article v-for="message in messages" :key="message.id" class="message-row" :class="message.sender">
            <div v-if="message.sender === 'assistant'" class="avatar">
              <Sparkles :size="16" />
            </div>
            <div class="message-bubble">{{ message.content }}</div>
            <span v-if="message.sender === 'user'" class="user-badge">我</span>
          </article>
        </div>

        <article v-if="stage === 'questions' && isPreparingQuestions" class="question-card loading-card">
          <div class="question-header">
            <div>
              <span>DeepSeek 正在分析</span>
              <h2>正在根据你的模块生成补充问题...</h2>
            </div>
          </div>
        </article>

        <article v-if="stage === 'questions' && !isPreparingQuestions && currentQuestion" class="question-card">
          <div class="question-header">
            <div>
              <span>补充内容</span>
              <h2>{{ currentQuestion.title }}</h2>
            </div>
            <div class="question-count">{{ currentIndex + 1 }} / {{ totalQuestions }}</div>
          </div>

          <div class="option-list">
            <label v-for="option in currentQuestion.options" :key="option.value" class="option-item">
              <input
                :type="currentQuestion.type === 'multiple' ? 'checkbox' : 'radio'"
                :name="currentQuestion.id"
                :value="option.value"
                :checked="selectedValues.includes(option.value)"
                @change="toggleOption(option.value)"
              />
              <span>
                <strong>{{ option.label }}</strong>
                <small>{{ option.description }}</small>
              </span>
            </label>

            <label v-if="currentQuestion.allowCustom" class="custom-answer">
              <span>其他</span>
              <input v-model="customText" type="text" placeholder="补充其他内容" />
            </label>
          </div>

          <footer class="question-footer">
            <span>{{ currentQuestion.type === 'multiple' ? '请选择多个答案' : '请选择一个答案' }}</span>
            <div>
              <button class="ghost-button" type="button" @click="skipQuestion">全部跳过</button>
              <button class="primary-button" type="button" :disabled="!canProceed" @click="submitAnswer">
                {{ currentQuestion.id === 'confirm' ? '确认' : '下一步' }}
              </button>
            </div>
          </footer>
        </article>

        <article v-if="stage === 'result' && generatedPlan" class="result-document">
          <div class="document-toolbar">
            <div>
              <h2>{{ generatedPlan.title }}</h2>
              <span>{{ usedModel }}</span>
            </div>
            <div>
              <button type="button" @click="copyResult">
                <Copy :size="16" />
              </button>
              <button type="button" @click="downloadMarkdown">
                <Download :size="16" />
              </button>
            </div>
          </div>

          <section class="doc-section">
            <h3>1. 被测对象与范围</h3>
            <table class="scope-table">
              <tbody>
                <tr v-for="item in generatedPlan.scope" :key="item">
                  <th>{{ item.split('：')[0] }}</th>
                  <td>{{ item.includes('：') ? item.split('：').slice(1).join('：') : item }}</td>
                </tr>
              </tbody>
            </table>
          </section>

          <section class="doc-section">
            <h3>2. 测试用例</h3>
            <article v-for="testCase in generatedPlan.testCases" :key="testCase.id" class="testcase-block">
              <div class="case-title">
                <span>{{ testCase.id }}</span>
                <strong>{{ testCase.title }}</strong>
                <em>{{ testCase.priority }}</em>
              </div>
              <p><b>类型：</b>{{ testCase.category }}</p>
              <p><b>前置条件：</b>{{ testCase.precondition }}</p>
              <ol>
                <li v-for="step in testCase.steps" :key="step">{{ step }}</li>
              </ol>
              <p><b>预期结果：</b>{{ testCase.expectedResult }}</p>
            </article>
          </section>

          <section class="doc-section">
            <h3>3. 风险提示</h3>
            <ul class="risk-list">
              <li v-for="risk in generatedPlan.risks" :key="risk">{{ risk }}</li>
            </ul>
          </section>
        </article>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowUp,
  Blocks,
  Bot,
  ChevronDown,
  Copy,
  Download,
  FileText,
  FlaskConical,
  Link as LinkIcon,
  Network,
  RefreshCw,
  ShieldCheck,
  Sparkle,
  Sparkles,
  TestTubeDiagonal,
  X,
  Zap
} from '@lucide/vue'
import { ElMessage } from 'element-plus'
import { generateChatPlan, generateClarifyingQuestions } from '@/api/chat'
import { createConversation, addMessage, getConversation } from '@/api/conversation'
import type { ChatAnswer, ChatMessage, ClarificationQuestion, GeneratedPlan } from '@/types/chat'
import {
  appendConfirmQuestion,
  buildAnswerSummary,
  createMockGeneratedPlan,
  fallbackClarificationQuestions,
  getNextQuestion,
  getOptionLabel
} from '@/utils/chatFlow'

type Stage = 'home' | 'questions' | 'result'

interface CategoryItem {
  label: string
  prompt: string
  color: string
  icon: unknown
}

const categories: CategoryItem[] = [
  { label: '推荐', prompt: '帮我生成一个功能模块的测试用例', color: '#6c63ff', icon: Sparkles },
  { label: '需求分析', prompt: '根据需求描述生成测试点和测试用例', color: '#3aa7ff', icon: Blocks },
  { label: '接口测试', prompt: '根据接口信息生成接口测试用例', color: '#22c997', icon: Network },
  { label: 'Web测试', prompt: '我想测试 Web 登录模块', color: '#ff8b6b', icon: TestTubeDiagonal },
  { label: '安全测试', prompt: '帮我补充安全测试场景和风险点', color: '#22bfce', icon: ShieldCheck },
  { label: '自动化脚本', prompt: '生成可转成自动化脚本的测试步骤', color: '#f25fd0', icon: FlaskConical },
  { label: '研发测试', prompt: '请生成研发自测用例方案', color: '#ffad42', icon: Bot }
]

const route = useRoute()
const router = useRouter()

const stage = ref<Stage>('home')
const selectedCategory = ref('研发测试')
const requirementInput = ref('我想测试登录模块')
const referenceUrl = ref('')
const followUpInput = ref('')
const answers = ref<ChatAnswer[]>([])
const selectedValues = ref<string[]>([])
const customText = ref('')
const messages = ref<ChatMessage[]>([])
const generatedPlan = ref<GeneratedPlan | null>(null)
const usedModel = ref('AI 生成')
const questionModel = ref('DeepSeek')
const questions = ref<ClarificationQuestion[]>([])
const isPreparingQuestions = ref(false)
const conversationId = ref<number | null>(null)
const isLoadingConversation = ref(false)

const totalQuestions = computed(() => questions.value.length)
const currentQuestion = computed<ClarificationQuestion | null>(() => getNextQuestion(questions.value, answers.value))
const currentIndex = computed(() => currentQuestion.value ? questions.value.findIndex((item) => item.id === currentQuestion.value?.id) : totalQuestions.value - 1)
const canProceed = computed(() => currentQuestion.value?.id === 'confirm' || selectedValues.value.length > 0 || Boolean(customText.value.trim()))
const answerSummary = computed(() => buildAnswerSummary(questions.value, answers.value))
const sessionTitle = computed(() => `${extractTitle(requirementInput.value)}-test-cases`)

onMounted(async () => {
  const id = route.query.conversationId
  if (id && !isNaN(Number(id))) {
    await loadConversation(Number(id))
  }
})

async function loadConversation(id: number) {
  isLoadingConversation.value = true
  try {
    const detail = await getConversation(id)
    conversationId.value = id
    requirementInput.value = detail.requirement || ''
    messages.value = (detail.messages || []).map((m: any) => ({
      id: m.id.toString(),
      sender: m.sender as 'user' | 'assistant',
      content: m.content,
      createdAt: m.createdAt
    }))

    if (detail.answers) {
      answers.value = Array.isArray(detail.answers) ? detail.answers : []
    }
    if (detail.generatedPlan) {
      generatedPlan.value = detail.generatedPlan as GeneratedPlan
      stage.value = 'result'
    } else if (detail.answers && detail.answers.length > 0) {
      stage.value = 'questions'
    } else {
      stage.value = 'home'
    }
    ElMessage.success('已加载对话')
  } catch {
    ElMessage.error('加载对话失败')
  } finally {
    isLoadingConversation.value = false
  }
}

function applyCategory(item: CategoryItem) {
  selectedCategory.value = item.label
  requirementInput.value = item.prompt
}

async function startConversation() {
  const requirement = requirementInput.value.trim()
  if (!requirement) {
    ElMessage.warning('请先描述要测试的模块或功能')
    return
  }
  conversationId.value = null
  stage.value = 'questions'
  generatedPlan.value = null
  usedModel.value = 'AI 生成'
  messages.value = [
    createMessage('user', requirement),
    createMessage('assistant', '我先根据你的模块生成几个补充问题，然后再按你的选择生成测试用例。')
  ]
  answers.value = []
  questions.value = []
  isPreparingQuestions.value = true

  try {
    const result = await generateClarifyingQuestions({
      requirement,
      referenceUrl: referenceUrl.value.trim() || undefined,
      conversationId: conversationId.value || undefined
    })
    questions.value = appendConfirmQuestion(result.questions || [])
    questionModel.value = result.usedModel || 'DeepSeek'
  } catch {
    questions.value = fallbackClarificationQuestions
    questionModel.value = '本地预览'
    messages.value.push(createMessage('assistant', 'DeepSeek 反问接口暂时不可用，我先用本地预览问题继续。'))
  } finally {
    isPreparingQuestions.value = false
  }

  const firstQuestion = currentQuestion.value
  if (firstQuestion) {
    messages.value.push(createMessage('assistant', `${questionModel.value}：${firstQuestion.title}`))
  }
  resetQuestionState()
}

function toggleOption(value: string) {
  const question = currentQuestion.value
  if (!question) {
    return
  }
  if (question.type === 'multiple') {
    selectedValues.value = selectedValues.value.includes(value)
      ? selectedValues.value.filter((item) => item !== value)
      : [...selectedValues.value, value]
    return
  }
  selectedValues.value = [value]
}

async function submitAnswer() {
  const question = currentQuestion.value
  if (!question) {
    return
  }
  const values = question.id === 'confirm' ? ['confirmed'] : selectedValues.value
  answers.value.push({
    questionId: question.id,
    values,
    customText: customText.value.trim() || undefined
  })

  const label = values.map((value) => getOptionLabel(questions.value, value)).join('、') || customText.value.trim() || '已确认'
  messages.value.push(createMessage('user', label))

  const next = getNextQuestion(questions.value, answers.value)
  if (!next) {
    await generatePlan()
    return
  }
  messages.value.push(createMessage('assistant', next.title))
  resetQuestionState()
}

function skipQuestion() {
  const question = currentQuestion.value
  if (!question) {
    return
  }
  answers.value.push({
    questionId: question.id,
    values: question.id === 'confirm' ? ['confirmed'] : [],
    customText: customText.value.trim() || undefined
  })
  const next = getNextQuestion(questions.value, answers.value)
  if (!next) {
    void generatePlan()
    return
  }
  messages.value.push(createMessage('assistant', next.title))
  resetQuestionState()
}

async function generatePlan() {
  stage.value = 'result'
  messages.value.push(createMessage('assistant', '我已开始整理测试范围、风险点和结构化测试用例。'))
  try {
    const result = await generateChatPlan({
      requirement: requirementInput.value,
      answers: answers.value,
      referenceUrl: referenceUrl.value.trim() || undefined,
      conversationId: conversationId.value || undefined
    })
    generatedPlan.value = result
    usedModel.value = result.usedModel || 'DeepSeek'
  } catch {
    generatedPlan.value = createMockGeneratedPlan(requirementInput.value, questions.value, answers.value)
    usedModel.value = '本地预览'
  }
}

async function sendFollowUp() {
  if (!followUpInput.value.trim()) {
    return
  }
  const content = followUpInput.value.trim()
  messages.value.push(createMessage('user', content))
  messages.value.push(createMessage('assistant', '已收到补充说明，您可以重新生成或手动复制当前方案继续完善。'))
  followUpInput.value = ''
}

async function copyResult() {
  if (!generatedPlan.value) {
    return
  }
  await navigator.clipboard.writeText(toMarkdown(generatedPlan.value))
  ElMessage.success('方案已复制')
}

function downloadMarkdown() {
  if (!generatedPlan.value) {
    return
  }
  const blob = new Blob([toMarkdown(generatedPlan.value)], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${sessionTitle.value}.md`
  link.click()
  URL.revokeObjectURL(url)
}

function resetSession() {
  stage.value = 'home'
  answers.value = []
  messages.value = []
  generatedPlan.value = null
  questions.value = []
  questionModel.value = 'DeepSeek'
  isPreparingQuestions.value = false
  usedModel.value = 'AI 生成'
  resetQuestionState()
}

function resetQuestionState() {
  selectedValues.value = []
  customText.value = ''
  const question = currentQuestion.value
  if (question?.type === 'confirm') {
    selectedValues.value = ['confirmed']
  }
}

function createMessage(sender: ChatMessage['sender'], content: string): ChatMessage {
  return {
    id: `${sender}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    sender,
    content,
    createdAt: new Date().toISOString()
  }
}

function toMarkdown(plan: GeneratedPlan): string {
  const cases = plan.testCases
    .map((item) => [
      `### ${item.id} ${item.title}`,
      `- 优先级：${item.priority}`,
      `- 类型：${item.category}`,
      `- 前置条件：${item.precondition}`,
      `- 测试步骤：${item.steps.join('；')}`,
      `- 预期结果：${item.expectedResult}`
    ].join('\n'))
    .join('\n\n')

  return [
    `# ${plan.title}`,
    '',
    plan.summary,
    '',
    '## 被测对象与范围',
    ...plan.scope.map((item) => `- ${item}`),
    '',
    '## 测试用例',
    cases,
    '',
    '## 风险提示',
    ...plan.risks.map((item) => `- ${item}`)
  ].join('\n')
}

function extractTitle(input: string): string {
  if (input.includes('登录')) {
    return 'login'
  }
  if (input.includes('接口')) {
    return 'api'
  }
  return 'test'
}
</script>
