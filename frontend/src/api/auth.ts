import request from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest, UpdateEmailRequest, UpdatePasswordRequest, UserInfo } from '@/types/user'

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

export function getProfileApi() {
  return request.get<UserInfo, UserInfo>('/auth/profile')
}

export function updateEmailApi(data: UpdateEmailRequest) {
  return request.put<void, void>('/auth/email', data)
}

export function updatePasswordApi(data: UpdatePasswordRequest) {
  return request.put<void, void>('/auth/password', data)
}
