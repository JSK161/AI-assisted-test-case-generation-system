<template>
  <div class="app-shell">
    <nav v-if="authStore.token" class="top-nav">
      <div class="nav-brand">
        <Sparkles :size="18" />
        <span>测例 AI</span>
      </div>

      <div class="nav-right">
        <span class="user-info">
          <UserRound :size="16" />
          {{ authStore.user?.realName || authStore.user?.username }}
          <el-tag v-if="authStore.user?.role === 'ADMIN'" size="small" type="warning" effect="dark">管理员</el-tag>
          <el-tag v-else size="small" type="info" effect="dark">成员</el-tag>
        </span>
        <el-dropdown trigger="click" @command="handleCommand">
          <button class="avatar-btn" type="button">
            <span class="avatar-circle">{{ (authStore.user?.realName || authStore.user?.username)?.[0] }}</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile" disabled>
                {{ authStore.user?.username }}
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <span style="color: #f56c6c">退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </nav>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { Sparkles, UserRound } from '@lucide/vue'
import { authStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()

function handleCommand(command: string) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      authStore.logout()
      router.push('/login')
    }).catch(() => {
      // cancelled
    })
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}

.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #0f0c29 0%, #1a1a3e 50%, #24243e 100%);
}

.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: rgba(255, 255, 255, 0.04);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(12px);
  flex-shrink: 0;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #c4b5fd;
  font-weight: 600;
  font-size: 16px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.avatar-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.avatar-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6c63ff, #a855f7);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* Element Plus dropdown dark theme overrides */
.el-dropdown-menu {
  background: #1e1e3a !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.el-dropdown-menu__item {
  color: rgba(255, 255, 255, 0.8) !important;
}

.el-dropdown-menu__item:hover {
  background: rgba(108, 99, 255, 0.2) !important;
  color: #c4b5fd !important;
}
</style>
