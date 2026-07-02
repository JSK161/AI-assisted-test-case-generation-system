import request from '../../utils/request'
import { authStore } from '../../utils/auth'
import { formatTime } from '../../utils/util'

Page({
  data: {
    list: [],
    loading: true,
    statusBarHeight: 44,
    safeAreaBottom: 0
  },

  onLoad() {
    const app = getApp()
    this.setData({ statusBarHeight: app.globalData.statusBarHeight || 44, safeAreaBottom: app.globalData.safeAreaBottom || 0 })
  },

  onShow() {
    if (!authStore.isLoggedIn()) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    this.loadList()
  },

  async loadList() {
    this.setData({ loading: true })
    try {
      const list = await request.get('/conversations')
      this.setData({ list: list.map(c => ({ ...c, updatedAt: formatTime(c.updatedAt) })) })
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  openConv(e) {
    const id = e.currentTarget.dataset.id
    wx.reLaunch({ url: `/pages/chat/chat?conversationId=${id}` })
  },

  deleteConv(e) {
    const { id, title } = e.currentTarget.dataset
    wx.showModal({
      title: '提示',
      content: `确定删除对话"${title}"吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            await request.del(`/conversations/${id}`)
            this.setData({ list: this.data.list.filter(c => c.id !== id) })
            wx.showToast({ title: '已删除', icon: 'success' })
          } catch {
            wx.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      }
    })
  },

  goBack() { wx.navigateBack() },
  goChat() { wx.switchTab({ url: '/pages/chat/chat' }) }
})
