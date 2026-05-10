<template>
  <view class="container inventory-page">
    <view class="search-panel">
      <view class="search-box">
        <text class="search-box__icon">⌕</text>
        <input
          v-model.trim="filters.query"
          class="search-box__input"
          placeholder="输入物料名称、编码、分类或规格"
          confirm-type="search"
          @confirm="searchStocks"
        />
        <button v-if="filters.query" class="search-box__clear" @click="clearKeyword">×</button>
      </view>
      <button class="search-button" :disabled="loading" @click="searchStocks">查询</button>
    </view>

    <scroll-view class="status-tabs" scroll-x :show-scrollbar="false">
      <view class="status-tabs__inner">
        <button
          v-for="item in statusOptions"
          :key="item.value"
          :class="['status-tab', filters.status === item.value ? 'status-tab--active' : '']"
          @click="changeStatus(item.value)"
        >
          {{ item.label }}
        </button>
      </view>
    </scroll-view>

    <view class="result-summary">
      <text class="result-summary__title">库存结果</text>
      <text class="result-summary__count">共 {{ totalElements }} 条</text>
    </view>

    <view v-if="loading && !stocks.length" class="loading-state">
      <text>正在查询库存...</text>
    </view>

    <view v-else-if="!stocks.length" class="empty-state">
      <text class="empty-state__title">没有找到库存</text>
      <text class="empty-state__desc">换个关键词或状态再试试</text>
    </view>

    <view v-else class="stock-list">
      <view v-for="stock in stocks" :key="stock.productId" class="stock-card">
        <view class="stock-card__top">
          <view class="stock-card__identity">
            <text class="stock-card__name">{{ stock.productName }}</text>
            <text class="stock-card__code">{{ stock.productCode }}</text>
          </view>
          <text :class="['stock-status', statusClass(stock)]">{{ statusText(stock) }}</text>
        </view>

        <view class="stock-card__tags">
          <text class="stock-tag">{{ productTypeText(stock.productType) }}</text>
          <text v-if="stock.category" class="stock-tag">{{ stock.category }}</text>
          <text v-if="stock.specification" class="stock-tag">{{ stock.specification }}</text>
        </view>

        <view class="quantity-main">
          <view>
            <text class="quantity-main__label">当前库存</text>
            <text :class="['quantity-main__value', stock.quantity === 0 ? 'quantity-main__value--danger' : '']">
              {{ formatQuantity(stock.quantity, stock.unit) }}
            </text>
          </view>
          <view class="quantity-main__available">
            <text class="quantity-main__label">可用</text>
            <text class="quantity-main__subvalue">{{ formatQuantity(stock.availableQuantity, stock.unit) }}</text>
          </view>
        </view>

        <view class="stock-metrics">
          <view class="metric-item">
            <text class="metric-item__label">锁定</text>
            <text class="metric-item__value">{{ formatQuantity(stock.lockedQuantity, stock.unit) }}</text>
          </view>
          <view class="metric-item">
            <text class="metric-item__label">预警线</text>
            <text class="metric-item__value">{{ formatQuantity(stock.alertQuantity, stock.unit) }}</text>
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
  </view>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { inventoryApi, type StockItem } from '@/api/inventory'

const pageSize = 12

const loading = ref(false)
const loadingMore = ref(false)
const page = ref(0)
const totalElements = ref(0)
const totalPages = ref(0)
const stocks = ref<StockItem[]>([])
const filters = reactive({
  query: '',
  category: 'all',
  status: 'all'
})

const statusOptions = [
  { label: '全部', value: 'all' },
  { label: '正常', value: 'normal' },
  { label: '低库存', value: 'warning' },
  { label: '缺货', value: 'out-of-stock' }
]

const hasMore = ref(false)

async function loadStocks(nextPage = 0, append = false) {
  if (append) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  try {
    const response = await inventoryApi.getAllStocks(nextPage, pageSize, filters)
    const content = response.content || []

    stocks.value = append ? [...stocks.value, ...content] : content
    page.value = response.number
    totalElements.value = response.totalElements
    totalPages.value = response.totalPages
    hasMore.value = response.number + 1 < response.totalPages
  } catch (error) {
    const message = error instanceof Error ? error.message : '库存查询失败'
    uni.showToast({ title: message, icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function searchStocks() {
  loadStocks(0)
}

function clearKeyword() {
  filters.query = ''
  loadStocks(0)
}

function changeStatus(status: string) {
  if (filters.status === status) return

  filters.status = status
  loadStocks(0)
}

function loadMore() {
  if (!hasMore.value || loadingMore.value) return

  loadStocks(page.value + 1, true)
}

function statusText(stock: StockItem) {
  if (stock.quantity === 0) return '缺货'
  return stock.isLowStock ? '低库存' : '正常'
}

function statusClass(stock: StockItem) {
  if (stock.quantity === 0) return 'stock-status--danger'
  return stock.isLowStock ? 'stock-status--warning' : 'stock-status--normal'
}

function productTypeText(type: StockItem['productType']) {
  return type === 'RAW_MATERIAL' ? '原料' : '成品'
}

function formatQuantity(value: number, unit: string) {
  return `${value}${unit || ''}`
}

onMounted(() => {
  loadStocks()
})
</script>

<style scoped lang="scss">
.inventory-page {
  min-height: 100vh;
  padding: 24rpx 28rpx 40rpx;
  background: #f4f7fb;
}

.search-panel {
  position: sticky;
  top: 0;
  z-index: 2;
  display: flex;
  gap: 16rpx;
  padding: 18rpx 0 20rpx;
  background: #f4f7fb;
}

.search-box {
  flex: 1;
  height: 88rpx;
  padding: 0 18rpx 0 24rpx;
  display: flex;
  align-items: center;
  gap: 14rpx;
  border: 2rpx solid #dbe5f2;
  border-radius: 22rpx;
  background: #ffffff;
}

.search-box__icon {
  font-size: 34rpx;
  color: #64748b;
}

.search-box__input {
  flex: 1;
  height: 100%;
  min-width: 0;
  font-size: 28rpx;
}

.search-box__clear {
  width: 48rpx;
  height: 48rpx;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: #eef2f7;
  color: #64748b;
  font-size: 34rpx;
  line-height: 45rpx;
}

.search-box__clear::after,
.search-button::after,
.status-tab::after,
.load-more::after {
  border: none;
}

.search-button {
  width: 116rpx;
  height: 88rpx;
  padding: 0;
  border: none;
  border-radius: 22rpx;
  background: #2563eb;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 700;
  line-height: 88rpx;
}

.status-tabs {
  width: 100%;
  margin-bottom: 24rpx;
  white-space: nowrap;
}

.status-tabs__inner {
  display: inline-flex;
  gap: 14rpx;
  padding-right: 28rpx;
}

.status-tab {
  height: 64rpx;
  padding: 0 26rpx;
  border: none;
  border-radius: 999rpx;
  background: #ffffff;
  color: #64748b;
  font-size: 26rpx;
  line-height: 64rpx;
}

.status-tab--active {
  background: #1d4ed8;
  color: #ffffff;
  font-weight: 700;
}

.result-summary {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.result-summary__title {
  font-size: 36rpx;
  font-weight: 800;
  color: #0f172a;
}

.result-summary__count {
  font-size: 25rpx;
  color: #64748b;
}

.stock-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.stock-card {
  padding: 26rpx;
  border: 2rpx solid rgba(226, 232, 240, 0.85);
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 34rpx rgba(15, 23, 42, 0.06);
}

.stock-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
  margin-bottom: 18rpx;
}

.stock-card__identity {
  flex: 1;
  min-width: 0;
}

.stock-card__name {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  line-height: 1.28;
  color: #0f172a;
}

.stock-card__code {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
}

.stock-status {
  flex: 0 0 auto;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 23rpx;
  font-weight: 800;
}

.stock-status--normal {
  background: #e8f1ff;
  color: #1d4ed8;
}

.stock-status--warning {
  background: #fff7ed;
  color: #c2410c;
}

.stock-status--danger {
  background: #fee2e2;
  color: #dc2626;
}

.stock-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 22rpx;
}

.stock-tag {
  max-width: 100%;
  padding: 7rpx 14rpx;
  border-radius: 999rpx;
  background: #f1f5f9;
  color: #475569;
  font-size: 23rpx;
  line-height: 1.3;
}

.quantity-main {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  padding: 22rpx;
  border-radius: 20rpx;
  background: #f8fafc;
  margin-bottom: 16rpx;
}

.quantity-main__label {
  display: block;
  margin-bottom: 8rpx;
  font-size: 23rpx;
  color: #64748b;
}

.quantity-main__value {
  font-size: 46rpx;
  font-weight: 900;
  line-height: 1.1;
  color: #0f172a;
}

.quantity-main__value--danger {
  color: #dc2626;
}

.quantity-main__available {
  min-width: 160rpx;
  text-align: right;
}

.quantity-main__subvalue {
  font-size: 32rpx;
  font-weight: 800;
  color: #059669;
}

.stock-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
}

.metric-item {
  padding: 18rpx;
  border-radius: 18rpx;
  background: #f8fafc;
}

.metric-item__label {
  display: block;
  margin-bottom: 8rpx;
  font-size: 22rpx;
  color: #64748b;
}

.metric-item__value {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.load-more {
  height: 82rpx;
  margin-top: 8rpx;
  border: none;
  border-radius: 22rpx;
  background: #e8f1ff;
  color: #1d4ed8;
  font-size: 28rpx;
  font-weight: 700;
  line-height: 82rpx;
}

.list-end,
.loading-state,
.empty-state {
  padding: 48rpx 24rpx;
  text-align: center;
  color: #64748b;
  font-size: 26rpx;
}

.empty-state {
  margin-top: 30rpx;
  border-radius: 24rpx;
  background: #ffffff;
}

.empty-state__title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}

.empty-state__desc {
  display: block;
  margin-top: 12rpx;
  font-size: 25rpx;
  color: #94a3b8;
}
</style>
