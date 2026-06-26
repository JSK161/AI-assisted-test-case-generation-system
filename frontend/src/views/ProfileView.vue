<template>
  <main class="profile-page">
    <div class="profile-card">
      <!-- Profile Header -->
      <div class="profile-header">
        <div class="profile-avatar">
          <span class="avatar-letter">{{ (authStore.user?.realName || authStore.user?.username)?.[0] }}</span>
        </div>
        <div class="profile-info">
          <h2>{{ authStore.user?.realName || authStore.user?.username }}</h2>
          <p class="profile-username">@{{ authStore.user?.username }}</p>
          <div class="profile-tags">
            <el-tag v-if="authStore.user?.role === 'ADMIN'" size="small" type="warning">管理员</el-tag>
            <el-tag v-else size="small" type="info">成员</el-tag>
          </div>
        </div>
      </div>

      <!-- Settings Grid -->
      <div class="settings-grid">
        <!-- Email -->
        <div class="settings-card" @click="!editingEmail && startEditEmail()">
          <div class="settings-card-icon"><Mail :size="18" /></div>
          <div class="settings-card-body">
            <span class="settings-card-label">邮箱</span>
            <template v-if="editingEmail">
              <el-input v-model="emailForm.email" placeholder="输入新邮箱" size="small" @keyup.enter="saveEmail" />
              <div class="settings-card-actions">
                <el-button type="primary" size="small" :loading="emailLoading" @click.stop="saveEmail">保存</el-button>
                <el-button size="small" @click.stop="cancelEditEmail">取消</el-button>
              </div>
            </template>
            <span v-else class="settings-card-value">{{ authStore.user?.email || '未设置' }}</span>
          </div>
        </div>

        <!-- Password -->
        <div class="settings-card" @click="!editingPassword && startEditPassword()">
          <div class="settings-card-icon"><Lock :size="18" /></div>
          <div class="settings-card-body">
            <span class="settings-card-label">密码</span>
            <template v-if="editingPassword">
              <el-input v-model="passwordForm.currentPassword" type="password" placeholder="当前密码" size="small" show-password />
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="新密码（至少6位）" size="small" show-password style="margin-top:6px" />
              <div class="settings-card-actions">
                <el-button type="primary" size="small" :loading="passwordLoading" @click.stop="savePassword">保存</el-button>
                <el-button size="small" @click.stop="cancelEditPassword">取消</el-button>
              </div>
            </template>
            <span v-else class="settings-card-value">••••••••</span>
          </div>
        </div>
      </div>

      <!-- Admin: User Management -->
      <div v-if="authStore.isAdmin.value" class="section-block">
        <div class="section-block-header">
          <Users :size="18" />
          <span>用户管理</span>
          <span class="section-badge">{{ userList.length }} 人</span>
        </div>
        <div class="user-table-wrap">
          <div class="user-table-inner">
            <div class="user-row user-header">
              <span class="col-id">ID</span>
              <span class="col-name">用户名</span>
              <span class="col-name">姓名</span>
              <span class="col-email">邮箱</span>
              <span class="col-role">角色</span>
              <span class="col-action">操作</span>
            </div>
            <div v-for="row in userList" :key="row.id" class="user-row">
              <span class="col-id">{{ row.id }}</span>
              <span class="col-name">{{ row.username }}</span>
              <span class="col-name">{{ row.realName }}</span>
              <span class="col-email">{{ row.email || '-' }}</span>
              <span class="col-role">
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
              </span>
              <span class="col-action">
                <button v-if="row.id !== authStore.user?.id" class="user-delete-btn" type="button" title="删除用户" @click="deleteUser(row)">
                  <Trash2 :size="15" />
                </button>
              </span>
            </div>
          </div>
        </div>
        <button class="section-block-action" @click="refreshUsers">
          <RefreshCw :size="14" />
          刷新
        </button>
      </div>

      <!-- Conversation History -->
      <div class="section-block">
        <div class="section-block-header">
          <MessageSquare :size="18" />
          <span>对话历史</span>
          <span class="section-badge">{{ conversations.length }}</span>
        </div>

        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>

        <div v-else-if="conversations.length === 0" class="empty-state">
          <MessageSquare :size="36" />
          <p>暂无对话历史</p>
          <el-button type="primary" size="small" @click="router.push('/')">开始新对话</el-button>
        </div>

        <div v-else class="conv-list">
          <div v-for="conv in conversations" :key="conv.id" class="conv-item" @click="openConversation(conv)">
            <div class="conv-icon"><MessageSquare :size="16" /></div>
            <div class="conv-content">
              <div class="conv-title">{{ conv.title }}</div>
              <span class="conv-time">{{ formatTime(conv.updatedAt) }}</span>
            </div>
            <button class="conv-del" type="button" title="删除" @click.stop="removeConversation(conv.id)">
              <Trash2 :size="14" />
            </button>
          </div>
        </div>

        <button class="section-block-action" @click="router.push('/')">
          <Plus :size="14" />
          新对话
        </button>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, Mail, MessageSquare, Plus, RefreshCw, Settings, Trash2, Users } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authStore } from '@/stores/auth'
import { listConversations, deleteConversation } from '@/api/conversation'
import { updateEmailApi, updatePasswordApi, getProfileApi } from '@/api/auth'
import { listUsersApi, updateUserRoleApi, deleteUserApi } from '@/api/admin'
import type { ConversationListItem } from '@/api/conversation'
import type { UserInfo } from '@/types/user'

const router = useRouter()
const conversations = ref<ConversationListItem[]>([])
const loading = ref(true)
const userList = ref<UserInfo[]>([])

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
  if (!emailForm.email.trim()) { ElMessage.warning('请输入邮箱'); return }
  emailLoading.value = true
  try {
    await updateEmailApi({ email: emailForm.email.trim() })
    if (authStore.user) authStore.user.email = emailForm.email.trim()
    ElMessage.success('邮箱已更新')
    editingEmail.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '更新失败')
  } finally { emailLoading.value = false }
}

// Password editing
const editingPassword = ref(false)
const passwordLoading = ref(false)
const passwordForm = reactive({ currentPassword: '', newPassword: '' })

function startEditPassword() {
  passwordForm.currentPassword = ''; passwordForm.newPassword = ''
  editingPassword.value = true
}

function cancelEditPassword() {
  editingPassword.value = false
  passwordForm.currentPassword = ''; passwordForm.newPassword = ''
}

async function savePassword() {
  if (!passwordForm.currentPassword) { ElMessage.warning('请输入当前密码'); return }
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) { ElMessage.warning('新密码至少需要 6 个字符'); return }
  passwordLoading.value = true
  try {
    await updatePasswordApi({ currentPassword: passwordForm.currentPassword, newPassword: passwordForm.newPassword })
    ElMessage.success('密码已更新')
    editingPassword.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '更新失败')
  } finally { passwordLoading.value = false }
}

// Admin
async function loadUsers() {
  try { userList.value = await listUsersApi() } catch { /* ignore */ }
}

async function refreshUsers() { await loadUsers(); ElMessage.success('已刷新') }

async function updateUserRole(id: number, role: string) {
  try {
    await updateUserRoleApi(id, role)
    const u = userList.value.find((x) => x.id === id)
    if (u) u.role = role
  } catch (e: any) { ElMessage.error(e.message || '更新失败') }
}

async function deleteUser(row: UserInfo) {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}"？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteUserApi(row.id)
    userList.value = userList.value.filter((u) => u.id !== row.id)
  } catch { /* cancelled */ }
}

// Init
onMounted(async () => {
  try {
    conversations.value = await listConversations()
    getProfileApi().then((u) => { if (authStore.user) authStore.user.email = u.email }).catch(() => {})
    if (authStore.isAdmin.value) loadUsers()
  } catch { /* ignore */
  } finally { loading.value = false }
})

function openConversation(conv: ConversationListItem) {
  router.push(`/?conversationId=${conv.id}`)
}

async function removeConversation(id: number) {
  try {
    await ElMessageBox.confirm('确定删除此对话？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteConversation(id)
    conversations.value = conversations.value.filter((c) => c.id !== id)
  } catch { /* cancelled */ }
}

function formatTime(dateStr: string): string {
  const d = new Date(dateStr); const now = new Date(); const diff = now.getTime() - d.getTime(); const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'; if (min < 60) return `${min}分钟前`; const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour}小时前`; const day = Math.floor(hour / 24)
  if (day < 7) return `${day}天前`
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
  background-image: radial-gradient(circle at 30% 40%, rgba(98, 102, 245, 0.04) 0%, transparent 50%),
                    radial-gradient(circle at 70% 60%, rgba(168, 85, 247, 0.03) 0%, transparent 50%);
}

.profile-card {
  width: min(720px, 100%);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Profile Header */
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 28px;
  background: var(--color-surface, #ffffff);
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 14px;
  box-shadow: var(--shadow-light);
  transition: box-shadow 0.2s ease, background 0.2s ease;
}

.profile-avatar .avatar-letter {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px; height: 60px;
  border-radius: 50%;
  background: var(--gradient-primary, linear-gradient(135deg, #6c63ff, #a855f7));
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(108, 99, 255, 0.3);
}

.profile-info h2 { margin: 0; font-size: 20px; color: var(--color-heading, #111116); }
.profile-username { margin: 2px 0 0; font-size: 13px; color: var(--color-muted, #7a7d8d); }
.profile-tags { display: flex; gap: 6px; margin-top: 6px; }

/* Settings Grid */
.settings-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.settings-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 20px;
  background: var(--color-surface, #ffffff);
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  cursor: pointer;
  transition: all 0.2s ease;
}

.settings-card:hover {
  box-shadow: var(--shadow-soft);
  border-color: var(--color-primary, #6266f5);
}

.settings-card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px; height: 38px;
  flex-shrink: 0;
  color: var(--color-primary, #6266f5);
  background: var(--color-primary-soft, #f1f0ff);
  border-radius: 10px;
}

.settings-card-body {
  flex: 1;
  min-width: 0;
}

.settings-card-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-heading, #111116);
  margin-bottom: 4px;
}

.settings-card-value {
  font-size: 13px;
  color: var(--color-muted, #7a7d8d);
}

.settings-card-actions {
  display: flex;
  gap: 6px;
  margin-top: 8px;
}

/* Section Blocks */
.section-block {
  background: var(--color-surface, #ffffff);
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow-light);
  transition: box-shadow 0.2s ease, background 0.2s ease;
}

.section-block-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-heading, #111116);
}

.section-badge {
  margin-left: auto;
  padding: 0 8px;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-muted, #7a7d8d);
  background: var(--color-surface-soft, #f7f7ff);
  border-radius: 10px;
  line-height: 20px;
}

.section-block-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 6px 14px;
  font-size: 12px;
  color: var(--color-primary, #6266f5);
  background: transparent;
  border: 1px solid var(--color-primary-soft, #f1f0ff);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.section-block-action:hover {
  background: var(--color-primary-soft, #f1f0ff);
}

/* User Table */
.user-table-wrap {
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 8px;
  overflow: hidden;
}

.user-row {
  display: grid;
  grid-template-columns: 50px 1fr 1fr 1.4fr 120px 70px;
  align-items: center;
  padding: 10px 14px;
  font-size: 13px;
  color: var(--color-text, #17171d);
  border-bottom: 1px solid var(--color-border, #e8e8f0);
  transition: background 0.15s;
}

.user-row:last-child { border-bottom: none; }
.user-row:not(.user-header):hover { background: var(--color-primary-soft, #f1f0ff); }

.user-header {
  font-weight: 600;
  font-size: 12px;
  color: var(--color-muted, #7a7d8d);
  background: var(--color-surface-soft, #f7f7ff);
}

.user-delete-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  color: #999;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
}

.user-delete-btn:hover { color: #f56c6c; background: rgba(245, 108, 108, 0.08); }

/* Conversation List */
.conv-list {
  display: grid;
  gap: 6px;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;
}

.conv-item:hover {
  border-color: var(--color-primary, #6266f5);
  background: var(--color-primary-soft, #f1f0ff);
}

.conv-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px; height: 32px;
  flex-shrink: 0;
  color: var(--color-primary, #6266f5);
  background: var(--color-primary-soft, #f1f0ff);
  border-radius: 8px;
}

.conv-content { flex: 1; min-width: 0; }

.conv-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text, #17171d);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.conv-time { font-size: 12px; color: var(--color-muted, #7a7d8d); }

.conv-del {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  color: #999;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.15s;
}

.conv-item:hover .conv-del { opacity: 1; }
.conv-del:hover { color: #f56c6c; background: rgba(245, 108, 108, 0.08); }

.loading-state { padding: 20px; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px 20px;
  color: var(--color-muted, #7a7d8d);
}

/* Element Plus overrides */
:deep(.el-select) { width: 90px; }
:deep(.el-select .el-input__wrapper) { background: var(--color-surface, #ffffff); box-shadow: 0 0 0 1px var(--color-border, #e8e8f0) inset; }
:deep(.el-popper) { --el-bg-color: var(--color-surface, #ffffff); --el-border-color-light: var(--color-border, #e8e8f0); --el-text-color-regular: var(--color-text, #17171d); }

/* Responsive */
@media (max-width: 640px) {
  .settings-grid { grid-template-columns: 1fr; }
  .user-row { grid-template-columns: 40px 1fr 1fr; gap: 8px; }
  .col-email, .col-role { display: none; }
}
</style>
