import request from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest } from '@/types/user'

export function loginApi(data: LoginRequest) {
  return request.post<LoginResponse, LoginResponse>('/auth/login', data, {
    headers: { 'X-Silent-Error': 'true' }
  })
}

export function registerApi(data: RegisterRequest) {
  return request.post<LoginResponse, LoginResponse>('/auth/register', data, {
    headers: { 'X-Silent-Error': 'true' }
  })
}
