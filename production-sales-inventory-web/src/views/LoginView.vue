<template>
  <main class="login-page">
    <section class="login-brand" aria-label="系统介绍">
      <div class="login-brand__content">
        <img class="login-brand__logo" :src="tenantLogo" :alt="tenantShort" />
        <div class="login-brand__badge">{{ tenantBadge }}</div>
        <h1>{{ tenantFull }}</h1>
        <div class="login-brand__mark" aria-hidden="true"></div>
        <p>
          覆盖采购、销售、生产、库存和往来资料，帮助团队实时掌握经营状态和业务变化。
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

    <section class="login-panel" aria-label="管理员登录">
      <div class="login-card">
        <div class="login-mobile-brand">
          <div class="login-mobile-brand__logo" aria-hidden="true">
            <img :src="tenantLogo" alt="" />
          </div>
          <h2>{{ tenantFull }}</h2>
        </div>

        <header class="login-header">
          <p class="login-header__eyebrow">Admin Console</p>
          <h2>欢迎登录</h2>
          <p>请输入管理员账号和密码访问控制台。</p>
        </header>

        <form class="login-form" novalidate @submit.prevent="handleLogin">
          <div class="login-form__fields">
            <label class="form-field" for="username">
              <span>账号 / 用户名</span>
              <input
                id="username"
                v-model.trim="loginForm.username"
                type="text"
                name="username"
                autocomplete="username"
                placeholder="请输入您的登录账号"
                :disabled="authStore.loading"
                @input="clearMessage"
              />
            </label>

            <label class="form-field" for="password">
              <span>密码</span>
              <input
                id="password"
                v-model="loginForm.password"
                type="password"
                name="password"
                autocomplete="current-password"
                placeholder="请输入您的密码"
                :disabled="authStore.loading"
                @input="clearMessage"
              />
            </label>
          </div>

          <div class="login-options">
            <label class="remember-option" for="remember">
              <input
                id="remember"
                v-model="rememberLogin"
                type="checkbox"
                :disabled="authStore.loading"
              />
              <span>记住登录状态</span>
            </label>

            <button class="text-button" type="button" @click="showPasswordTip">
              忘记密码？
            </button>
          </div>

          <p v-if="message" class="login-message" :class="`login-message--${messageType}`" role="status">
            {{ message }}
          </p>

          <button class="login-submit" type="submit" :disabled="authStore.loading">
            <span v-if="!authStore.loading">进入系统</span>
            <span v-else class="login-submit__loading">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="12" cy="12" r="9" />
              </svg>
              验证中...
            </span>
          </button>

          <p class="auth-switch">
            还没有管理员账号？
            <RouterLink to="/register">创建账号</RouterLink>
          </p>
        </form>

        <footer class="login-footer">
          <span>&copy; 2026 {{ tenantFull }}</span>
          <nav aria-label="登录页辅助链接">
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
import { useRoute, useRouter } from 'vue-router'

import { ApiError } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import type { LoginRequest } from '@/types/auth'

const tenantShort = import.meta.env.VITE_TENANT_SHORT
const tenantFull = import.meta.env.VITE_TENANT_FULL
const tenantBadge = import.meta.env.VITE_TENANT_BADGE
const tenantLogo = import.meta.env.VITE_TENANT_LOGO

const features = ['产销存数据联动', '库存预警提醒', '供应商客户协同', '安全加密防护']

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginForm = reactive<LoginRequest>({
  username: resolveInitialUsername(),
  password: ''
})

const rememberLogin = ref(authStore.remembered)
const message = ref('')
const messageType = ref<'error' | 'info'>('error')

if (route.query.registered === '1') {
  messageType.value = 'info'
  message.value = '注册成功，请使用新账号登录。'
}

async function handleLogin() {
  clearMessage()

  if (!validateForm()) {
    return
  }

  try {
    await authStore.login(
      {
        username: loginForm.username,
        password: loginForm.password
      },
      rememberLogin.value
    )
    await router.replace(resolveRedirectPath())
  } catch (error) {
    messageType.value = 'error'
    message.value = resolveLoginError(error)
  }
}

function validateForm() {
  if (!loginForm.username) {
    messageType.value = 'error'
    message.value = '请输入管理员账号。'
    return false
  }

  if (!loginForm.password) {
    messageType.value = 'error'
    message.value = '请输入登录密码。'
    return false
  }

  return true
}

function clearMessage() {
  message.value = ''
}

function showPasswordTip() {
  messageType.value = 'info'
  message.value = '请联系系统管理员重置密码。'
}

function resolveRedirectPath() {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/')) {
    return redirect
  }
  return '/dashboard'
}

function resolveInitialUsername() {
  const username = route.query.username
  return typeof username === 'string' ? username : ''
}

function resolveLoginError(error: unknown) {
  if (error instanceof ApiError) {
    if (error.status === 401) {
      return '账号或密码错误，请重新输入。'
    }
    if (error.status === 403) {
      return '该管理员账号已被禁用，请联系系统负责人。'
    }
    if (error.status >= 500) {
      return '服务暂时不可用，请稍后再试。'
    }
    return error.message || '登录失败，请检查输入信息。'
  }

  return '网络连接异常，请确认后端服务是否已启动。'
}
</script>
