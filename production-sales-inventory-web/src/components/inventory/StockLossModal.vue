<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <header class="modal-header">
          <h2>新增报损</h2>
          <button type="button" class="modal-close" @click="closeModal">
            <XMarkIcon class="close-icon" />
          </button>
        </header>

        <form class="modal-body" @submit.prevent="handleSubmit">
          <div class="form-grid">
            <label class="form-field">
              <span>库存产品选择</span>
              <select v-model.number="form.productId" required>
                <option :value="0" disabled>请选择库存产品</option>
                <option v-for="stock in stocks" :key="stock.productId" :value="stock.productId">
                  {{ stock.productName }} ({{ stock.productCode }}) - 可用 {{ stock.availableQuantity }}{{ stock.unit }}
                </option>
              </select>
            </label>

            <label class="form-field">
              <span>报损批次</span>
              <select v-model.number="form.batchId" required :disabled="!form.productId || batchesLoading">
                <option :value="0" disabled>{{ batchPlaceholder }}</option>
                <option v-for="batch in batches" :key="batch.id" :value="batch.id">
                  {{ batch.batchNo }} - 可用 {{ batch.availableQuantity }}{{ batch.productUnit }}
                </option>
              </select>
            </label>

            <div v-if="selectedStock" class="preview-card">
              <div class="preview-card__header">
                <div>
                  <strong>{{ selectedStock.productName }}</strong>
                  <span>{{ selectedBatch?.batchNo || selectedStock.productCode }}</span>
                </div>
                <span class="preview-card__badge">损失预估</span>
              </div>
              <div class="preview-card__grid">
                <div>
                  <label>{{ selectedBatch ? '批次成本单价' : '产品成本单价' }}</label>
                  <strong>{{ formatMoney(costUnitPrice) }}</strong>
                </div>
                <div>
                  <label>预估损失金额</label>
                  <strong>{{ formatMoney(previewLossAmount) }}</strong>
                </div>
              </div>
            </div>

            <label class="form-field">
              <span>报损类型</span>
              <select v-model="form.lossType" required>
                <option value="PRODUCTION_LOSS">生产报损</option>
                <option value="PACKAGING_LOSS">包装报损</option>
                <option value="SHIPPING_LOSS">运输报损</option>
                <option value="EXPIRED_LOSS">过期报损</option>
                <option value="DAMAGE_LOSS">破损报损</option>
                <option value="OTHER_LOSS">其他报损</option>
              </select>
            </label>

            <label class="form-field">
              <span>报损数量</span>
              <input v-model.number="form.quantity" type="number" min="1" step="1" placeholder="请输入报损数量" required />
            </label>

            <label class="form-field">
              <span>报损原因</span>
              <textarea v-model="form.remark" rows="3" placeholder="可填写过期、破损、运输损耗等说明"></textarea>
            </label>
          </div>

          <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="loading || submitDisabled">
              {{ loading ? '处理中...' : '确认报损' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { XMarkIcon } from '@heroicons/vue/24/outline'
import { inventoryApi, type StockBatch, type StockItem, type StockLossRequest } from '@/api/inventory'

const props = defineProps<{ isOpen: boolean }>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const loading = ref(false)
const batchesLoading = ref(false)
const errorMessage = ref('')
const stocks = ref<StockItem[]>([])
const batches = ref<StockBatch[]>([])
const form = ref<StockLossRequest>({
  productId: 0,
  batchId: 0,
  lossType: 'DAMAGE_LOSS',
  quantity: 0,
  remark: ''
})

const selectedStock = computed(() => stocks.value.find(stock => stock.productId === form.value.productId) || null)
const selectedBatch = computed(() => batches.value.find(batch => batch.id === form.value.batchId) || null)
const costUnitPrice = computed(() => selectedBatch.value?.unitCost || selectedStock.value?.costPrice || 0)
const previewLossAmount = computed(() => costUnitPrice.value * Math.max(form.value.quantity, 0))
const submitDisabled = computed(() => {
  if (!form.value.productId || !form.value.batchId || form.value.quantity <= 0) return true
  return selectedBatch.value !== null && form.value.quantity > selectedBatch.value.availableQuantity
})
const batchPlaceholder = computed(() => {
  if (!form.value.productId) return '请先选择库存产品'
  if (batchesLoading.value) return '正在加载批次'
  if (batches.value.length === 0) return '该库存产品暂无可用批次'
  return '请选择报损批次'
})

watch(() => form.value.productId, (productId) => {
  form.value.batchId = 0
  if (productId) {
    loadBatches(productId)
  } else {
    batches.value = []
  }
})

watch(() => props.isOpen, (isOpen) => {
  if (isOpen) {
    resetForm()
    loadStocks()
  }
})

async function loadStocks() {
  try {
    const result = await inventoryApi.getAllStocks(0, 1000)
    stocks.value = result.content
  } catch (error) {
    console.error('加载库存产品失败:', error)
    errorMessage.value = '库存产品加载失败，请稍后重试'
  }
}

async function loadBatches(productId: number) {
  batchesLoading.value = true
  try {
    batches.value = await inventoryApi.getBatchesByProductId(productId)
  } catch (error) {
    console.error('加载批次失败:', error)
    errorMessage.value = '批次加载失败，请稍后重试'
  } finally {
    batchesLoading.value = false
  }
}

async function handleSubmit() {
  if (submitDisabled.value) {
    errorMessage.value = selectedBatch.value ? '报损数量不能超过批次可用库存' : '请完整填写报损信息'
    return
  }

  loading.value = true
  try {
    const payload: StockLossRequest = {
      productId: form.value.productId,
      batchId: form.value.batchId,
      lossType: form.value.lossType,
      quantity: form.value.quantity
    }
    if (form.value.remark?.trim()) {
      payload.remark = form.value.remark.trim()
    }
    await inventoryApi.reportLoss(payload)
    emit('success')
    closeModal()
  } catch (error) {
    console.error('报损失败:', error)
    errorMessage.value = error instanceof Error ? error.message : '报损失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function formatMoney(value: number) {
  return `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function closeModal() {
  emit('close')
}

function resetForm() {
  errorMessage.value = ''
  batches.value = []
  form.value = {
    productId: 0,
    batchId: 0,
    lossType: 'DAMAGE_LOSS',
    quantity: 0,
    remark: ''
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.6);
}

.modal-content {
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.15);
}

.modal-header,
.modal-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-actions {
  position: sticky;
  bottom: -24px;
  justify-content: flex-end;
  gap: 12px;
  margin: 24px -24px -24px;
  border-top: 1px solid #e5e7eb;
  border-bottom: 0;
  background: #fff;
}

.modal-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.modal-close {
  border: 0;
  background: transparent;
  color: #64748b;
  cursor: pointer;
}

.close-icon {
  width: 20px;
  height: 20px;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
}

.form-grid,
.form-field {
  display: grid;
  gap: 14px;
}

.form-field {
  gap: 8px;
}

.form-field span {
  color: #374151;
  font-size: 14px;
  font-weight: 600;
}

.form-field input,
.form-field select,
.form-field textarea {
  min-height: 44px;
  padding: 11px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
}

.form-field textarea {
  resize: vertical;
}

.preview-card {
  padding: 14px;
  border: 1px solid #fee2e2;
  border-radius: 10px;
  background: #fffafa;
}

.preview-card__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.preview-card__header div {
  display: grid;
  gap: 3px;
}

.preview-card__header strong {
  color: #0f172a;
  font-size: 16px;
}

.preview-card__header span {
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.preview-card__badge {
  align-self: flex-start;
  padding: 4px 8px;
  border-radius: 999px;
  background: #fee2e2;
  color: #991b1b !important;
}

.preview-card__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.preview-card__grid > div {
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
}

.preview-card__grid label {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.preview-card__grid strong {
  color: #0f172a;
  font-size: 18px;
}

.form-error {
  margin: 14px 0 0;
  padding: 10px 12px;
  border: 1px solid #fecaca;
  border-radius: 10px;
  background: #fef2f2;
  color: #b91c1c;
  font-size: 14px;
}

.btn {
  min-width: 96px;
  min-height: 42px;
  border: 0;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
}

.btn-primary {
  background: #dc2626;
  color: #fff;
}

.btn-primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.btn-secondary {
  border: 1px solid #d1d5db;
  background: #f3f4f6;
  color: #374151;
}
</style>
