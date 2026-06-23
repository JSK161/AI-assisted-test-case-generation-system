import { reactive, computed } from 'vue'
import type { UserInfo } from '@/types/user'

interface AuthState {
  token: string | null
  user: UserInfo | null
}

const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

function loadState(): AuthState {
  const token = localStorage.getItem(TOKEN_KEY)
  const userStr = localStorage.getItem(USER_KEY)
  let user: UserInfo | null = null
  if (userStr) {
    try {
      user = JSON.parse(userStr) as UserInfo
    } catch {
      localStorage.removeItem(USER_KEY)
    }
  }
  return { token, user }
}

const state = reactive<AuthState>(loadState())

export const authStore = {
  get token() {
    return state.token
  },
  get user() {
    return state.user
  },
  get isLoggedIn() {
    return computed(() => !!state.token && !!state.user)
  },
  get isAdmin() {
    return computed(() => state.user?.role === 'ADMIN')
  },

  login(token: string, user: UserInfo) {
    state.token = token
    state.user = user
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  },

  logout() {
    state.token = null
    state.user = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }
}
