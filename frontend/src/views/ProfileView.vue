<template>
  <main class="profile-page">
    <div class="profile-card">
      <div class="profile-header">
        <div class="profile-avatar">
          <span class="avatar-letter">{{ (authStore.user?.realName || authStore.user?.username)?.[0] }}</span>
        </div>
        <div class="profile-info">
          <h2>{{ authStore.user?.realName || authStore.user?.username }}</h2>
          <p>@{{ authStore.user?.username }}</p>
          <div class="profile-tags">
            <el-tag v-if="authStore.user?.role === 'ADMIN'" size="small" type="warning">管理员</el-tag>
            <el-tag v-else size="small" type="info">成员</el-tag>
            <el-tag v-if="authStore.user?.email" size="small" type="success">{{ authStore.user?.email }}</el-tag>
            <el-tag v-else size="small" type="danger">未设置邮箱</el-tag>
          </div>
        </div>
      </div>

      <!-- Account Settings -->
      <div class="settings-section">
        <h3><Settings :size="18" /> 账号设置</h3>

        <div class="settings-item">
          <div class="settings-label">
            <Mail :size="16" />
            <span>邮箱</span>
          </div>
          <div class="settings-value">
            <template v-if="editingEmail">
              <el-input v-model="emailForm.email" placeholder="输入新邮箱" size="small" style="width: 220px" />
              <el-button type="primary" size="small" :loading="emailLoading" @click="saveEmail">保存</el-button>
              <el-button size="small" @click="cancelEditEmail">取消</el-button>
            </template>
            <template v-else>
              <span>{{ authStore.user?.email || '未设置' }}</span>
              <el-button link type="primary" size="small" @click="startEditEmail">修改</el-button>
            </template>
          </div>
        </div>

        <div class="settings-item">
          <div class="settings-label">
            <Lock :size="16" />
            <span>密码</span>
          </div>
          <div class="settings-value">
            <template v-if="editingPassword">
              <div class="password-fields">
                <el-input v-model="passwordForm.currentPassword" type="password" placeholder="当前密码" size="small" show-password />
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="新密码（至少6位）" size="small" show-password />
              </div>
              <div class="password-actions">
                <el-button type="primary" size="small" :loading="passwordLoading" @click="savePassword">保存</el-button>
                <el-button size="small" @click="cancelEditPassword">取消</el-button>
              </div>
            </template>
            <template v-else>
              <span>••••••••</span>
              <el-button link type="primary" size="small" @click="startEditPassword">修改</el-button>
            </template>
          </div>
        </div>
      </div>

      <!-- Admin: User Management -->
      <div v-if="authStore.isAdmin.value" class="settings-section">
        <h3><Users :size="18" /> 用户管理</h3>
        <el-table :data="userList" stripe size="small" style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="用户名" min-width="100" />
          <el-table-column prop="realName" label="姓名" min-width="90" />
          <el-table-column prop="email" label="邮箱" min-width="140">
            <template #default="{ row }">
              {{ row.email || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              <el-select
                v-if="row.id !== authStore.user?.id"
                :model-value="row.role"
                size="small"
                @change="(val: string) => updateUserRole(row.id, val)"
              >
                <el-option label="管理员" value="ADMIN" />
                <el-option label="成员" value="MEMBER" />
              </el-select>
              <el-tag v-else size="small" type="warning">当前用户</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button
                v-if="row.id !== authStore.user?.id"
                type="danger"
                size="small"
                :icon="Trash2"
                circle
                @click="deleteUser(row)"
              />
            </template>
          </el-table-column>
        </el-table>
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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { History, Lock, Mail, MessageSquare, Plus, Settings, Trash2 } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authStore } from '@/stores/auth'
import { listConversations, deleteConversation } from '@/api/conversation'
import { updateEmailApi, updatePasswordApi, getProfileApi } from '@/api/auth'
import type { ConversationListItem } from '@/api/conversation'

const router = useRouter()
const conversations = ref<ConversationListItem[]>([])
const loading = ref(true)

// Email editing
const editingEmail = ref(false)
const emailLoading = ref(false)
const emailForm = reactive({ email: '' })

function startEditEmail() {
  emailForm.email = authStore.user?.email || ''
  editingEmail.value = true
}

function cancelEditEmail() {
  editingEmail.value = false
  emailForm.email = ''
}

async function saveEmail() {
  if (!emailForm.email.trim()) {
    ElMessage.warning('请输入邮箱')
    return
  }
  emailLoading.value = true
  try {
    await updateEmailApi({ email: emailForm.email.trim() })
    if (authStore.user) authStore.user.email = emailForm.email.trim()
    ElMessage.success('邮箱已更新')
    editingEmail.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '更新失败')
  } finally {
    emailLoading.value = false
  }
}

// Password editing
const editingPassword = ref(false)
const passwordLoading = ref(false)
const passwordForm = reactive({ currentPassword: '', newPassword: '' })

function startEditPassword() {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  editingPassword.value = true
}

function cancelEditPassword() {
  editingPassword.value = false
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
}

async function savePassword() {
  if (!passwordForm.currentPassword) {
    ElMessage.warning('请输入当前密码')
    return
  }
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少需要 6 个字符')
    return
  }
  passwordLoading.value = true
  try {
    await updatePasswordApi({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码已更新')
    editingPassword.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '更新失败')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(async () => {
  try {
    const [convList] = await Promise.all([
      listConversations(),
      getProfileApi().then(u => { if (authStore.user) authStore.user.email = u.email }).catch(() => {})
    ])
    conversations.value = convList
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
  margin: 0 0 2px;
  font-size: 22px;
  color: var(--color-heading, #111116);
}

.profile-info p {
  margin: 0 0 6px;
  color: var(--color-muted, #7a7d8d);
  font-size: 14px;
}

.profile-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

/* Settings section */
.settings-section {
  background: #ffffff;
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 20px;
}

.settings-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  font-size: 16px;
  color: var(--color-heading, #111116);
}

.settings-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 0;
  border-top: 1px solid var(--color-border, #e8e8f0);
}

.settings-item:first-of-type {
  border-top: none;
  padding-top: 0;
}

.settings-label {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 80px;
  flex-shrink: 0;
  color: var(--color-muted, #7a7d8d);
  font-size: 14px;
}

.settings-value {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 14px;
  color: var(--color-text, #17171d);
}

.password-fields {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 220px;
}

.password-actions {
  display: flex;
  gap: 6px;
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
