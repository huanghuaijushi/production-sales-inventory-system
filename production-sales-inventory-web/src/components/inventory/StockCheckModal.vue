<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <header class="modal-header">
          <h2>库存盘点</h2>
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
              <span>盘点批次</span>
              <select v-model.number="form.batchId" required :disabled="!form.productId || batchesLoading">
                <option :value="0" disabled>{{ batchPlaceholder }}</option>
                <option v-for="batch in batches" :key="batch.id" :value="batch.id">
                  {{ batch.batchNo }} - 系统 {{ batch.availableQuantity }}{{ batch.productUnit }}
                </option>
              </select>
            </label>

            <div v-if="selectedBatch" class="check-preview">
              <div>
                <label>系统数量</label>
                <strong>{{ selectedBatch.availableQuantity }}{{ selectedBatch.productUnit }}</strong>
              </div>
              <div>
                <label>实盘数量</label>
                <strong>{{ Math.max(form.actualQuantity, 0) }}{{ selectedBatch.productUnit }}</strong>
              </div>
              <div :class="['check-preview__diff', differenceClass]">
                <label>差异数量</label>
                <strong>{{ differenceText }}</strong>
              </div>
            </div>

            <label class="form-field">
              <span>实盘数量</span>
              <input v-model.number="form.actualQuantity" type="number" min="0" step="1" placeholder="请输入实际清点数量" required />
            </label>

            <label class="form-field">
              <span>盘点备注</span>
              <textarea v-model="form.remark" rows="3" placeholder="可填写盘点原因、位置、异常说明等"></textarea>
            </label>
          </div>

          <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="loading || submitDisabled">
              {{ loading ? '处理中...' : '确认盘点' }}
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
import { inventoryApi, type StockBatch, type StockCheckRequest, type StockItem } from '@/api/inventory'

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
const form = ref<StockCheckRequest>({
  productId: 0,
  batchId: 0,
  actualQuantity: 0,
  remark: ''
})

const selectedBatch = computed(() => batches.value.find(batch => batch.id === form.value.batchId) || null)
const differenceQuantity = computed(() => {
  if (!selectedBatch.value) return 0
  return Math.max(form.value.actualQuantity, 0) - selectedBatch.value.availableQuantity
})
const differenceText = computed(() => {
  if (!selectedBatch.value) return '-'
  const prefix = differenceQuantity.value > 0 ? '+' : ''
  return `${prefix}${differenceQuantity.value}${selectedBatch.value.productUnit}`
})
const differenceClass = computed(() => {
  if (differenceQuantity.value > 0) return 'is-gain'
  if (differenceQuantity.value < 0) return 'is-loss'
  return 'is-match'
})
const submitDisabled = computed(() => !form.value.productId || !form.value.batchId || form.value.actualQuantity < 0)
const batchPlaceholder = computed(() => {
  if (!form.value.productId) return '请先选择库存产品'
  if (batchesLoading.value) return '正在加载批次'
  if (batches.value.length === 0) return '该库存产品暂无可盘点批次'
  return '请选择盘点批次'
})

watch(() => props.isOpen, (isOpen) => {
  if (isOpen) {
    resetForm()
    loadStocks()
  }
})

watch(() => form.value.productId, (productId) => {
  form.value.batchId = 0
  if (productId) {
    loadBatches(productId)
  } else {
    batches.value = []
  }
})

watch(() => form.value.batchId, () => {
  if (selectedBatch.value) {
    form.value.actualQuantity = selectedBatch.value.availableQuantity
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
    errorMessage.value = '请完整填写盘点信息'
    return
  }

  loading.value = true
  try {
    const payload: StockCheckRequest = {
      productId: form.value.productId,
      batchId: form.value.batchId,
      actualQuantity: form.value.actualQuantity
    }
    if (form.value.remark?.trim()) {
      payload.remark = form.value.remark.trim()
    }
    await inventoryApi.quickCheck(payload)
    emit('success')
    closeModal()
  } catch (error) {
    console.error('盘点失败:', error)
    errorMessage.value = error instanceof Error ? error.message : '盘点失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  errorMessage.value = ''
  batches.value = []
  form.value = {
    productId: 0,
    batchId: 0,
    actualQuantity: 0,
    remark: ''
  }
}

function closeModal() {
  emit('close')
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

.check-preview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  padding: 14px;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #f8fbff;
}

.check-preview > div {
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
}

.check-preview label {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.check-preview strong {
  color: #0f172a;
  font-size: 18px;
}

.check-preview__diff.is-gain strong {
  color: #047857;
}

.check-preview__diff.is-loss strong {
  color: #b91c1c;
}

.check-preview__diff.is-match strong {
  color: #2563eb;
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
  background: #2563eb;
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
