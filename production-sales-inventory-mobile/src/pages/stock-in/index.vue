<template>
  <view class="container stock-in-page">
    <view class="hero card">
      <text class="hero__eyebrow">Inventory Inbound</text>
      <text class="hero__title">入库</text>
      <text class="hero__subtitle">选择物料、填写数量与批次信息，快速完成采购、生产或盘点入库。</text>
    </view>

    <view class="summary-row">
      <view class="summary-card card">
        <text class="summary-card__label">当前状态</text>
        <text class="summary-card__value">{{ selectedStock ? '已选择商品' : '未选择商品' }}</text>
      </view>
      <view class="summary-card card">
        <text class="summary-card__label">库存数量</text>
        <text class="summary-card__value">{{ selectedStock ? `${selectedStock.quantity}${selectedStock.unit}` : '-' }}</text>
      </view>
    </view>

    <view class="card section-card">
      <view class="section-head">
        <text class="section-title">1. 选择商品</text>
        <text class="section-tip">可先搜索再选择</text>
      </view>

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

    <view class="card section-card">
      <view class="section-head">
        <text class="section-title">2. 填写入库信息</text>
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

      <view class="field-group">
        <text class="field-label">数量 *</text>
        <input
          v-model.number="form.quantity"
          class="form-input mobile-input"
          type="number"
          min="1"
          step="1"
          placeholder="请输入入库数量"
        />
      </view>

      <view class="field-group">
        <text class="field-label">批次号</text>
        <input
          v-model.trim="form.batchNo"
          class="form-input mobile-input"
          type="text"
          placeholder="不填则自动生成"
        />
      </view>

      <view class="field-grid">
        <view class="field-group">
          <text class="field-label">生产日期</text>
          <picker mode="date" :value="form.productionDate" @change="onProductionDateChange">
            <view class="picker-display">{{ form.productionDate || '请选择日期' }}</view>
          </picker>
        </view>

        <view class="field-group">
          <text class="field-label">到期日期</text>
          <picker mode="date" :value="form.expiryDate" @change="onExpiryDateChange">
            <view class="picker-display">{{ form.expiryDate || '请选择日期' }}</view>
          </picker>
        </view>
      </view>

      <view class="field-group">
        <text class="field-label">供应商 / 备注</text>
        <textarea
          v-model.trim="form.remark"
          class="remark-input"
          placeholder="可填写供应商、进货说明等"
          :maxlength="200"
        />
      </view>
    </view>

    <view class="card preview-card" v-if="selectedStock">
      <view class="preview-card__header">
        <text class="preview-card__title">入库预览</text>
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
    </view>

    <view class="card notice-card" v-if="errorMessage">
      <text class="notice-card__text">{{ errorMessage }}</text>
    </view>

    <view class="action-bar">
      <button class="secondary-button action-bar__btn" :disabled="submitting" @click="resetForm">重置</button>
      <button class="primary-button action-bar__btn" :disabled="submitting || !selectedStock" @click="handleSubmit">
        {{ submitting ? '提交中...' : '确认入库' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { inventoryApi, type StockItem, type StockOperationRequest, type StockRecordSubType } from '@/api/inventory'

const loadingStocks = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const searchKeyword = ref('')
const stocks = ref<StockItem[]>([])

const operationOptions: Array<{ value: StockRecordSubType; label: string }> = [
  { value: 'PURCHASE', label: '采购入库' },
  { value: 'PRODUCTION', label: '生产入库' },
  { value: 'INVENTORY', label: '盘点入库' }
]

const form = reactive<StockOperationRequest>({
  productId: 0,
  type: 'IN',
  subType: 'PURCHASE',
  quantity: 1,
  batchNo: '',
  productionDate: '',
  expiryDate: '',
  remark: ''
})

const selectedStock = computed(() => stocks.value.find((item) => item.productId === form.productId) || null)

const filteredStocks = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  if (!keyword) {
    return stocks.value
  }

  return stocks.value.filter((item) => {
    return item.productName.toLowerCase().includes(keyword) || item.productCode.toLowerCase().includes(keyword)
  })
})

function resetForm() {
  form.productId = 0
  form.subType = 'PURCHASE'
  form.quantity = 1
  form.batchNo = ''
  form.productionDate = ''
  form.expiryDate = ''
  form.remark = ''
  searchKeyword.value = ''
  errorMessage.value = ''
}

function selectStock(productId: number) {
  form.productId = productId
  errorMessage.value = ''
}

function onProductionDateChange(event: { detail: { value: string } }) {
  form.productionDate = event.detail.value
}

function onExpiryDateChange(event: { detail: { value: string } }) {
  form.expiryDate = event.detail.value
}

function validateForm() {
  if (!form.productId) {
    errorMessage.value = '请选择要入库的商品'
    return false
  }

  if (!Number.isInteger(form.quantity) || form.quantity <= 0) {
    errorMessage.value = '请输入正确的入库数量'
    return false
  }

  if (form.expiryDate && form.productionDate && form.expiryDate < form.productionDate) {
    errorMessage.value = '到期日期不能早于生产日期'
    return false
  }

  errorMessage.value = ''
  return true
}

async function loadStocks() {
  loadingStocks.value = true
  errorMessage.value = ''

  try {
    const response = await inventoryApi.getAllStocks(0, 1000)
    stocks.value = response.content || []
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载库存失败'
  } finally {
    loadingStocks.value = false
  }
}

async function handleSubmit() {
  if (submitting.value || !validateForm()) {
    return
  }

  submitting.value = true
  errorMessage.value = ''

  try {
    await inventoryApi.createInbound({
      productId: form.productId,
      type: 'IN',
      subType: form.subType,
      quantity: form.quantity,
      batchNo: form.batchNo || undefined,
      productionDate: form.productionDate || undefined,
      expiryDate: form.expiryDate || undefined,
      remark: form.remark || undefined
    })

    uni.showToast({ title: '入库成功', icon: 'success' })
    resetForm()
    await loadStocks()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '提交入库失败'
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
.stock-in-page {
  padding-bottom: 140rpx;
}

.hero {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 24rpx;
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
  color: #ffffff;
}

.hero__eyebrow {
  width: fit-content;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.16);
  font-size: 22rpx;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.hero__title {
  font-size: 48rpx;
  font-weight: 800;
}

.hero__subtitle {
  font-size: 26rpx;
  line-height: 1.7;
  opacity: 0.92;
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

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.segmented {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14rpx;
}

.segmented__item {
  min-height: 80rpx;
  padding: 16rpx 12rpx;
  border-radius: 18rpx;
  background: #f8fafc;
  border: 2rpx solid #dbe3f0;
  font-size: 24rpx;
  text-align: center;
  color: #334155;
}

.segmented__item--active {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border-color: transparent;
  color: #ffffff;
  font-weight: 700;
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
  bottom: 28rpx;
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
