import axios, { type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

interface ApiResponse<T> {
  code?: number
  message?: string
  data?: T
}

const request = axios.create({
  baseURL: '/api',
  timeout: 60000
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResponse<unknown>
    if (payload && payload.code && payload.code !== 200) {
      if (!isSilentError(response.config.headers)) {
        ElMessage.error(payload.message || '请求失败')
      }
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return (payload?.data ?? payload) as unknown as AxiosResponse
  },
  (error) => {
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
