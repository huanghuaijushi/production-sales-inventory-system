<template>
  <NetworkBanner />
  <view class="container batch-page">
    <view class="filter-row">
      <view class="filter-row__tabs">
        <view
          v-for="tab in tabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ 'filter-tab--active': activeTab === tab.value }"
          @click="changeTab(tab.value)"
        >
          {{ tab.label }}
        </view>
      </view>
    </view>

    <view class="summary-bar">
      <text class="summary-bar__text">{{ summaryText }}</text>
    </view>

    <view v-if="loading && batches.length === 0" class="state-card">
      <text>正在加载批次...</text>
    </view>

    <view v-else-if="batches.length === 0" class="state-card">
      <text class="state-card__title">{{ emptyTitle }}</text>
      <text class="state-card__desc">{{ emptyDesc }}</text>
    </view>

    <view v-else class="batch-list">
      <view v-for="batch in batches" :key="batch.id" class="batch-card">
        <view class="batch-card__head">
          <view class="batch-card__title">
            <text class="batch-card__product">{{ batch.productName }}</text>
            <text class="batch-card__code">{{ batch.productCode }}</text>
          </view>
          <text class="batch-tag" :class="expiryTagClass(batch)">
            {{ expiryTagText(batch) }}
          </text>
        </view>

        <view class="batch-card__meta">
          <view class="meta-item">
            <text class="meta-item__label">批次号</text>
            <text class="meta-item__value">{{ batch.batchNo }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-item__label">可用</text>
            <text class="meta-item__value meta-item__value--strong">
              {{ batch.availableQuantity }}{{ batch.productUnit }}
            </text>
          </view>
        </view>

        <view class="batch-card__dates">
          <view class="meta-item">
            <text class="meta-item__label">生产日期</text>
            <text class="meta-item__value">{{ batch.productionDate || '-' }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-item__label">到期日期</text>
            <text class="meta-item__value">{{ batch.expiryDate || '-' }}</text>
          </view>
        </view>

        <view v-if="batch.remark" class="batch-card__remark">
          <text>{{ batch.remark }}</text>
        </view>
      </view>

      <button v-if="hasMore" class="load-more" :disabled="loadingMore" @click="loadMore">
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </button>
      <view v-else class="list-end">
        <text>已经到底了</text>
      </view>
    </view>

    <view v-if="errorMessage" class="card notice-card">
      <text class="notice-card__text">{{ errorMessage }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { inventoryApi, type StockBatch } from '@/api/inventory'
import NetworkBanner from '@/components/NetworkBanner.vue'

type TabValue = 'all' | 'expiring' | 'expired'

const tabs: Array<{ value: TabValue; label: string }> = [
  { value: 'all', label: '全部可用' },
  { value: 'expiring', label: '7 天内临期' },
  { value: 'expired', label: '已过期' }
]

const pageSize = 30
const activeTab = ref<TabValue>('all')
const batches = ref<StockBatch[]>([])
const totalElements = ref(0)
const currentPage = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const errorMessage = ref('')

const hasMore = computed(() => batches.value.length < totalElements.value)

const summaryText = computed(() => {
  if (loading.value && batches.value.length === 0) return '加载中...'
  const tabLabel = tabs.find((t) => t.value === activeTab.value)?.label || ''
  return `${tabLabel}：共 ${totalElements.value} 批`
})

const emptyTitle = computed(() => {
  if (activeTab.value === 'expiring') return '没有 7 天内临期的批次'
  if (activeTab.value === 'expired') return '没有已过期但仍有库存的批次'
  return '没有可用批次'
})

const emptyDesc = computed(() => {
  if (activeTab.value === 'expiring') return '库存暂时安全'
  if (activeTab.value === 'expired') return '所有批次都在保质期内'
  return '尝试切换上方筛选'
})

function changeTab(tab: TabValue) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  currentPage.value = 0
  batches.value = []
  totalElements.value = 0
  void load(true)
}

function tabParams() {
  switch (activeTab.value) {
    case 'expiring':
      return { expiringWithinDays: 7, availableOnly: true }
    case 'expired':
      return { expiringWithinDays: 0, availableOnly: true }
    default:
      return { availableOnly: true }
  }
}

async function load(reset: boolean) {
  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }
  errorMessage.value = ''

  try {
    const response = await inventoryApi.getAvailableBatches({
      page: reset ? 0 : currentPage.value + 1,
      size: pageSize,
      ...tabParams()
    })

    const list = response.content || []
    const filtered = filterByTab(list)

    if (reset) {
      batches.value = filtered
      currentPage.value = 0
    } else {
      batches.value = batches.value.concat(filtered)
      currentPage.value += 1
    }

    totalElements.value = response.totalElements || batches.value.length
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载批次失败'
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function filterByTab(list: StockBatch[]): StockBatch[] {
  // 已过期 tab 在后端拿了 expiringWithinDays=0 的范围（含已过期），
  // 但也会包含当天到期的，前端再精确过滤一次，只保留 expiryDate < today。
  if (activeTab.value !== 'expired') return list

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  return list.filter((batch) => {
    if (!batch.expiryDate) return false
    const expiry = new Date(batch.expiryDate)
    return expiry.getTime() < today.getTime()
  })
}

function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  void load(false)
}

function expiryTagClass(batch: StockBatch) {
  const status = computeExpiryStatus(batch.expiryDate)
  return `batch-tag--${status}`
}

function expiryTagText(batch: StockBatch) {
  if (!batch.expiryDate) return '无保质期'
  const days = daysUntilExpiry(batch.expiryDate)

  if (days === null) return '无保质期'
  if (days < 0) return `已过期 ${Math.abs(days)} 天`
  if (days === 0) return '今天到期'
  if (days <= 7) return `${days} 天后到期`
  return `还有 ${days} 天`
}

function computeExpiryStatus(expiryDate: string | null | undefined): 'expired' | 'urgent' | 'soon' | 'safe' | 'none' {
  if (!expiryDate) return 'none'
  const days = daysUntilExpiry(expiryDate)
  if (days === null) return 'none'
  if (days < 0) return 'expired'
  if (days <= 3) return 'urgent'
  if (days <= 7) return 'soon'
  return 'safe'
}

function daysUntilExpiry(expiryDate: string): number | null {
  const expiry = new Date(expiryDate)
  if (Number.isNaN(expiry.getTime())) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  expiry.setHours(0, 0, 0, 0)
  return Math.round((expiry.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
}

onShow(() => {
  void load(true)
})

onPullDownRefresh(async () => {
  try {
    await load(true)
  } finally {
    uni.stopPullDownRefresh()
  }
})
</script>

<style scoped lang="scss">
.batch-page {
  padding-bottom: 60rpx;
}

.filter-row {
  margin-bottom: 18rpx;
}

.filter-row__tabs {
  display: flex;
  gap: 14rpx;
  flex-wrap: wrap;
}

.filter-tab {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 26rpx;
  border-radius: 999rpx;
  background: #ffffff;
  border: 2rpx solid #dbe3f0;
  font-size: 24rpx;
  color: #475569;
}

.filter-tab--active {
  background: #be123c;
  color: #ffffff;
  border-color: transparent;
  font-weight: 700;
}

.summary-bar {
  margin-bottom: 18rpx;
  padding: 0 8rpx;
}

.summary-bar__text {
  font-size: 24rpx;
  color: #64748b;
}

.state-card {
  padding: 60rpx 24rpx;
  border-radius: 22rpx;
  background: #ffffff;
  text-align: center;
  color: #94a3b8;
  font-size: 26rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.state-card__title {
  font-size: 28rpx;
  font-weight: 700;
  color: #475569;
}

.state-card__desc {
  font-size: 24rpx;
  color: #94a3b8;
}

.batch-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.batch-card {
  padding: 24rpx;
  border-radius: 22rpx;
  background: #ffffff;
  border: 2rpx solid rgba(226, 232, 240, 0.6);
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.batch-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
}

.batch-card__title {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.batch-card__product {
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}

.batch-card__code {
  font-size: 22rpx;
  color: #94a3b8;
}

.batch-tag {
  flex: 0 0 auto;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.batch-tag--expired {
  background: #fee2e2;
  color: #b91c1c;
}

.batch-tag--urgent {
  background: #ffedd5;
  color: #c2410c;
}

.batch-tag--soon {
  background: #fef3c7;
  color: #b45309;
}

.batch-tag--safe {
  background: #dcfce7;
  color: #166534;
}

.batch-tag--none {
  background: #f1f5f9;
  color: #64748b;
}

.batch-card__meta,
.batch-card__dates {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.meta-item__label {
  font-size: 22rpx;
  color: #94a3b8;
}

.meta-item__value {
  font-size: 26rpx;
  color: #0f172a;
}

.meta-item__value--strong {
  font-size: 28rpx;
  font-weight: 800;
  color: #be123c;
}

.batch-card__remark {
  padding-top: 12rpx;
  border-top: 2rpx solid #f1f5f9;
  font-size: 24rpx;
  color: #64748b;
}

.load-more {
  margin-top: 10rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 18rpx;
  background: #ffffff;
  border: 2rpx solid #dbe3f0;
  color: #be123c;
  font-size: 26rpx;
}

.list-end {
  text-align: center;
  color: #94a3b8;
  font-size: 24rpx;
  padding: 24rpx 0;
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
