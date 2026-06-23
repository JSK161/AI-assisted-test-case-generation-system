<template>
  <main class="profile-page">
    <div class="profile-card">
      <div class="profile-header">
        <div class="profile-avatar">
          <span class="avatar-letter">{{ (authStore.user?.realName || authStore.user?.username)?.[0] }}</span>
        </div>
        <div class="profile-info">
          <h2>{{ authStore.user?.realName || authStore.user?.username }}</h2>
          <p>{{ authStore.user?.username }}</p>
          <el-tag v-if="authStore.user?.role === 'ADMIN'" size="small" type="warning">管理员</el-tag>
          <el-tag v-else size="small" type="info">成员</el-tag>
        </div>
      </div>

      <div class="section-header">
        <h3>
          <History :size="18" />
          对话历史
        </h3>
        <el-button size="small" type="primary" :icon="Plus" @click="router.push('/')">新对话</el-button>
      </div>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="conversations.length === 0" class="empty-state">
        <MessageSquare :size="48" />
        <p>暂无对话历史</p>
        <el-button type="primary" @click="router.push('/')">开始新对话</el-button>
      </div>

      <div v-else class="conversation-list">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conversation-item"
          @click="openConversation(conv)"
        >
          <div class="conv-icon">
            <MessageSquare :size="18" />
          </div>
          <div class="conv-content">
            <div class="conv-title">{{ conv.title }}</div>
            <div class="conv-meta">
              <span class="conv-time">{{ formatTime(conv.updatedAt) }}</span>
            </div>
          </div>
          <button class="conv-delete" type="button" title="删除" @click.stop="removeConversation(conv.id)">
            <Trash2 :size="16" />
          </button>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { History, MessageSquare, Plus, Trash2 } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authStore } from '@/stores/auth'
import { listConversations, deleteConversation } from '@/api/conversation'
import type { ConversationListItem } from '@/api/conversation'

const router = useRouter()
const conversations = ref<ConversationListItem[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    conversations.value = await listConversations()
  } catch {
    ElMessage.error('加载对话列表失败')
  } finally {
    loading.value = false
  }
})

function openConversation(conv: ConversationListItem) {
  router.push(`/?conversationId=${conv.id}`)
}

async function removeConversation(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除此对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteConversation(id)
    conversations.value = conversations.value.filter((c) => c.id !== id)
    ElMessage.success('已删除')
  } catch {
    // cancelled
  }
}

function formatTime(dateStr: string): string {
  const d = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - d.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour}小时前`
  const diffDay = Math.floor(diffHour / 24)
  if (diffDay < 7) return `${diffDay}天前`
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
</script>

<style scoped>
.profile-page {
  min-height: calc(100vh - 56px);
  display: flex;
  justify-content: center;
  padding: 32px 20px;
  background: var(--color-bg, #fbfbfd);
}

.profile-card {
  width: min(800px, 100%);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px 32px;
  background: #ffffff;
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 12px;
  margin-bottom: 20px;
}

.profile-avatar .avatar-letter {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6c63ff, #a855f7);
  color: #fff;
  font-size: 26px;
  font-weight: 600;
}

.profile-info h2 {
  margin: 0 0 4px;
  font-size: 22px;
  color: var(--color-heading, #111116);
}

.profile-info p {
  margin: 0 0 6px;
  color: var(--color-muted, #7a7d8d);
  font-size: 14px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  color: var(--color-heading, #111116);
}

.loading-state {
  padding: 24px;
  background: #ffffff;
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 60px 20px;
  color: var(--color-muted, #7a7d8d);
  background: #ffffff;
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 12px;
}

.empty-state p {
  margin: 0;
}

.conversation-list {
  display: grid;
  gap: 8px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  background: #ffffff;
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 10px;
  cursor: pointer;
  transition: box-shadow 0.15s, border-color 0.15s;
}

.conversation-item:hover {
  border-color: var(--color-primary, #6266f5);
  box-shadow: 0 2px 8px rgba(98, 102, 245, 0.08);
}

.conv-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary, #6266f5);
  background: var(--color-primary-soft, #f1f0ff);
  border-radius: 8px;
}

.conv-content {
  flex: 1;
  min-width: 0;
}

.conv-title {
  color: var(--color-text, #17171d);
  font-weight: 500;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.conv-time {
  color: var(--color-muted, #7a7d8d);
  font-size: 12px;
}

.conv-delete {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  color: #999;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.15s, background 0.15s;
}

.conversation-item:hover .conv-delete {
  opacity: 1;
}

.conv-delete:hover {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.08);
}
</style>
