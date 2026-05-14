<template>
  <NetworkBanner />
  <view class="container stock-out-page">
    <view class="summary-row">
      <view class="summary-card card">
        <text class="summary-card__label">当前状态</text>
        <text class="summary-card__value">{{ selectedStock ? '已选择商品' : '未选择商品' }}</text>
      </view>
      <view class="summary-card card">
        <text class="summary-card__label">可用库存</text>
        <text class="summary-card__value">{{ selectedStock ? `${selectedStock.availableQuantity}${selectedStock.unit}` : '-' }}</text>
      </view>
    </view>

    <view class="card section-card">
      <view class="section-head">
        <text class="section-title">{{ productPicking ? '1. 选择商品' : '已选商品' }}</text>
        <text v-if="!productPicking" class="section-link" @click="startPicking">更换</text>
        <text v-else class="section-tip">先选商品，再选择批次</text>
      </view>

      <view v-if="productPicking">
        <view class="search-box">
          <text class="search-box__icon">⌕</text>
          <input
            v-model.trim="searchKeyword"
            class="search-box__input"
            placeholder="按名称或编码搜索"
            confirm-type="search"
          />
        </view>

        <view class="stock-list">
          <view
            v-for="stock in filteredStocks"
            :key="stock.productId"
            class="stock-item"
            :class="{ 'stock-item--active': stock.productId === form.productId }"
            @click="selectStock(stock.productId)"
          >
            <view class="stock-item__main">
              <text class="stock-item__name">{{ stock.productName }}</text>
              <text class="stock-item__code">{{ stock.productCode }}</text>
            </view>
            <view class="stock-item__meta">
              <text>当前 {{ stock.quantity }}{{ stock.unit }}</text>
              <text>可用 {{ stock.availableQuantity }}{{ stock.unit }}</text>
            </view>
          </view>

          <view v-if="!loadingStocks && filteredStocks.length === 0" class="empty-state">
            <text class="empty-state__title">没有找到匹配商品</text>
            <text class="empty-state__desc">请尝试使用名称或编码搜索</text>
          </view>
        </view>
      </view>

      <view v-else-if="selectedStock" class="selected-product">
        <text class="selected-product__name">{{ selectedStock.productName }}</text>
        <text class="selected-product__code">编码 {{ selectedStock.productCode }}</text>
        <view class="selected-product__meta">
          <text>当前 {{ selectedStock.quantity }}{{ selectedStock.unit }}</text>
          <text>可用 {{ selectedStock.availableQuantity }}{{ selectedStock.unit }}</text>
        </view>
      </view>
    </view>

    <view class="card section-card">
      <view class="section-head">
        <text class="section-title">2. 填写出库信息</text>
        <text class="section-tip">带 * 为必填项</text>
      </view>

      <view class="field-group">
        <text class="field-label">操作类型 *</text>
        <view class="segmented">
          <view
            v-for="option in operationOptions"
            :key="option.value"
            class="segmented__item"
            :class="{ 'segmented__item--active': form.subType === option.value }"
            @click="form.subType = option.value"
          >
            {{ option.label }}
          </view>
        </view>
      </view>

      <view class="field-group" v-if="isSalesOutbound">
        <text class="field-label">本次销售单价</text>
        <input
          v-model.number="form.businessUnitPrice"
          class="form-input mobile-input"
          type="digit"
          placeholder="不填则使用建议销售单价"
        />
      </view>

      <view class="field-group">
        <text class="field-label">数量 *</text>
        <input
          v-model.number="form.quantity"
          class="form-input mobile-input"
          type="digit"
          placeholder="请输入出库数量"
        />
      </view>

      <view class="field-group">
        <text class="field-label">出库批次 *</text>
        <picker :disabled="!form.productId || batchesLoading || filteredBatches.length === 0" :range="filteredBatches" range-key="displayName" @change="onBatchChange">
          <view class="picker-display">{{ batchDisplayText }}</view>
        </picker>
      </view>

      <view class="field-group">
        <text class="field-label">用途 / 备注</text>
        <textarea
          v-model.trim="form.remark"
          class="remark-input"
          placeholder="可填写销售、领料或盘亏说明等"
          :maxlength="200"
        />
      </view>
    </view>

    <view class="card preview-card" v-if="selectedStock">
      <view class="preview-card__header">
        <text class="preview-card__title">出库预览</text>
        <text class="preview-card__badge">{{ selectedStock.productType === 'FINISHED_PRODUCT' ? '成品' : '原料' }}</text>
      </view>

      <view class="preview-grid">
        <view class="preview-item">
          <text class="preview-item__label">商品</text>
          <text class="preview-item__value">{{ selectedStock.productName }}</text>
        </view>
        <view class="preview-item">
          <text class="preview-item__label">编码</text>
          <text class="preview-item__value">{{ selectedStock.productCode }}</text>
        </view>
        <view class="preview-item">
          <text class="preview-item__label">当前库存</text>
          <text class="preview-item__value">{{ selectedStock.quantity }}{{ selectedStock.unit }}</text>
        </view>
        <view class="preview-item">
          <text class="preview-item__label">可用库存</text>
          <text class="preview-item__value">{{ selectedStock.availableQuantity }}{{ selectedStock.unit }}</text>
        </view>
      </view>

      <view class="batch-preview" v-if="selectedBatch">
        <text class="batch-preview__label">当前批次</text>
        <text class="batch-preview__value">{{ selectedBatch.batchNo }} · 可用 {{ selectedBatch.availableQuantity }}{{ selectedBatch.productUnit }}</text>
      </view>
    </view>

    <view class="card notice-card" v-if="errorMessage">
      <text class="notice-card__text">{{ errorMessage }}</text>
    </view>

    <view class="action-bar">
      <button class="secondary-button action-bar__btn" :disabled="submitting" @click="resetForm">重置</button>
      <button class="primary-button action-bar__btn" :disabled="submitting || !selectedStock" @click="handleSubmit">
        {{ submitting ? '提交中...' : '确认出库' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { inventoryApi, type StockBatch, type StockItem, type StockOperationRequest, type StockOutSubType } from '@/api/inventory'
import NetworkBanner from '@/components/NetworkBanner.vue'

interface OutboundBatch extends StockBatch {
  displayName: string
}

const loadingStocks = ref(false)
const batchesLoading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const searchKeyword = ref('')
const stocks = ref<StockItem[]>([])
const batches = ref<OutboundBatch[]>([])
const selectedBatchIndex = ref(0)
const productPicking = ref(true)

const operationOptions: Array<{ value: StockOutSubType; label: string }> = [
  { value: 'SALES', label: '销售出库' },
  { value: 'PRODUCTION_USAGE', label: '生产领料' }
]

const form = reactive<StockOperationRequest>({
  productId: 0,
  subType: 'SALES',
  quantity: 1,
  batchId: 0,
  businessUnitPrice: undefined,
  remark: ''
})

const selectedStock = computed(() => stocks.value.find((item) => item.productId === form.productId) || null)
const isSalesOutbound = computed(() => form.subType === 'SALES')
const selectedBatch = computed(() => batches.value[selectedBatchIndex.value] || null)

const filteredStocks = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  if (!keyword) {
    return stocks.value
  }

  return stocks.value.filter((item) => {
    return item.productName.toLowerCase().includes(keyword) || item.productCode.toLowerCase().includes(keyword)
  })
})

const filteredBatches = computed(() => batches.value)

const batchDisplayText = computed(() => {
  if (!form.productId) return '请先选择商品'
  if (batchesLoading.value) return '正在加载批次'
  if (filteredBatches.value.length === 0) return '该商品暂无可用批次'
  return selectedBatch.value ? `${selectedBatch.value.batchNo} · 可用 ${selectedBatch.value.availableQuantity}${selectedBatch.value.productUnit}` : '请选择批次'
})

function resetForm() {
  form.productId = 0
  form.subType = 'SALES'
  form.quantity = 1
  form.batchId = 0
  form.businessUnitPrice = undefined
  form.remark = ''
  searchKeyword.value = ''
  batches.value = []
  selectedBatchIndex.value = 0
  errorMessage.value = ''
  productPicking.value = true
}

function selectStock(productId: number) {
  const isSameProduct = form.productId === productId
  form.productId = productId
  productPicking.value = false

  if (!isSameProduct) {
    form.batchId = 0
    selectedBatchIndex.value = 0
    batches.value = []
    void loadBatches(productId)
  }
  errorMessage.value = ''
}

function startPicking() {
  productPicking.value = true
}

function onBatchChange(event: { detail: { value: string } }) {
  selectedBatchIndex.value = Number(event.detail.value)
  form.batchId = selectedBatch.value?.id || 0
}

function validateForm() {
  if (!form.productId) {
    errorMessage.value = '请选择要出库的商品'
    return false
  }

  if (!Number.isInteger(form.quantity) || form.quantity <= 0) {
    errorMessage.value = '请输入正确的出库数量'
    return false
  }

  if (!form.batchId) {
    errorMessage.value = '请选择要出库的批次'
    return false
  }

  const batch = selectedBatch.value
  if (batch && form.quantity > batch.availableQuantity) {
    const msg = `批次「${batch.batchNo}」可用 ${batch.availableQuantity}${batch.productUnit}，少 ${form.quantity - batch.availableQuantity}${batch.productUnit}`
    errorMessage.value = msg
    uni.showModal({
      title: '批次库存不足',
      content: msg,
      showCancel: false,
      confirmText: '我知道了'
    })
    return false
  }

  const stock = selectedStock.value
  if (stock && form.quantity > stock.availableQuantity) {
    const msg = `${stock.productName} 可用 ${stock.availableQuantity}${stock.unit}，少 ${form.quantity - stock.availableQuantity}${stock.unit}`
    errorMessage.value = msg
    uni.showModal({
      title: '库存不足',
      content: msg,
      showCancel: false,
      confirmText: '我知道了'
    })
    return false
  }

  if (isSalesOutbound.value && form.businessUnitPrice != null && form.businessUnitPrice < 0) {
    errorMessage.value = '销售单价不能小于 0'
    return false
  }

  errorMessage.value = ''
  return true
}

async function loadStocks() {
  loadingStocks.value = true
  errorMessage.value = ''

  try {
    const response = await inventoryApi.getAllStocks(0, 200)
    stocks.value = response.content || []
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '库存商品加载失败'
  } finally {
    loadingStocks.value = false
  }
}

async function loadBatches(productId: number) {
  batchesLoading.value = true
  errorMessage.value = ''

  try {
    const response = await inventoryApi.getBatchesByProductId(productId)
    const sorted = [...(response || [])].sort((a, b) => {
      // 按到期日升序：最临期的排在最前面，作为 FIFO 默认推荐
      const aDate = a.expiryDate || '9999-12-31'
      const bDate = b.expiryDate || '9999-12-31'
      return aDate.localeCompare(bDate)
    })

    batches.value = sorted.map((batch) => ({
      ...batch,
      displayName: `${batch.batchNo} · 可用 ${batch.availableQuantity}${batch.productUnit}`
    }))
    selectedBatchIndex.value = 0
    form.batchId = batches.value[0]?.id || 0
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '批次加载失败'
    batches.value = []
    form.batchId = 0
  } finally {
    batchesLoading.value = false
  }
}

async function handleSubmit() {
  if (submitting.value || !validateForm()) {
    return
  }

  submitting.value = true
  errorMessage.value = ''

  try {
    await inventoryApi.createOutbound({
      productId: form.productId,
      subType: form.subType,
      quantity: form.quantity,
      batchId: form.batchId,
      businessUnitPrice: isSalesOutbound.value && form.businessUnitPrice != null ? form.businessUnitPrice : undefined,
      remark: form.remark || undefined
    })

    uni.showToast({ title: '出库成功', icon: 'success' })
    resetForm()
    await loadStocks()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '提交出库失败'
    uni.showToast({ title: errorMessage.value, icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadStocks()
})
</script>

<style scoped lang="scss">
.stock-out-page {
  padding-bottom: 140rpx;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.summary-card {
  padding: 26rpx;
}

.summary-card__label {
  display: block;
  font-size: 24rpx;
  color: #64748b;
  margin-bottom: 10rpx;
}

.summary-card__value {
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.section-card {
  margin-bottom: 24rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  align-items: flex-end;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #0f172a;
}

.section-tip {
  font-size: 24rpx;
  color: #94a3b8;
}

.section-link {
  font-size: 26rpx;
  font-weight: 700;
  color: #1d4ed8;
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(37, 99, 235, 0.08);
}

.selected-product {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding: 22rpx 24rpx;
  border-radius: 18rpx;
  background: #f8fafc;
  border: 2rpx solid #dbe3f0;
}

.selected-product__name {
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}

.selected-product__code {
  font-size: 24rpx;
  color: #64748b;
}

.selected-product__meta {
  display: flex;
  gap: 24rpx;
  font-size: 24rpx;
  color: #475569;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 18rpx;
  height: 88rpx;
  padding: 0 24rpx;
  border-radius: 20rpx;
  background: #f8fafc;
  border: 2rpx solid #dbe3f0;
  margin-bottom: 24rpx;
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

.stock-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.stock-item {
  padding: 24rpx;
  border-radius: 20rpx;
  background: #f8fafc;
  border: 2rpx solid transparent;
}

.stock-item--active {
  background: rgba(37, 99, 235, 0.08);
  border-color: rgba(37, 99, 235, 0.3);
}

.stock-item__main {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  margin-bottom: 12rpx;
}

.stock-item__name {
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.stock-item__code {
  font-size: 24rpx;
  color: #64748b;
}

.stock-item__meta {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  font-size: 24rpx;
  color: #475569;
}

.field-group {
  margin-bottom: 24rpx;
}

.field-label {
  display: block;
  margin-bottom: 14rpx;
  font-size: 26rpx;
  color: #475569;
}

.mobile-input,
.remark-input,
.picker-display {
  width: 100%;
  border-radius: 20rpx;
  border: 2rpx solid #dbe3f0;
  background: #f8fafc;
  font-size: 28rpx;
}

.mobile-input {
  height: 88rpx;
  padding: 0 24rpx;
}

.remark-input {
  min-height: 168rpx;
  padding: 24rpx;
}

.picker-display {
  display: flex;
  align-items: center;
  min-height: 88rpx;
  padding: 0 24rpx;
  color: #0f172a;
}

.segmented {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14rpx;
}

.segmented__item {
  min-height: 80rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  background: #f8fafc;
  border: 2rpx solid #dbe3f0;
  font-size: 24rpx;
  color: #334155;
  display: flex;
  align-items: center;
  justify-content: center;
}

.segmented__item--active {
  background: #1d4ed8;
  border-color: transparent;
  color: #ffffff;
  font-weight: 700;
}

.primary-button {
  background: #1d4ed8;
}

.primary-button:active {
  background: #1e40af;
}

.preview-card {
  margin-bottom: 24rpx;
}

.preview-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22rpx;
}

.preview-card__title {
  font-size: 32rpx;
  font-weight: 700;
  color: #0f172a;
}

.preview-card__badge {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(37, 99, 235, 0.1);
  color: #1d4ed8;
  font-size: 22rpx;
  font-weight: 700;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.preview-item {
  padding: 20rpx;
  border-radius: 18rpx;
  background: #f8fafc;
}

.preview-item__label {
  display: block;
  margin-bottom: 10rpx;
  font-size: 22rpx;
  color: #64748b;
}

.preview-item__value {
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.batch-preview {
  margin-top: 18rpx;
  padding: 20rpx;
  border-radius: 18rpx;
  background: rgba(37, 99, 235, 0.08);
}

.batch-preview__label {
  display: block;
  margin-bottom: 8rpx;
  font-size: 22rpx;
  color: #64748b;
}

.batch-preview__value {
  font-size: 26rpx;
  font-weight: 700;
  color: #1d4ed8;
}

.notice-card {
  margin-bottom: 24rpx;
  background: #fff7ed;
  border: 2rpx solid #fdba74;
}

.notice-card__text {
  font-size: 26rpx;
  color: #c2410c;
}

.empty-state {
  padding: 40rpx 24rpx;
  text-align: center;
}

.empty-state__title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.empty-state__desc {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #94a3b8;
}

.action-bar {
  position: fixed;
  left: 32rpx;
  right: 32rpx;
  bottom: calc(28rpx + env(safe-area-inset-bottom));
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 18rpx;
  padding: 18rpx;
  border-radius: 28rpx;
  background: rgba(245, 247, 251, 0.92);
  backdrop-filter: blur(14px);
  box-shadow: 0 -8rpx 28rpx rgba(15, 23, 42, 0.08);
}

.action-bar__btn {
  height: 88rpx;
  line-height: 88rpx;
}
</style>
