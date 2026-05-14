<template>
  <NetworkBanner />
  <view class="login-page">
    <view class="login-brand">
      <image class="login-brand__logo" src="/static/logo.png" mode="aspectFit" />
    </view>

    <view class="login-form">
      <FieldBlock label="用户名" :error="errors.username">
        <input
          v-model.trim="form.username"
          class="form-input"
          :class="{ 'form-input--error': errors.username }"
          placeholder="请输入用户名"
          confirm-type="next"
        />
      </FieldBlock>

      <FieldBlock label="密码" :error="errors.password">
        <view class="password-row">
          <input
            v-model="form.password"
            class="form-input form-input--password"
            :class="{ 'form-input--error': errors.password }"
            :type="passwordVisible ? 'text' : 'password'"
            :password="!passwordVisible"
            placeholder="请输入密码"
            confirm-type="done"
            @confirm="handleLogin"
          />
          <view class="password-toggle" @click="passwordVisible = !passwordVisible">
            <text>{{ passwordVisible ? '隐藏' : '显示' }}</text>
          </view>
        </view>
      </FieldBlock>

      <view class="remember-row" @click="rememberUsername = !rememberUsername">
        <view class="remember-checkbox" :class="{ 'remember-checkbox--checked': rememberUsername }">
          <text v-if="rememberUsername" class="remember-checkbox__tick">✓</text>
        </view>
        <text class="remember-label">记住用户名</text>
      </view>

      <button class="primary-button" :loading="submitting" :disabled="submitting" @click="handleLogin">
        {{ submitting ? '登录中...' : '登录' }}
      </button>

      <view class="login-hint">
        <text class="login-hint__text">账号由管理员在网站后台创建</text>
        <text class="login-hint__sub">如忘记密码或需开通账号，请联系管理员</text>
      </view>
    </view>

    <view class="login-footer">
      <text class="login-footer__text">v{{ versionName }}  ·  {{ backendShort }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import FieldBlock from '@/components/FieldBlock.vue'
import NetworkBanner from '@/components/NetworkBanner.vue'
import { useAuthStore } from '@/stores/auth'

const REMEMBER_KEY = 'login.rememberedUsername'
const authStore = useAuthStore()
const submitting = ref(false)
const passwordVisible = ref(false)
const rememberUsername = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const errors = reactive({
  username: '',
  password: ''
})

const versionName = '0.1.0'
const backendShort = computeBackendShort()

function computeBackendShort() {
  const url = import.meta.env.VITE_API_BASE_URL || ''
  if (!url) return '未配置后端'
  try {
    const host = url.replace(/^https?:\/\//i, '').split('/')[0]
    return host
  } catch {
    return url
  }
}

function validate() {
  errors.username = form.username ? '' : '请输入用户名'
  errors.password = form.password ? '' : '请输入密码'

  return !errors.username && !errors.password
}

async function handleLogin() {
  if (submitting.value || !validate()) {
    return
  }

  submitting.value = true

  try {
    await authStore.login({
      username: form.username,
      password: form.password
    })

    if (rememberUsername.value) {
      uni.setStorageSync(REMEMBER_KEY, form.username)
    } else {
      uni.removeStorageSync(REMEMBER_KEY)
    }

    uni.showToast({ title: '登录成功', icon: 'success' })
    uni.reLaunch({ url: '/pages/home/index' })
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '登录失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const remembered = uni.getStorageSync(REMEMBER_KEY)
  if (remembered) {
    form.username = remembered
    rememberUsername.value = true
  }
})
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  padding: calc(env(safe-area-inset-top) + 80rpx) 48rpx calc(env(safe-area-inset-bottom) + 32rpx);
  background: #ffffff;
  display: flex;
  flex-direction: column;
}

.login-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 72rpx;
}

.login-brand__logo {
  width: 280rpx;
  height: 280rpx;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
  flex: 1;
}

.password-row {
  position: relative;
}

.form-input--password {
  padding-right: 130rpx;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 18rpx;
  transform: translateY(-50%);
  padding: 8rpx 18rpx;
  border-radius: 14rpx;
  background: rgba(15, 23, 42, 0.05);
  color: #475569;
  font-size: 24rpx;
}

.password-toggle:active {
  background: rgba(15, 23, 42, 0.1);
}

.remember-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 4rpx 0;
  margin: -8rpx 0 8rpx;
}

.remember-checkbox {
  width: 36rpx;
  height: 36rpx;
  border-radius: 8rpx;
  border: 2rpx solid #cbd5e1;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remember-checkbox--checked {
  background: #1e40af;
  border-color: #1e40af;
}

.remember-checkbox__tick {
  color: #ffffff;
  font-size: 24rpx;
  line-height: 1;
}

.remember-label {
  font-size: 26rpx;
  color: #475569;
}

.login-hint {
  margin-top: 8rpx;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.login-hint__text {
  font-size: 24rpx;
  color: #64748b;
}

.login-hint__sub {
  font-size: 22rpx;
  color: #94a3b8;
}

.login-footer {
  margin-top: auto;
  padding-top: 40rpx;
  text-align: center;
}

.login-footer__text {
  font-size: 22rpx;
  color: #94a3b8;
  letter-spacing: 0.02em;
}
</style>
