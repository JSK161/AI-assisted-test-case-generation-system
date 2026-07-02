import request from '@/utils/request'
import type { UserInfo } from '@/types/user'

export function listUsersApi() {
  return request.get<UserInfo[], UserInfo[]>('/admin/users')
}

export function updateUserRoleApi(id: number, role: string) {
  return request.put(`/admin/users/${id}/role`, { role })
}

export function deleteUserApi(id: number) {
  return request.delete(`/admin/users/${id}`)
}
