<template>
  <view class="login-page">
    <view class="login-hero">
      <text class="login-hero__badge">PSI Mobile</text>
      <text class="login-hero__title">产销存移动端</text>
      <text class="login-hero__subtitle">给车间、仓库现场使用的移动工作台</text>
    </view>

    <view class="login-card card">
      <view class="login-card__header">
        <text class="page-title">账号登录</text>
        <text class="page-subtitle">请输入系统账号和密码</text>
      </view>

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
        <input
          v-model="form.password"
          class="form-input"
          :class="{ 'form-input--error': errors.password }"
          type="password"
          password
          placeholder="请输入密码"
          confirm-type="done"
          @confirm="handleLogin"
        />
      </FieldBlock>

      <button class="primary-button" :loading="submitting" :disabled="submitting" @click="handleLogin">
        {{ submitting ? '登录中...' : '登录' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import FieldBlock from '@/components/FieldBlock.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const submitting = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const errors = reactive({
  username: '',
  password: ''
})

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
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  padding: 96rpx 32rpx 48rpx;
  background:
    radial-gradient(circle at 20% 12%, rgba(59, 130, 246, 0.32), transparent 30%),
    linear-gradient(180deg, #dbeafe 0%, #f5f7fb 44%, #f5f7fb 100%);
}

.login-hero {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-bottom: 56rpx;
}

.login-hero__badge {
  width: fit-content;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
  font-size: 24rpx;
  font-weight: 700;
}

.login-hero__title {
  color: #0f172a;
  font-size: 52rpx;
  font-weight: 800;
}

.login-hero__subtitle {
  color: #475569;
  font-size: 28rpx;
  line-height: 1.6;
}

.login-card {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.login-card__header {
  display: flex;
  flex-direction: column;
}
</style>
