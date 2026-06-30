const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

export const authStore = {
  getToken() {
    return wx.getStorageSync(TOKEN_KEY) || null
  },
  getUser() {
    try {
      return JSON.parse(wx.getStorageSync(USER_KEY) || 'null')
    } catch { return null }
  },
  setLogin(token, user) {
    wx.setStorageSync(TOKEN_KEY, token)
    wx.setStorageSync(USER_KEY, JSON.stringify(user))
  },
  setUser(user) {
    wx.setStorageSync(USER_KEY, JSON.stringify(user))
  },
  clear() {
    wx.removeStorageSync(TOKEN_KEY)
    wx.removeStorageSync(USER_KEY)
  },
  isLoggedIn() {
    return !!this.getToken()
  },
  isAdmin() {
    const user = this.getUser()
    return user && user.role === 'ADMIN'
  }
}
