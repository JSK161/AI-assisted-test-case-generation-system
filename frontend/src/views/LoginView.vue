<template>
  <main class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="brand-icon">
          <Sparkles :size="24" />
        </div>
        <h1>登录</h1>
        <p>AI 辅助测试用例生成系统</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </div>

      <div class="demo-hint">
        演示账号：admin / 123456
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Sparkles } from '@lucide/vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { loginApi } from '@/api/auth'
import { authStore } from '@/stores/auth'

const router = useRouter()
const formRef = ref<FormInstance>()

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loading = ref(false)

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await loginApi({
      username: form.username.trim(),
      password: form.password
    })
    authStore.login(res.token, res.user)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg, #fbfbfd);
  background-image: radial-gradient(circle at 30% 40%, rgba(98, 102, 245, 0.04) 0%, transparent 50%),
                    radial-gradient(circle at 70% 60%, rgba(168, 85, 247, 0.03) 0%, transparent 50%);
  padding: 20px;
}

.auth-card {
  width: 400px;
  max-width: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.97) 0%, rgba(251, 251, 254, 0.94) 100%);
  backdrop-filter: blur(20px);
  border: 1px solid var(--color-border, #e8e8f0);
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04), 0 2px 8px rgba(0, 0, 0, 0.02);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-header .brand-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: var(--gradient-primary, linear-gradient(135deg, #6c63ff, #a855f7));
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(108, 99, 255, 0.3);
}

.auth-header h1 {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #111116 0%, #3a3a5c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 6px;
}

.auth-header p {
  color: var(--color-muted, #7a7d8d);
  font-size: 14px;
  margin: 0;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 10px;
  background: var(--gradient-primary, linear-gradient(135deg, #6c63ff, #a855f7));
  border: none;
  box-shadow: 0 2px 8px rgba(108, 99, 255, 0.25);
  transition: all 0.2s ease;
}

.submit-btn:hover {
  background: var(--gradient-primary-hover, linear-gradient(135deg, #7c73ff, #b865ff));
  box-shadow: 0 4px 14px rgba(108, 99, 255, 0.35);
  transform: translateY(-1px);
}

.auth-footer {
  text-align: center;
  color: var(--color-muted, #7a7d8d);
  font-size: 14px;
  margin-top: 8px;
}

.auth-footer a {
  color: var(--color-primary, #6266f5);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.15s;
}

.auth-footer a:hover {
  color: #a855f7;
}

.demo-hint {
  text-align: center;
  color: var(--color-muted, #7a7d8d);
  font-size: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border, #e8e8f0);
}

:deep(.el-input__wrapper) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.08) 0%, rgba(247, 247, 255, 0.12) 100%);
  border: 1px solid var(--color-border, #e8e8f0);
  box-shadow: none !important;
  border-radius: 10px;
  padding: 4px 16px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: rgba(98, 102, 245, 0.4);
  box-shadow: 0 1px 4px rgba(98, 102, 245, 0.06);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary, #6266f5);
  box-shadow: 0 2px 8px rgba(98, 102, 245, 0.1);
}

:deep(.el-input__inner) {
  color: var(--color-text, #17171d);
  height: 44px;
}

:deep(.el-input__inner::placeholder) {
  color: var(--color-muted, #7a7d8d);
}

:deep(.el-input__prefix) {
  color: var(--color-muted, #7a7d8d);
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
