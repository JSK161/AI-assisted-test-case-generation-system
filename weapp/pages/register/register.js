import request from '../../utils/request'
import { authStore } from '../../utils/auth'

Page({
  data: {
    username: '',
    password: '',
    email: '',
    loading: false,
    statusBarHeight: 44
  },

  onLoad() {
    const app = getApp()
    this.setData({ statusBarHeight: app.globalData.statusBarHeight || 44 })
  },

  onInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({ [field]: e.detail.value })
  },

  async handleRegister() {
    const { username, password, email } = this.data
    if (!username.trim() || username.trim().length < 3) {
      return wx.showToast({ title: '用户名至少3个字符', icon: 'none' })
    }
    if (!password || password.length < 6) {
      return wx.showToast({ title: '密码至少6位', icon: 'none' })
    }

    this.setData({ loading: true })
    try {
      const res = await request.post('/auth/register', {
        username: username.trim(),
        password,
        email: email.trim() || undefined
      })
      authStore.setLogin(res.token, res.user)
      wx.showToast({ title: '注册成功', icon: 'success' })
      wx.switchTab({ url: '/pages/chat/chat' })
    } catch (e) {
      wx.showToast({ title: e.message || '注册失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
