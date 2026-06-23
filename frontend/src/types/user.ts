export interface UserInfo {
  id: number
  username: string
  realName: string
  role: string
}

export interface LoginResponse {
  token: string
  user: UserInfo
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  realName: string
}
