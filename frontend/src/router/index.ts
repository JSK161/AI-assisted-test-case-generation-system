import { createRouter, createWebHistory } from 'vue-router'
import AiChatView from '@/views/AiChatView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import ProfileView from '@/views/ProfileView.vue'
import { authStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: LoginView,
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      component: RegisterView,
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: AiChatView,
      meta: { requiresAuth: true }
    },
    {
      path: '/chat',
      component: AiChatView,
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      component: ProfileView,
      meta: { requiresAuth: true }
    },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

router.beforeEach((to, _from, next) => {
  const isLoggedIn = !!authStore.token
  if (to.meta.requiresAuth !== false && !isLoggedIn) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
