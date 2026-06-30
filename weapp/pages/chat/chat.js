import request from '../../utils/request'
import { authStore } from '../../utils/auth'
import { formatTime } from '../../utils/util'

const categories = [
  { label: '推荐', prompt: '帮我生成一个功能模块的测试用例', color: '#6c63ff', icon: '★' },
  { label: '需求分析', prompt: '根据需求描述生成测试点和测试用例', color: '#3aa7ff', icon: '◆' },
  { label: '接口测试', prompt: '根据接口信息生成接口测试用例', color: '#22c997', icon: '●' },
  { label: 'Web测试', prompt: '我想测试 Web 登录模块', color: '#ff8b6b', icon: '■' },
  { label: '安全测试', prompt: '帮我补充安全测试场景和风险点', color: '#22bfce', icon: '▲' },
  { label: '自动化脚本', prompt: '生成可转成自动化脚本的测试步骤', color: '#f25fd0', icon: '♥' },
  { label: '研发测试', prompt: '请生成研发自测用例方案', color: '#ffad42', icon: '♦' }
]

Page({
  data: {
    stage: 'home',
    categories,
    requirement: '',
    referenceUrl: '',
    userName: authStore.getUser()?.realName || authStore.getUser()?.username || '用户',
    // Questions stage
    messages: [],
    questions: [],
    answers: [],
    currentQuestion: null,
    currentIndex: 0,
    totalQuestions: 0,
    selectedValues: [],
    customText: '',
    canProceed: false,
    questionModel: '',
    isPreparing: false,
    conversationId: null,
    // File
    fileName: '',
    fileContent: '',
    // Result
    planData: null,
    usedModel: ''
  },

  onLoad(options) {
    if (options.conversationId) {
      this.loadConversation(Number(options.conversationId))
    }
  },

  onShow() {
    const user = authStore.getUser()
    if (!authStore.isLoggedIn()) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    this.setData({
      userName: user?.realName || user?.username || '用户'
    })
  },

  // ===== Input Handlers =====
  onRequirementInput(e) { this.setData({ requirement: e.detail.value }) },
  onUrlInput(e) { this.setData({ referenceUrl: e.detail.value }) },
  onCustomText(e) { this.setData({ customText: e.detail.value }) },

  // ===== Navigation =====
  goHome() {
    this.setData({ stage: 'home', requirement: '', messages: [], questions: [], planData: null, conversationId: null })
  },
  goProfile() {
    wx.navigateTo({ url: '/pages/profile/profile' })
  },

  // ===== Category =====
  pickCategory(e) {
    const idx = e.currentTarget.dataset.index
    this.setData({ requirement: categories[idx].prompt })
  },

  // ===== File =====
  chooseFile() {
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      success: (res) => {
        const file = res.tempFiles[0]
        const ext = file.name.split('.').pop().toLowerCase()
        const textExts = ['txt','md','json','xml','yaml','yml','csv','html','js','ts','vue','java','py','sql']
        if (!textExts.includes(ext)) {
          return wx.showToast({ title: '仅支持文本文件', icon: 'none' })
        }
        wx.getFileSystemManager().readFile({
          filePath: file.path,
          encoding: 'utf-8',
          success: (r) => {
            this.setData({ fileName: file.name, fileContent: r.data })
            wx.showToast({ title: `已附加 ${file.name}`, icon: 'success' })
          },
          fail: () => wx.showToast({ title: '读取文件失败', icon: 'none' })
        })
      }
    })
  },
  clearFile() {
    this.setData({ fileName: '', fileContent: '' })
  },

  // ===== Start Conversation =====
  async startConversation() {
    const requirement = this.data.requirement.trim()
    const fileContent = this.data.fileContent
    if (!requirement && !fileContent) {
      return wx.showToast({ title: '请输入需求或上传文件', icon: 'none' })
    }

    let fullReq = requirement
    if (fileContent) {
      fullReq += '\n\n【附加文件 ' + this.data.fileName + ' 的内容】\n' + fileContent
    }

    this.setData({
      stage: 'questions',
      messages: [{
        id: Date.now(),
        sender: 'user',
        content: requirement || `（已附加文件：${this.data.fileName}）`
      }, {
        id: Date.now() + 1,
        sender: 'assistant',
        content: '我先根据你的模块生成几个补充问题，然后再按你的选择生成测试用例。'
      }],
      answers: [],
      questions: [],
      currentQuestion: null,
      isPreparing: true,
      conversationId: null
    })

    try {
      const conv = await request.post('/conversations', {
        title: (requirement || this.data.fileName).substring(0, 50),
        requirement: fullReq
      })
      this.setData({ conversationId: conv.id })
      await request.post(`/conversations/${conv.id}/messages`, {
        sender: 'user',
        content: requirement || `（已附加文件：${this.data.fileName}）`
      })
    } catch (e) { /* ignore */ }

    try {
      const result = await request.post('/chat/questions', {
        requirement: fullReq,
        referenceUrl: this.data.referenceUrl.trim() || undefined,
        conversationId: this.data.conversationId || undefined
      })
      const questions = (result.questions || []).concat([{
        id: 'confirm',
        title: '以上问题是否覆盖了您的需求？如果没有，请补充',
        type: 'choice',
        options: [{ value: 'yes', label: '是的，开始生成方案', description: '直接生成测试方案' }]
      }])
      this.setData({
        questions,
        totalQuestions: questions.length,
        questionModel: result.usedModel || 'DeepSeek',
        isPreparing: false
      })
      this.showNextQuestion()
    } catch {
      const fallback = [
        { id: 'scope', title: '被测模块的范围是什么？', type: 'multiple', options: [
          { value: 'function', label: '功能测试', description: '核心业务流程' },
          { value: 'login', label: '登录认证', description: '账号密码/验证码' }
        ]},
        { id: 'confirm', title: '以上问题是否覆盖了您的需求？', type: 'choice', options: [
          { value: 'yes', label: '是的，开始生成方案', description: '直接生成测试方案' }
        ]}
      ]
      this.setData({ questions: fallback, totalQuestions: fallback.length, isPreparing: false, questionModel: '本地预览' })
      this.showNextQuestion()
    }
  },

  showNextQuestion() {
    const { questions, answers } = this.data
    const answered = answers.map(a => a.questionId)
    const next = questions.find(q => !answered.includes(q.id))
    if (next) {
      this.setData({
        currentQuestion: next,
        currentIndex: questions.indexOf(next),
        selectedValues: [],
        customText: '',
        canProceed: next.id === 'confirm'
      })
    }
  },

  toggleOption(e) {
    const val = e.detail.value || []
    this.setData({ selectedValues: val, canProceed: val.length > 0 })
  },

  skipQuestion() {
    const { currentQuestion, questions, answers } = this.data
    this.setData({
      answers: [...answers, { questionId: currentQuestion.id, values: [], customText: '' }]
    })
    this.showNextQuestion()
  },

  async submitAnswer() {
    const { currentQuestion, selectedValues, customText, answers, conversationId } = this.data
    const answer = { questionId: currentQuestion.id, values: selectedValues, customText: customText.trim() }

    this.setData({ answers: [...answers, answer] })

    if (currentQuestion.id === 'confirm') {
      await this.generatePlan()
    } else {
      this.showNextQuestion()
    }
  },

  async generatePlan() {
    this.setData({ stage: 'result' })

    const fullReq = this.data.requirement + (this.data.fileContent ? '\n\n【附加文件内容】\n' + this.data.fileContent : '')

    try {
      const result = await request.post('/chat/generate', {
        requirement: fullReq,
        answers: this.data.answers,
        referenceUrl: this.data.referenceUrl.trim() || undefined,
        conversationId: this.data.conversationId || undefined
      })
      this.setData({ planData: result, usedModel: result.usedModel || 'DeepSeek' })
    } catch {
      // Mock fallback
      this.setData({
        planData: {
          title: fullReq.substring(0, 30) + '-测试方案',
          summary: '基于功能描述生成的系统测试方案',
          scope: ['功能模块：' + fullReq.substring(0, 50)],
          risks: ['暂无明确风险点'],
          testCases: [
            { id: 'TC-001', title: '基本功能验证', priority: 'P0', category: '功能测试', precondition: '系统正常运行', steps: ['步骤1', '步骤2', '验证结果'], expectedResult: '功能正确' }
          ]
        },
        usedModel: '本地预览'
      })
    }
  },

  async loadConversation(id) {
    wx.showLoading({ title: '加载中...' })
    try {
      const detail = await request.get(`/conversations/${id}`)
      const msgs = (detail.messages || []).map(m => ({ id: m.id, sender: m.sender, content: m.content }))
      this.setData({
        conversationId: id,
        requirement: detail.requirement || '',
        messages: msgs,
        stage: detail.generatedPlan ? 'result' : 'home',
        planData: detail.generatedPlan || null
      })
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  copyPlan() {
    const text = JSON.stringify(this.data.planData, null, 2)
    wx.setClipboardData({ data: text })
    wx.showToast({ title: '已复制', icon: 'success' })
  },

  downloadPlan() {
    const text = JSON.stringify(this.data.planData, null, 2)
    const fs = wx.getFileSystemManager()
    const path = wx.env.USER_DATA_PATH + '/test-plan.json'
    fs.writeFileSync(path, text, 'utf-8')
    wx.openDocument({ filePath: path, fileType: 'json' })
  }
})
