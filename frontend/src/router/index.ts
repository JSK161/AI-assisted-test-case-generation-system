import { createRouter, createWebHistory } from 'vue-router'
import AiChatView from '@/views/AiChatView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: AiChatView },
    { path: '/chat', component: AiChatView },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

export default router
