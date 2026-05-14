<template>
  <main class="login-page">
    <section class="login-brand" aria-label="系统介绍">
      <div class="login-brand__content">
        <img class="login-brand__logo" :src="tenantLogo" :alt="tenantShort" />
        <div class="login-brand__badge">{{ tenantBadge }}</div>
        <h1>创建管理员账号</h1>
        <div class="login-brand__mark" aria-hidden="true"></div>
        <p>
          为{{ tenantFull }}创建后台管理员账号，后续可进入控制台管理采购、销售、生产和库存数据。
        </p>

        <ul class="login-feature-list" aria-label="系统能力">
          <li v-for="item in features" :key="item">
            <span class="login-feature-list__icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" focusable="false">
                <path d="M5 12.5 9.2 16.8 19 7" />
              </svg>
            </span>
            <span>{{ item }}</span>
          </li>
        </ul>
      </div>
    </section>

    <section class="login-panel" aria-label="管理员注册">
      <div class="login-card">
        <div class="login-mobile-brand">
          <div class="login-mobile-brand__logo" aria-hidden="true">
            <img :src="tenantLogo" alt="" />
          </div>
          <h2>{{ tenantFull }}</h2>
        </div>

        <header class="login-header">
          <p class="login-header__eyebrow">Admin Console</p>
          <h2>管理员注册</h2>
          <p>创建账号后即可返回登录页访问系统后台。</p>
        </header>

        <form class="login-form" novalidate @submit.prevent="handleRegister">
          <div class="login-form__fields">
            <label class="form-field" for="register-username">
              <span>账号 / 用户名</span>
              <input
                id="register-username"
                v-model.trim="registerForm.username"
                type="text"
                name="username"
                autocomplete="username"
                placeholder="3-50 位字母、数字或下划线"
                :disabled="authStore.loading"
                @input="clearMessage"
              />
            </label>

            <label class="form-field" for="register-nickname">
              <span>管理员昵称</span>
              <input
                id="register-nickname"
                v-model.trim="registerForm.nickname"
                type="text"
                name="nickname"
                autocomplete="name"
                placeholder="请输入管理员显示名称"
                :disabled="authStore.loading"
                @input="clearMessage"
              />
            </label>

            <label class="form-field" for="register-password">
              <span>密码</span>
              <input
                id="register-password"
                v-model="registerForm.password"
                type="password"
                name="password"
                autocomplete="new-password"
                placeholder="至少 8 位，包含字母和数字"
                :disabled="authStore.loading"
                @input="clearMessage"
              />
            </label>

            <label class="form-field" for="register-confirm-password">
              <span>确认密码</span>
              <input
                id="register-confirm-password"
                v-model="registerForm.confirmPassword"
                type="password"
                name="confirmPassword"
                autocomplete="new-password"
                placeholder="请再次输入密码"
                :disabled="authStore.loading"
                @input="clearMessage"
              />
            </label>
          </div>

          <p v-if="message" class="login-message" :class="`login-message--${messageType}`" role="status">
            {{ message }}
          </p>

          <button class="login-submit" type="submit" :disabled="authStore.loading">
            <span v-if="!authStore.loading">创建账号</span>
            <span v-else class="login-submit__loading">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="12" cy="12" r="9" />
              </svg>
              提交中...
            </span>
          </button>

          <p class="auth-switch">
            已有管理员账号？
            <RouterLink to="/login">返回登录</RouterLink>
          </p>
        </form>

        <footer class="login-footer">
          <span>&copy; 2026 {{ tenantFull }}</span>
          <nav aria-label="注册页辅助链接">
            <a href="#" @click.prevent>服务协议</a>
            <a href="#" @click.prevent>隐私政策</a>
          </nav>
        </footer>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { ApiError } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import type { RegisterRequest } from '@/types/auth'

interface RegisterForm extends RegisterRequest {
  confirmPassword: string
}

const tenantShort = import.meta.env.VITE_TENANT_SHORT
const tenantFull = import.meta.env.VITE_TENANT_FULL
const tenantBadge = import.meta.env.VITE_TENANT_BADGE
const tenantLogo = import.meta.env.VITE_TENANT_LOGO

const features = ['管理员身份管理', '操作数据可追踪', '账户密码加密', '后台访问保护']

const router = useRouter()
const authStore = useAuthStore()

const registerForm = reactive<RegisterForm>({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const message = ref('')
const messageType = ref<'error' | 'info'>('error')

async function handleRegister() {
  clearMessage()

  if (!validateForm()) {
    return
  }

  try {
    await authStore.register({
      username: registerForm.username,
      nickname: registerForm.nickname,
      password: registerForm.password
    })
    await router.replace({
      name: 'login',
      query: {
        registered: '1',
        username: registerForm.username
      }
    })
  } catch (error) {
    messageType.value = 'error'
    message.value = resolveRegisterError(error)
  }
}

function validateForm() {
  if (!registerForm.username) {
    setError('请输入管理员账号。')
    return false
  }

  if (!/^[A-Za-z0-9_]{3,50}$/.test(registerForm.username)) {
    setError('账号需为 3-50 位字母、数字或下划线。')
    return false
  }

  if (!registerForm.nickname) {
    setError('请输入管理员昵称。')
    return false
  }

  if (registerForm.nickname.length > 80) {
    setError('管理员昵称不能超过 80 个字符。')
    return false
  }

  if (!registerForm.password) {
    setError('请输入登录密码。')
    return false
  }

  if (registerForm.password.length < 8 || registerForm.password.length > 72) {
    setError('密码长度需为 8-72 位。')
    return false
  }

  if (!/[A-Za-z]/.test(registerForm.password) || !/\d/.test(registerForm.password)) {
    setError('密码需至少包含一个字母和一个数字。')
    return false
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    setError('两次输入的密码不一致。')
    return false
  }

  return true
}

function setError(text: string) {
  messageType.value = 'error'
  message.value = text
}

function clearMessage() {
  message.value = ''
}

function resolveRegisterError(error: unknown) {
  if (error instanceof ApiError) {
    if (error.status === 409) {
      return '该账号已存在，请更换账号。'
    }
    if (error.status === 400) {
      return '注册信息不符合要求，请检查后重新提交。'
    }
    if (error.status >= 500) {
      return '服务暂时不可用，请稍后再试。'
    }
    return error.message || '注册失败，请检查输入信息。'
  }

  return '网络连接异常，请确认后端服务是否已启动。'
}
</script>
