<template>
  <NetworkBanner />
  <view class="container records-page">
    <view class="search-row">
      <view class="search-box">
        <text class="search-box__icon">⌕</text>
        <input
          v-model.trim="keyword"
          class="search-box__input"
          placeholder="按商品名 / 批次号 / 单号搜索"
          confirm-type="search"
          @confirm="reload"
        />
        <text v-if="keyword" class="search-box__clear" @click="clearKeyword">×</text>
      </view>
    </view>

    <view class="summary-bar">
      <text class="summary-bar__text">共 {{ totalElements }} 条流水</text>
    </view>

    <view v-if="loading && records.length === 0" class="state-card">
      <text>正在加载流水...</text>
    </view>

    <view v-else-if="records.length === 0" class="state-card">
      <text class="state-card__title">没有库存流水</text>
      <text class="state-card__desc">{{ keyword ? '换个关键词试试' : '今天还没人动过货' }}</text>
    </view>

    <view v-else class="record-list">
      <view v-for="record in records" :key="record.id" class="record-card">
        <view class="record-card__time-col">
          <text class="record-card__date">{{ formatDate(record.createdAt) }}</text>
          <text class="record-card__time">{{ formatTime(record.createdAt) }}</text>
          <view class="record-tag" :class="toneClass(record.type)">
            {{ typeText(record) }}
          </view>
        </view>

        <view class="record-card__body">
          <view class="record-card__title-row">
            <text class="record-card__product">{{ record.productName || '未知商品' }}</text>
            <text class="record-card__qty" :class="qtyClass(record.type)">
              {{ formatQuantitySign(record) }}{{ record.productUnit || '' }}
            </text>
          </view>

          <view class="record-card__meta">
            <text v-if="record.batchNo">批次 {{ record.batchNo }}</text>
            <text>{{ subTypeText(record.subType) }}</text>
            <text>{{ record.operatorName }}</text>
          </view>

          <view v-if="record.remark" class="record-card__remark">
            <text>{{ record.remark }}</text>
          </view>
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
import { inventoryApi, type StockRecordResponse } from '@/api/inventory'
import NetworkBanner from '@/components/NetworkBanner.vue'

const pageSize = 20
const records = ref<StockRecordResponse[]>([])
const totalElements = ref(0)
const currentPage = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const errorMessage = ref('')
const keyword = ref('')

const hasMore = computed(() => records.value.length < totalElements.value)

const subTypeLabels: Record<string, string> = {
  PURCHASE: '采购入库',
  PRODUCTION: '生产入库',
  SALES: '销售出库',
  PRODUCTION_USAGE: '生产领料',
  INTERNAL_USAGE: '内部使用',
  OTHER_OUTBOUND: '其他出库',
  PRODUCTION_LOSS: '生产报损',
  PACKAGING_LOSS: '包装损坏',
  SHIPPING_LOSS: '运输损耗',
  EXPIRED_LOSS: '临期报损',
  DAMAGE_LOSS: '破损报损',
  OTHER_LOSS: '其他报损',
  INVENTORY: '盘点调整',
  INVENTORY_GAIN: '盘盈',
  INVENTORY_LOSS: '盘亏'
}

function reload() {
  currentPage.value = 0
  records.value = []
  totalElements.value = 0
  void load(true)
}

function clearKeyword() {
  keyword.value = ''
  reload()
}

async function load(reset: boolean) {
  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }
  errorMessage.value = ''

  try {
    const nextPage = reset ? 0 : currentPage.value + 1
    const response = await inventoryApi.getStockRecords({
      page: nextPage,
      size: pageSize,
      query: keyword.value || undefined
    })

    const list = response.content || []

    if (reset) {
      records.value = list
      currentPage.value = 0
    } else {
      records.value = records.value.concat(list)
      currentPage.value = nextPage
    }

    totalElements.value = response.totalElements || records.value.length
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载流水失败'
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  void load(false)
}

function formatDate(iso: string) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso.slice(0, 10)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${m}-${day}`
}

function formatTime(iso: string) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

function typeText(record: StockRecordResponse) {
  if (record.type === 'IN') return '入'
  if (record.type === 'OUT') {
    if (record.subType?.endsWith('_LOSS')) return '损'
    if (record.subType?.startsWith('INVENTORY')) return '盘'
    return '出'
  }
  return '调'
}

function toneClass(type: string) {
  switch (type) {
    case 'IN':
      return 'record-tag--green'
    case 'OUT':
      return 'record-tag--blue'
    case 'ADJUST':
      return 'record-tag--purple'
    default:
      return 'record-tag--neutral'
  }
}

function qtyClass(type: string) {
  if (type === 'IN') return 'record-card__qty--positive'
  if (type === 'OUT') return 'record-card__qty--negative'
  return 'record-card__qty--neutral'
}

function formatQuantitySign(record: StockRecordResponse) {
  const qty = Math.abs(record.quantity)
  if (record.type === 'IN') return `+${qty}`
  if (record.type === 'OUT') return `-${qty}`
  return record.quantity > 0 ? `+${qty}` : `-${qty}`
}

function subTypeText(subType: string) {
  return subTypeLabels[subType] || subType || ''
}

onShow(() => {
  reload()
})

onPullDownRefresh(async () => {
  currentPage.value = 0
  records.value = []
  totalElements.value = 0
  try {
    await load(true)
  } finally {
    uni.stopPullDownRefresh()
  }
})
</script>

<style scoped lang="scss">
.records-page {
  padding-bottom: 60rpx;
}

.search-row {
  margin-bottom: 18rpx;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 18rpx;
  height: 88rpx;
  padding: 0 24rpx;
  border-radius: 22rpx;
  background: #ffffff;
  border: 2rpx solid #dbe3f0;
}

.search-box__icon {
  color: #64748b;
  font-size: 32rpx;
  line-height: 1;
}

.search-box__input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
}

.search-box__clear {
  font-size: 36rpx;
  color: #94a3b8;
  padding: 0 12rpx;
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

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-card {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  border-radius: 20rpx;
  background: #ffffff;
  border: 2rpx solid rgba(226, 232, 240, 0.6);
}

.record-card__time-col {
  flex: 0 0 96rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  padding-right: 14rpx;
  border-right: 2rpx solid #f1f5f9;
}

.record-card__date {
  font-size: 24rpx;
  font-weight: 700;
  color: #0f172a;
}

.record-card__time {
  font-size: 22rpx;
  color: #94a3b8;
}

.record-tag {
  margin-top: 6rpx;
  width: 48rpx;
  height: 48rpx;
  line-height: 48rpx;
  text-align: center;
  border-radius: 50%;
  font-size: 24rpx;
  font-weight: 800;
}

.record-tag--green {
  background: #dcfce7;
  color: #166534;
}

.record-tag--blue {
  background: #dbeafe;
  color: #1d4ed8;
}

.record-tag--purple {
  background: #ede9fe;
  color: #6d28d9;
}

.record-tag--neutral {
  background: #f1f5f9;
  color: #475569;
}

.record-card__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.record-card__title-row {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  align-items: baseline;
}

.record-card__product {
  flex: 1;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.record-card__qty {
  font-size: 30rpx;
  font-weight: 800;
}

.record-card__qty--positive {
  color: #059669;
}

.record-card__qty--negative {
  color: #b91c1c;
}

.record-card__qty--neutral {
  color: #0f172a;
}

.record-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  font-size: 22rpx;
  color: #64748b;
}

.record-card__remark {
  padding-top: 8rpx;
  border-top: 2rpx dashed #f1f5f9;
  font-size: 24rpx;
  color: #475569;
}

.load-more {
  margin-top: 10rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 18rpx;
  background: #ffffff;
  border: 2rpx solid #dbe3f0;
  color: #0e7490;
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
