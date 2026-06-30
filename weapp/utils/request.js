const app = getApp()

const request = (url, options = {}) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('auth_token')
    const header = { 'Content-Type': 'application/json' }
    if (token) header['Authorization'] = 'Bearer ' + token

    wx.request({
      url: app.globalData.baseUrl + url,
      method: options.method || 'GET',
      data: options.data,
      header,
      timeout: 60000,
      success: (res) => {
        const payload = res.data
        if (payload && payload.code && payload.code !== 200) {
          if (payload.code === 401) {
            wx.removeStorageSync('auth_token')
            wx.removeStorageSync('auth_user')
            wx.redirectTo({ url: '/pages/login/login' })
            wx.showToast({ title: '登录已过期', icon: 'none' })
            return reject(new Error(payload.message || '登录已过期'))
          }
          return reject(new Error(payload.message || '请求失败'))
        }
        resolve(payload?.data ?? payload)
      },
      fail: (err) => {
        if (err.errMsg?.includes('timeout')) {
          reject(new Error('请求超时'))
        } else {
          reject(new Error('网络异常，请检查服务器'))
        }
      }
    })
  })
}

export default {
  get(url) { return request(url, { method: 'GET' }) },
  post(url, data) { return request(url, { method: 'POST', data }) },
  put(url, data) { return request(url, { method: 'PUT', data }) },
  del(url) { return request(url, { method: 'DELETE' }) }
}
