import request from '../../utils/request'
import { authStore } from '../../utils/auth'

Page({
  data: {
    username: '',
    password: '',
    loading: false
  },

  onUsernameInput(e) { this.setData({ username: e.detail.value }) },
  onPasswordInput(e) { this.setData({ password: e.detail.value }) },

  async handleLogin() {
    const { username, password } = this.data
    if (!username.trim() || !password) return

    this.setData({ loading: true })
    try {
      const res = await request.post('/auth/login', {
        username: username.trim(),
        password
      })
      authStore.setLogin(res.token, res.user)
      wx.showToast({ title: '登录成功', icon: 'success' })
      wx.switchTab({ url: '/pages/chat/chat' })
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
