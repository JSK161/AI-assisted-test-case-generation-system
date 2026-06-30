import { authStore } from './utils/auth'

App({
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    statusBarHeight: 0,
    safeAreaBottom: 0
  },
  onLaunch() {
    try {
      const sys = wx.getSystemInfoSync()
      this.globalData.statusBarHeight = sys.statusBarHeight || 44
      this.globalData.safeAreaBottom = sys.screenHeight - (sys.safeArea?.bottom || sys.screenHeight) + 10
    } catch {}
    wx.cloud?.init({ env: 'prod' })
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
