import request from '../../utils/request'
import { authStore } from '../../utils/auth'

Page({
  data: {
    user: null,
    userAvatar: '',
    isAdmin: false,
    userList: [],
    roleOptions: ['ADMIN', 'MEMBER'],
    apiKey: '',
    maskedApiKey: '',
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
    const user = authStore.getUser()
    const displayName = user?.realName || user?.username || ''
    this.setData({
      user,
      displayName,
      displayUsername: user?.username || '',
      displayRoleTag: user?.role === 'ADMIN' ? 'tag-admin' : 'tag-member',
      displayRoleText: user?.role === 'ADMIN' ? '管理员' : '成员',
      hasEmail: !!user?.email,
      displayEmail: user?.email || '未设置',
      userAvatar: displayName?.[0] || '?',
      isAdmin: authStore.isAdmin()
    })
    this.loadProfile()
    if (authStore.isAdmin()) this.loadUsers()
    this.loadApiKey()
  },

  async loadApiKey() {
    try {
      const res = await request.get('/user/api-key')
      const key = res.apiKey || ''
      this.setData({
        apiKey: key,
        maskedApiKey: key ? key.substring(0, 8) + '••••' : ''
      })
    } catch { /* ignore */ }
  },

  editApiKey() {
    wx.showModal({
      title: '设置 API Key',
      content: ' ',
      editable: true,
      placeholderText: '输入 DeepSeek API Key (sk-...)',
      success: async (res) => {
        if (res.confirm) {
          const key = (res.content || '').trim()
          try {
            await request.put('/user/api-key', { apiKey: key })
            this.setData({
              apiKey: key,
              maskedApiKey: key ? key.substring(0, 8) + '••••' : ''
            })
            wx.showToast({ title: 'API Key 已保存', icon: 'success' })
          } catch (e) {
            wx.showToast({ title: e.message || '保存失败', icon: 'none' })
          }
        }
      }
    })
  },

  async loadProfile() {
    try {
      const profile = await request.get('/user/profile')
      authStore.setUser(profile)
      this.setData({
        user: profile,
        displayName: (profile.realName || profile.username || ''),
        displayEmail: profile.email || '未设置',
        hasEmail: !!profile.email
      })
    } catch { /* ignore */ }
  },

  async loadUsers() {
    try {
      const list = await request.get('/admin/users')
      this.setData({ userList: list })
    } catch { /* ignore */ }
  },

  editEmail() {
    wx.showModal({
      title: '修改邮箱',
      editable: true,
      placeholderText: '输入新邮箱',
      content: this.data.user?.email || '',
      success: async (res) => {
        if (res.confirm && res.content) {
          try {
            await request.put('/user/email', { email: res.content.trim() })
            wx.showToast({ title: '邮箱已更新', icon: 'success' })
            this.loadProfile()
          } catch (e) {
            wx.showToast({ title: e.message || '更新失败', icon: 'none' })
          }
        }
      }
    })
  },

  editPassword() {
    let currentPwd = '', newPwd = ''
    wx.showModal({
      title: '修改密码',
      content: ' ',
      editable: false,
      success: () => {
        // Use prompt-style UI
        wx.showModal({
          title: '当前密码',
          content: ' ',
          editable: true,
          placeholderText: '输入当前密码',
          success: (r1) => {
            if (!r1.confirm) return
            currentPwd = r1.content
            wx.showModal({
              title: '新密码',
              content: ' ',
              editable: true,
              placeholderText: '至少6位',
              success: async (r2) => {
                if (!r2.confirm) return
                newPwd = r2.content
                if (!newPwd || newPwd.length < 6) {
                  return wx.showToast({ title: '密码至少6位', icon: 'none' })
                }
                try {
                  await request.put('/user/password', { currentPassword: currentPwd, newPassword: newPwd })
                  wx.showToast({ title: '密码已更新', icon: 'success' })
                } catch (e) {
                  wx.showToast({ title: e.message || '更新失败', icon: 'none' })
                }
              }
            })
          }
        })
      }
    })
  },

  onRoleChange(e) {
    const id = e.currentTarget.dataset.id
    const role = this.data.roleOptions[e.detail.value]
    wx.showLoading({ title: '更新中...' })
    request.put(`/admin/users/${id}/role`, { role }).then(() => {
      wx.hideLoading()
      wx.showToast({ title: '角色已更新', icon: 'success' })
      this.loadUsers()
    }).catch((e) => {
      wx.hideLoading()
      wx.showToast({ title: e.message || '更新失败', icon: 'none' })
    })
  },

  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          authStore.clear()
          wx.redirectTo({ url: '/pages/login/login' })
        }
      }
    })
  }
})
