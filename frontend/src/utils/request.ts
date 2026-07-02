import axios, { type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { authStore } from '@/stores/auth'
import router from '@/router'

interface ApiResponse<T> {
  code?: number
  message?: string
  data?: T
}

const request = axios.create({
  baseURL: '/api',
  timeout: 60000
})

// Request interceptor — attach JWT token
request.interceptors.request.use((config) => {
  const token = authStore.token
  if (token && config.headers) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResponse<unknown>
    if (payload && payload.code && payload.code !== 200) {
      if (payload.code === 401) {
        authStore.logout()
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
        return Promise.reject(new Error('登录已过期'))
      }
      if (!isSilentError(response.config.headers)) {
        ElMessage.error(payload.message || '请求失败')
      }
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return (payload?.data ?? payload) as unknown as AxiosResponse
  },
  (error) => {
    if (error.response?.status === 401) {
      authStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(new Error('登录已过期'))
    }
    const message = error.response?.data?.message || error.message || '服务暂时不可用'
    if (!isSilentError(error.config?.headers)) {
      ElMessage.error(message)
    }
    return Promise.reject(new Error(message))
  }
)

function isSilentError(headers: unknown): boolean {
  if (!headers) {
    return false
  }
  if (typeof headers === 'object' && 'get' in headers && typeof headers.get === 'function') {
    return headers.get('X-Silent-Error') === 'true'
  }
  return Boolean((headers as Record<string, string>)['X-Silent-Error'])
}

export default request
