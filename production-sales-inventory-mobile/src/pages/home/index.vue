<template>
  <NetworkBanner />
  <view class="container home-page">
    <view class="topbar">
      <view class="topbar__identity">
        <text class="topbar__dot">●</text>
        <text class="topbar__name">{{ authStore.displayName }}</text>
      </view>
      <view class="topbar__settings" @click="openSettings">
        <text class="topbar__gear">⚙</text>
      </view>
    </view>

    <view class="metrics">
      <view class="metric-card card" :class="metricToneClass('inbound')">
        <text class="metric-card__label">今日入库</text>
        <text class="metric-card__value">{{ formatNumber(dashboard?.today?.inboundQuantity) }}</text>
        <text class="metric-card__sub">{{ formatRecordCount(dashboard?.today?.inboundRecordCount) }}</text>
      </view>
      <view class="metric-card card" :class="metricToneClass('outbound')">
        <text class="metric-card__label">今日出库</text>
        <text class="metric-card__value">{{ formatNumber(dashboard?.today?.outboundQuantity) }}</text>
        <text class="metric-card__sub">{{ formatRecordCount(dashboard?.today?.outboundRecordCount) }}</text>
      </view>
      <view class="metric-card card" :class="metricToneClass('low')">
        <text class="metric-card__label">低库存</text>
        <text class="metric-card__value">{{ formatNumber(dashboard?.lowStockCount) }}</text>
        <text class="metric-card__sub">商品</text>
      </view>
      <view class="metric-card card" :class="metricToneClass('out')">
        <text class="metric-card__label">缺货</text>
        <text class="metric-card__value">{{ formatNumber(dashboard?.outOfStockCount) }}</text>
        <text class="metric-card__sub">商品</text>
      </view>
    </view>

    <view class="card alert-card">
      <view class="alert-card__head">
        <text class="alert-card__title">需要关注</text>
        <text class="alert-card__hint">{{ alertHint }}</text>
      </view>

      <view v-if="loadingDashboard && alertItems.length === 0" class="alert-card__empty">
        <text>正在加载库存风险...</text>
      </view>

      <view v-else-if="alertItems.length === 0" class="alert-card__empty">
        <text>暂无库存风险</text>
      </view>

      <view v-else class="alert-list">
        <view v-for="item in alertItems" :key="item.key" class="alert-item">
          <text class="alert-item__dot" :class="`alert-item__dot--${item.tone}`">●</text>
          <view class="alert-item__body">
            <text class="alert-item__name">{{ item.name }}</text>
            <text class="alert-item__desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section-title">
      <text class="section-title__main">常用</text>
    </view>

    <view class="actions">
      <view
        v-for="item in visibleShortcuts"
        :key="item.path"
        class="action-card"
        :class="`action-card--${item.tone}`"
        @click="navigate(item.path)"
      >
        <image class="action-card__icon" :src="item.icon" mode="aspectFit" />
        <text class="action-card__title">{{ item.title }}</text>
      </view>
    </view>

    <view v-if="!visibleShortcuts.length" class="empty-state">
      <text>当前账号暂无移动端功能权限</text>
    </view>

    <view v-if="errorMessage" class="card notice-card">
      <text class="notice-card__text">{{ errorMessage }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { inventoryApi, type DashboardWarningItem, type ExpiringBatchItem, type MobileHome, type StockItem } from '@/api/inventory'
import NetworkBanner from '@/components/NetworkBanner.vue'

const authStore = useAuthStore()

type ShortcutTone = 'blue' | 'green' | 'orange' | 'purple' | 'rose' | 'cyan'

interface ShortcutItem {
  title: string
  path: string
  icon: string
  tone: ShortcutTone
  permissions: string[]
}

interface AlertItem {
  key: string
  name: string
  desc: string
  tone: 'red' | 'orange' | 'amber'
}

function lucideIcon(color: string, paths: string) {
  return svgToDataUri(`
    <svg viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg">
      ${paths}
    </svg>
  `)
}

const shortcuts: ShortcutItem[] = [
  {
    title: '入库',
    path: '/pages/stock-in/index',
    icon: lucideIcon('#059669', `
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
      <path d="m7 10 5 5 5-5"/>
      <path d="M12 15V3"/>
    `),
    tone: 'green',
    permissions: ['stock:in', 'inbound:create', 'mobile:stock-in']
  },
  {
    title: '出库',
    path: '/pages/stock-out/index',
    icon: lucideIcon('#1d4ed8', `
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
      <path d="m17 8-5-5-5 5"/>
      <path d="M12 3v12"/>
    `),
    tone: 'blue',
    permissions: ['stock:out', 'outbound:create', 'mobile:stock-out']
  },
  {
    title: '盘点',
    path: '/pages/check/index',
    icon: lucideIcon('#6d28d9', `
      <rect width="8" height="4" x="8" y="2" rx="1"/>
      <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/>
      <path d="m9 14 2 2 4-4"/>
    `),
    tone: 'purple',
    permissions: ['inventory:check', 'stock:check', 'mobile:check']
  },
  {
    title: '报损',
    path: '/pages/loss/index',
    icon: lucideIcon('#c2410c', `
      <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
      <path d="M12 9v4"/>
      <path d="M12 17h.01"/>
    `),
    tone: 'orange',
    permissions: ['inventory:loss', 'stock:loss', 'mobile:loss']
  },
  {
    title: '查批次',
    path: '/pages/batch/index',
    icon: lucideIcon('#be123c', `
      <rect width="20" height="5" x="2" y="3" rx="1"/>
      <path d="M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8"/>
      <path d="M10 12h4"/>
    `),
    tone: 'rose',
    permissions: ['inventory:view', 'stock:view', 'mobile:inventory']
  },
  {
    title: '查流水',
    path: '/pages/records/index',
    icon: lucideIcon('#0891b2', `
      <path d="M3 12a9 9 0 1 0 9-9 9.74 9.74 0 0 0-6.74 2.74L3 8"/>
      <path d="M3 3v5h5"/>
      <path d="M12 7v5l4 2"/>
    `),
    tone: 'cyan',
    permissions: ['inventory:view', 'stock:view', 'mobile:inventory']
  }
]

const dashboard = ref<MobileHome | null>(null)
const loadingDashboard = ref(false)
const errorMessage = ref('')

const visibleShortcuts = computed(() => {
  const permissions = authStore.sysUser?.permissions || []
  const roles = authStore.sysUser?.roles || []

  if (roles.some((role) => ['ADMIN', 'SUPER_ADMIN'].includes(role))) {
    return shortcuts
  }

  if (!permissions.length) {
    return shortcuts
  }

  const matched = shortcuts.filter((shortcut) =>
    shortcut.permissions.some((permission) => permissions.includes(permission))
  )

  return matched.length ? matched : shortcuts
})

const alertItems = computed<AlertItem[]>(() => {
  const items: AlertItem[] = []

  const lowStockItems = dashboard.value?.lowStockItems || []
  lowStockItems.slice(0, 3).forEach((stock: StockItem) => {
    const isOut = stock.availableQuantity === 0
    items.push({
      key: `low-${stock.productId}`,
      name: stock.productName,
      desc: isOut ? '已缺货' : `仅剩 ${stock.availableQuantity}${stock.unit}（预警线 ${stock.alertQuantity}${stock.unit}）`,
      tone: isOut ? 'red' : 'orange'
    })
  })

  const expiringBatches = dashboard.value?.expiringBatches || []
  expiringBatches.slice(0, 3).forEach((batch: ExpiringBatchItem) => {
    const days = batch.daysToExpire
    let desc: string
    let tone: AlertItem['tone'] = 'amber'

    if (days === null || days === undefined) {
      desc = `批次 ${batch.batchNo} · 可用 ${batch.availableQuantity}${batch.productUnit}`
    } else if (days < 0) {
      desc = `批次 ${batch.batchNo} 已过期 ${Math.abs(days)} 天，剩 ${batch.availableQuantity}${batch.productUnit}`
      tone = 'red'
    } else if (days === 0) {
      desc = `批次 ${batch.batchNo} 今天到期，剩 ${batch.availableQuantity}${batch.productUnit}`
      tone = 'red'
    } else if (days <= 3) {
      desc = `批次 ${batch.batchNo} · ${days} 天后到期，剩 ${batch.availableQuantity}${batch.productUnit}`
      tone = 'orange'
    } else {
      desc = `批次 ${batch.batchNo} · ${days} 天后到期，剩 ${batch.availableQuantity}${batch.productUnit}`
    }

    items.push({
      key: `exp-${batch.batchId}`,
      name: `[临期] ${batch.productName}`,
      desc,
      tone
    })
  })

  const warnings = dashboard.value?.rawMaterialWarnings || []
  warnings.slice(0, 3).forEach((warning: DashboardWarningItem, index: number) => {
    items.push({
      key: `warn-${index}-${warning.name}`,
      name: warning.name,
      desc: `${warning.current} / 安全线 ${warning.safety} · ${warning.status}`,
      tone: warning.statusClass?.includes('danger') ? 'red' : 'amber'
    })
  })

  return items
})

const alertHint = computed(() => {
  if (loadingDashboard.value && !dashboard.value) return '加载中'
  if (alertItems.value.length === 0) return ''
  return `${alertItems.value.length} 项`
})

function metricToneClass(kind: 'inbound' | 'outbound' | 'low' | 'out') {
  switch (kind) {
    case 'inbound':
      return 'metric-card--green'
    case 'outbound':
      return 'metric-card--blue'
    case 'low':
      return dashboard.value && dashboard.value.lowStockCount > 0
        ? 'metric-card--amber'
        : 'metric-card--neutral'
    case 'out':
      return dashboard.value && dashboard.value.outOfStockCount > 0
        ? 'metric-card--red'
        : 'metric-card--neutral'
  }
}

function formatNumber(value: number | undefined | null) {
  if (value === null || value === undefined) return '-'
  if (!Number.isFinite(value)) return '-'
  return String(value)
}

function formatRecordCount(value: number | undefined | null) {
  if (value === null || value === undefined) return '-'
  return `${value} 单`
}

async function loadDashboard() {
  loadingDashboard.value = true
  errorMessage.value = ''

  try {
    dashboard.value = await inventoryApi.getMobileHome()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载首页数据失败'
  } finally {
    loadingDashboard.value = false
  }
}

onShow(() => {
  if (!authStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }

  void loadDashboard()
})

onPullDownRefresh(async () => {
  try {
    await loadDashboard()
  } finally {
    uni.stopPullDownRefresh()
  }
})

function navigate(path: string) {
  uni.navigateTo({ url: path })
}

function openSettings() {
  uni.showActionSheet({
    itemList: ['退出登录'],
    success: (res) => {
      if (res.tapIndex === 0) {
        void handleLogout()
      }
    }
  })
}

async function handleLogout() {
  await authStore.logout()
  uni.showToast({ title: '已退出登录', icon: 'none' })
  uni.reLaunch({ url: '/pages/login/index' })
}

function svgToDataUri(svg: string) {
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg.trim())}`
}
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  padding: calc(env(safe-area-inset-top) + 20rpx) 28rpx 48rpx;
  background: #f5f7fb;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56rpx;
  margin-bottom: 28rpx;
  padding: 0 4rpx;
}

.topbar__identity {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.topbar__dot {
  font-size: 16rpx;
  line-height: 1;
  color: #22c55e;
}

.topbar__name {
  font-size: 28rpx;
  font-weight: 600;
  color: #334155;
  letter-spacing: 0.02em;
}

.topbar__settings {
  width: 80rpx;
  height: 80rpx;
  margin-right: -16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: transparent;
}

.topbar__settings:active {
  background: rgba(15, 23, 42, 0.06);
}

.topbar__gear {
  font-size: 36rpx;
  line-height: 1;
  color: #64748b;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
  margin-bottom: 24rpx;
}

.metric-card {
  padding: 24rpx 22rpx;
  border-radius: 22rpx;
  background: #ffffff;
  border: 2rpx solid rgba(226, 232, 240, 0.6);
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.metric-card__label {
  font-size: 24rpx;
  color: #64748b;
}

.metric-card__value {
  font-size: 56rpx;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.1;
}

.metric-card__sub {
  font-size: 22rpx;
  color: #94a3b8;
}

.metric-card--green {
  background: #ecfdf5;
  border-color: rgba(16, 185, 129, 0.24);
}

.metric-card--green .metric-card__value {
  color: #047857;
}

.metric-card--blue {
  background: #eff6ff;
  border-color: rgba(37, 99, 235, 0.24);
}

.metric-card--blue .metric-card__value {
  color: #1d4ed8;
}

.metric-card--amber {
  background: #fffbeb;
  border-color: rgba(217, 119, 6, 0.28);
}

.metric-card--amber .metric-card__value {
  color: #b45309;
}

.metric-card--red {
  background: #fef2f2;
  border-color: rgba(220, 38, 38, 0.28);
}

.metric-card--red .metric-card__value {
  color: #b91c1c;
}

.metric-card--neutral .metric-card__value {
  color: #0f172a;
}

.alert-card {
  margin-bottom: 28rpx;
  padding: 24rpx;
}

.alert-card__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.alert-card__title {
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}

.alert-card__hint {
  font-size: 22rpx;
  color: #94a3b8;
}

.alert-card__empty {
  padding: 24rpx 0;
  text-align: center;
  color: #94a3b8;
  font-size: 26rpx;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 14rpx;
}

.alert-item__dot {
  font-size: 22rpx;
  line-height: 1.5;
  flex: 0 0 18rpx;
}

.alert-item__dot--red {
  color: #dc2626;
}

.alert-item__dot--orange {
  color: #ea580c;
}

.alert-item__dot--amber {
  color: #d97706;
}

.alert-item__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.alert-item__name {
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.alert-item__desc {
  font-size: 24rpx;
  color: #64748b;
}

.section-title {
  display: flex;
  align-items: baseline;
  margin-bottom: 18rpx;
  padding: 0 8rpx;
}

.section-title__main {
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}

.actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.action-card {
  min-height: 168rpx;
  padding: 24rpx 26rpx;
  border-radius: 22rpx;
  background: #ffffff;
  border: 2rpx solid rgba(226, 232, 240, 0.6);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.action-card:active {
  transform: scale(0.97);
  box-shadow: 0 4rpx 16rpx rgba(15, 23, 42, 0.08);
}

.action-card__icon {
  width: 64rpx;
  height: 64rpx;
}

.action-card__title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 0.02em;
}

.action-card--green {
  background: #f0fdf4;
  border-color: rgba(16, 185, 129, 0.2);
}

.action-card--blue {
  background: #eff6ff;
  border-color: rgba(37, 99, 235, 0.2);
}

.action-card--purple {
  background: #f5f3ff;
  border-color: rgba(124, 58, 237, 0.2);
}

.action-card--orange {
  background: #fff7ed;
  border-color: rgba(234, 88, 12, 0.2);
}

.action-card--rose {
  background: #fff1f2;
  border-color: rgba(190, 18, 60, 0.2);
}

.action-card--cyan {
  background: #ecfeff;
  border-color: rgba(8, 145, 178, 0.2);
}

.empty-state {
  padding: 48rpx 24rpx;
  border-radius: 24rpx;
  background: #ffffff;
  text-align: center;
  color: #64748b;
  font-size: 28rpx;
}

.notice-card {
  margin-top: 24rpx;
  background: #fff7ed;
  border: 2rpx solid #fdba74;
}

.notice-card__text {
  font-size: 26rpx;
  color: #c2410c;
}
</style>
