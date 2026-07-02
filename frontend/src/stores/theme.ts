import { reactive, computed } from 'vue'

type Theme = 'light' | 'dark'

const STORAGE_KEY = 'app_theme'

function loadTheme(): Theme {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved === 'dark' || saved === 'light') return saved
  return 'light'
}

const state = reactive<{ theme: Theme }>({
  theme: loadTheme()
})

function applyTheme(theme: Theme) {
  document.documentElement.setAttribute('data-theme', theme)
}

// Apply on init
applyTheme(state.theme)

export const themeStore = {
  get theme() {
    return state.theme
  },
  get isDark() {
    return computed(() => state.theme === 'dark')
  },
  toggle() {
    state.theme = state.theme === 'light' ? 'dark' : 'light'
    localStorage.setItem(STORAGE_KEY, state.theme)
    applyTheme(state.theme)
  },
  setTheme(theme: Theme) {
    state.theme = theme
    localStorage.setItem(STORAGE_KEY, theme)
    applyTheme(theme)
  }
}
