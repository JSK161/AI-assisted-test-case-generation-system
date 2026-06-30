import { authStore } from './utils/auth'

App({
  globalData: {
    baseUrl: 'http://localhost:8080/api'
  },
  onLaunch() {
    wx.cloud?.init({ env: 'prod' })
    // Auto login if token exists
    const token = authStore.getToken()
    if (token) {
      this.checkLogin(token)
    }
  },
  checkLogin(token) {
    wx.request({
      url: this.globalData.baseUrl + '/user/profile',
      header: { Authorization: 'Bearer ' + token },
      success: (res) => {
        if (res.data.code === 200) {
          authStore.setUser(res.data.data)
        } else {
          authStore.clear()
        }
      },
      fail: () => authStore.clear()
    })
  }
})
