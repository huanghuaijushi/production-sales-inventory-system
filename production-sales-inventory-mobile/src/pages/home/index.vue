<template>
  <view class="container home-page">
    <view class="welcome-card">
      <image class="welcome-card__avatar" :src="workerAvatar" mode="aspectFit" />
      <view class="welcome-card__content">
        <text class="welcome-card__greeting">{{ greetingText }}，{{ authStore.displayName }}</text>
        <text class="welcome-card__meta">{{ workerMeta }}</text>
      </view>
      <image class="welcome-card__factory" :src="factoryMark" mode="aspectFit" />
    </view>

    <view class="section-title">
      <text class="section-title__main">今日常用</text>
      <text class="section-title__hint">按权限自动显示功能</text>
    </view>

    <view :class="['actions', isListMode ? 'actions--list' : 'actions--grid']">
      <view
        v-for="item in visibleShortcuts"
        :key="item.path"
        :class="[
          'action-card',
          `action-card--${item.tone}`,
          item.featured && !isListMode ? 'action-card--featured' : '',
          isListMode ? 'action-card--list' : ''
        ]"
        @click="navigate(item.path)"
      >
        <image class="action-card__icon" :src="item.icon" mode="aspectFit" />
        <text class="action-card__title">{{ item.title }}</text>
        <view class="action-card__arrow">
          <text>›</text>
        </view>
      </view>
    </view>

    <view v-if="!visibleShortcuts.length" class="empty-state">
      <text>当前账号暂无移动端功能权限</text>
    </view>

    <button class="logout-link" @click="handleLogout">退出登录 ›</button>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

type ShortcutTone = 'blue' | 'green' | 'orange'

interface ShortcutItem {
  title: string
  path: string
  icon: string
  tone: ShortcutTone
  featured?: boolean
  permissions: string[]
}

const shortcuts: ShortcutItem[] = [
  {
    title: '生产报工',
    path: '/pages/production/index',
    icon: svgToDataUri(`
      <svg width="160" height="160" viewBox="0 0 160 160" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="80" cy="84" r="58" fill="#DBEAFE"/>
        <rect x="43" y="34" width="74" height="94" rx="10" fill="#FFFFFF" stroke="#2563EB" stroke-width="10"/>
        <path d="M65 34h30l4 17H61l4-17Z" fill="#2563EB"/>
        <circle cx="80" cy="34" r="9" fill="#2563EB"/>
        <path d="M62 77h36M62 94h26" stroke="#B8D1FF" stroke-width="8" stroke-linecap="round"/>
        <path d="M61 111l16 16 30-38" stroke="#2563EB" stroke-width="12" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    `),
    tone: 'blue',
    featured: true,
    permissions: ['production:report', 'production:work', 'mobile:production']
  },
  {
    title: '库存查询',
    path: '/pages/inventory/index',
    icon: svgToDataUri(`
      <svg width="150" height="150" viewBox="0 0 150 150" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect x="34" y="28" width="10" height="88" rx="3" fill="#2563EB"/>
        <rect x="106" y="28" width="10" height="88" rx="3" fill="#2563EB"/>
        <rect x="29" y="45" width="92" height="9" rx="4" fill="#2563EB"/>
        <rect x="29" y="82" width="92" height="9" rx="4" fill="#2563EB"/>
        <rect x="45" y="56" width="26" height="26" rx="4" fill="#8DBBFF"/>
        <rect x="75" y="56" width="30" height="26" rx="4" fill="#B7D4FF"/>
        <rect x="45" y="93" width="38" height="24" rx="4" fill="#B7D4FF"/>
        <rect x="87" y="93" width="18" height="24" rx="4" fill="#8DBBFF"/>
      </svg>
    `),
    tone: 'blue',
    permissions: ['inventory:view', 'stock:view', 'mobile:inventory']
  },
  {
    title: '入库',
    path: '/pages/stock-in/index',
    icon: svgToDataUri(`
      <svg width="150" height="150" viewBox="0 0 150 150" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M43 73l31-16 33 16-32 17-32-17Z" fill="#8EE7BF"/>
        <path d="M43 73v38l32 18V90L43 73Z" fill="#65D8A4"/>
        <path d="M107 73v38l-32 18V90l32-17Z" fill="#3CC58E"/>
        <path d="M75 25v54" stroke="#10B981" stroke-width="14" stroke-linecap="round"/>
        <path d="M52 56l23 23 23-23" stroke="#10B981" stroke-width="14" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    `),
    tone: 'green',
    permissions: ['stock:in', 'inbound:create', 'mobile:stock-in']
  },
  {
    title: '出库',
    path: '/pages/stock-out/index',
    icon: svgToDataUri(`
      <svg width="150" height="150" viewBox="0 0 150 150" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M40 81l35-18 36 18-36 19-35-19Z" fill="#8DBBFF"/>
        <path d="M40 81v36l35 19v-36L40 81Z" fill="#60A5FA"/>
        <path d="M111 81v36l-36 19v-36l36-19Z" fill="#3B82F6"/>
        <path d="M69 62l36-36" stroke="#2563EB" stroke-width="13" stroke-linecap="round"/>
        <path d="M102 59l3-33-33 3" stroke="#2563EB" stroke-width="13" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    `),
    tone: 'blue',
    permissions: ['stock:out', 'outbound:create', 'mobile:stock-out']
  },
  {
    title: '异常上报',
    path: '/pages/exception/index',
    icon: svgToDataUri(`
      <svg width="150" height="150" viewBox="0 0 150 150" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M64 30c5-9 17-9 22 0l43 75c5 9-1 20-11 20H32c-10 0-16-11-11-20l43-75Z" fill="#FF6B3D"/>
        <path d="M75 57v30" stroke="#FFFFFF" stroke-width="12" stroke-linecap="round"/>
        <circle cx="75" cy="106" r="7" fill="#FFFFFF"/>
      </svg>
    `),
    tone: 'orange',
    permissions: ['exception:create', 'exception:report', 'mobile:exception']
  }
]

const workerAvatar = svgToDataUri(`
  <svg width="136" height="136" viewBox="0 0 136 136" fill="none" xmlns="http://www.w3.org/2000/svg">
    <circle cx="68" cy="68" r="64" fill="#EAF2FF"/>
    <path d="M37 111c6-20 18-30 31-30s25 10 31 30c-9 7-19 11-31 11s-22-4-31-11Z" fill="#365D95"/>
    <circle cx="68" cy="60" r="29" fill="#FFD1A8"/>
    <path d="M40 56c3-22 14-34 28-34s25 12 28 34H40Z" fill="#2563EB"/>
    <path d="M39 50h58c4 0 7 3 7 7v2H32v-2c0-4 3-7 7-7Z" fill="#1D4ED8"/>
    <circle cx="57" cy="63" r="3" fill="#111827"/>
    <circle cx="79" cy="63" r="3" fill="#111827"/>
    <path d="M58 77c6 7 14 7 20 0" stroke="#111827" stroke-width="4" stroke-linecap="round"/>
  </svg>
`)

const factoryMark = svgToDataUri(`
  <svg width="210" height="120" viewBox="0 0 210 120" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M35 95V70l43-15v40H35Z" fill="#E8EEF8"/>
    <path d="M78 95V51l44-16v60H78Z" fill="#E2EAF6"/>
    <path d="M122 95V44h42v51h-42Z" fill="#DDE7F4"/>
    <path d="M166 95V18h20l7 77h-27Z" fill="#D9E4F2"/>
    <rect x="135" y="59" width="10" height="11" rx="2" fill="#F8FAFC"/>
    <rect x="153" y="59" width="10" height="11" rx="2" fill="#F8FAFC"/>
    <path d="M25 47c2-9 9-15 18-15 8 0 15 5 18 12 7 0 12 5 12 12H19c0-5 2-8 6-9Z" fill="#E8EEF8"/>
  </svg>
`)

const greetingText = computed(() => {
  const hour = new Date().getHours()

  if (hour < 6) return '夜班辛苦'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const workerMeta = computed(() => {
  const username = authStore.sysUser?.username || '-'
  const roles = authStore.sysUser?.roles || []
  const role = roles.find((item) => item !== 'SUPER_ADMIN') || roles[0] || ''

  return `${formatRole(role)} · 工号 ${username}`
})

const visibleShortcuts = computed(() => {
  const permissions = authStore.sysUser?.permissions || []
  const roles = authStore.sysUser?.roles || []

  if (!permissions.length || roles.some((role) => ['ADMIN', 'SUPER_ADMIN'].includes(role))) {
    return shortcuts
  }

  const matchedShortcuts = shortcuts.filter((shortcut) =>
    shortcut.permissions.some((permission) => permissions.includes(permission))
  )

  return matchedShortcuts.length ? matchedShortcuts : shortcuts
})

const isListMode = computed(() => visibleShortcuts.value.length <= 3)

onShow(() => {
  if (!authStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/index' })
  }
})

function navigate(path: string) {
  uni.navigateTo({ url: path })
}

async function handleLogout() {
  await authStore.logout()
  uni.showToast({ title: '已退出登录', icon: 'none' })
  uni.reLaunch({ url: '/pages/login/index' })
}

function formatRole(role: string) {
  const roleMap: Record<string, string> = {
    ADMIN: '现场管理',
    SUPER_ADMIN: '现场管理',
    WAREHOUSE: '仓储组',
    PRODUCTION: '生产组',
    QUALITY: '质检组',
    WORKER: '生产组'
  }

  return roleMap[role] || role || '现场岗位'
}

function svgToDataUri(svg: string) {
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg.trim())}`
}
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  padding: 32rpx 32rpx 42rpx;
  background:
    radial-gradient(circle at 20% 8%, rgba(37, 99, 235, 0.1), transparent 36%),
    linear-gradient(180deg, #f7fbff 0%, #f4f7fb 100%);
}

.welcome-card {
  position: relative;
  min-height: 176rpx;
  margin-bottom: 42rpx;
  padding: 28rpx;
  overflow: hidden;
  display: flex;
  align-items: center;
  border: 2rpx solid rgba(226, 232, 240, 0.75);
  border-radius: 26rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18rpx 52rpx rgba(15, 23, 42, 0.08);
}

.welcome-card__avatar {
  position: relative;
  z-index: 1;
  width: 116rpx;
  height: 116rpx;
  flex: 0 0 116rpx;
  margin-right: 24rpx;
}

.welcome-card__content {
  position: relative;
  z-index: 1;
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.welcome-card__greeting {
  font-size: 38rpx;
  font-weight: 800;
  line-height: 1.25;
  color: #0f172a;
}

.welcome-card__meta {
  font-size: 27rpx;
  line-height: 1.4;
  color: #475569;
}

.welcome-card__factory {
  position: absolute;
  right: -14rpx;
  bottom: 0;
  width: 210rpx;
  height: 120rpx;
  opacity: 0.9;
}

.section-title {
  display: flex;
  align-items: baseline;
  gap: 22rpx;
  margin-bottom: 24rpx;
}

.section-title__main {
  font-size: 42rpx;
  font-weight: 800;
  color: #0f172a;
}

.section-title__hint {
  font-size: 25rpx;
  color: #7c8ba1;
}

.actions {
  margin-bottom: 42rpx;
}

.actions--grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 22rpx;
}

.actions--list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.action-card {
  position: relative;
  min-height: 260rpx;
  padding: 26rpx 24rpx 24rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(226, 232, 240, 0.74);
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 16rpx 42rpx rgba(15, 23, 42, 0.07);
}

.action-card--featured {
  grid-column: 1 / -1;
  min-height: 252rpx;
  padding: 34rpx 76rpx 34rpx 36rpx;
  flex-direction: row;
  justify-content: flex-start;
}

.action-card--list {
  min-height: 168rpx;
  padding: 24rpx 80rpx 24rpx 28rpx;
  flex-direction: row;
  justify-content: flex-start;
}

.action-card__icon {
  width: 142rpx;
  height: 142rpx;
  margin-bottom: 14rpx;
}

.action-card--featured .action-card__icon {
  width: 170rpx;
  height: 170rpx;
  margin-right: 42rpx;
  margin-bottom: 0;
}

.action-card--list .action-card__icon {
  width: 116rpx;
  height: 116rpx;
  margin-right: 30rpx;
  margin-bottom: 0;
}

.action-card__title {
  max-width: 100%;
  font-size: 38rpx;
  font-weight: 800;
  line-height: 1.2;
  color: #0f172a;
  text-align: center;
}

.action-card--featured .action-card__title {
  font-size: 54rpx;
  text-align: left;
}

.action-card--list .action-card__title {
  font-size: 44rpx;
  text-align: left;
}

.action-card__arrow {
  position: absolute;
  right: 28rpx;
  bottom: 28rpx;
  width: 54rpx;
  height: 54rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 48rpx;
  line-height: 1;
}

.action-card--featured .action-card__arrow,
.action-card--list .action-card__arrow {
  top: 50%;
  bottom: auto;
  transform: translateY(-50%);
}

.action-card--blue .action-card__arrow {
  background: #e7f0ff;
  color: #1d4ed8;
}

.action-card--green .action-card__arrow {
  background: #dcfce7;
  color: #059669;
}

.action-card--orange .action-card__arrow {
  background: #ffebe4;
  color: #f05a2a;
}

.empty-state {
  padding: 48rpx 24rpx;
  border-radius: 24rpx;
  background: #ffffff;
  text-align: center;
  color: #64748b;
  font-size: 28rpx;
}

.logout-link {
  width: fit-content;
  height: auto;
  line-height: 1.4;
  margin: 40rpx auto 0;
  padding: 0 24rpx;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 28rpx;
}

.logout-link::after {
  border: none;
}
</style>
