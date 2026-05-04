<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <header class="modal-header">
          <h2>{{ isInbound ? '新增入库' : '新增出库' }}</h2>
          <button type="button" class="modal-close" @click="closeModal">
            <XMarkIcon class="close-icon" />
          </button>
        </header>

        <form class="modal-body" @submit.prevent="handleSubmit">
          <div class="form-grid">
            <label class="form-field">
              <span>商品选择</span>
              <select v-model.number="form.productId" required>
                <option :value="0" disabled>请选择商品</option>
                <option v-for="stock in stocks" :key="stock.productId" :value="stock.productId">
                  {{ stock.productName }} ({{ stock.productCode }}) - 当前库存 {{ stock.quantity }}{{ stock.unit }}
                </option>
              </select>
            </label>

            <label class="form-field">
              <span>操作类型</span>
              <select v-model="form.subType" required>
                <option v-for="type in operationTypes" :key="type.value" :value="type.value">
                  {{ type.label }}
                </option>
              </select>
            </label>

            <label class="form-field">
              <span>数量</span>
              <input
                type="number"
                v-model.number="form.quantity"
                min="1"
                step="1"
                placeholder="请输入数量"
                required
              />
            </label>

            <label v-if="isInbound" class="form-field">
              <span>批次号</span>
              <input
                type="text"
                v-model="form.batchNo"
                placeholder="可选，批次号"
              />
            </label>

            <label v-if="isInbound" class="form-field">
              <span>生产日期</span>
              <input
                type="date"
                v-model="form.productionDate"
              />
            </label>

            <label class="form-field">
              <span>{{ isInbound ? '供应商 / 备注' : '用途 / 备注' }}</span>
              <textarea
                v-model="form.remark"
                :placeholder="isInbound ? '可填写供应商、进货说明等' : '可填写销售、领用、损耗说明等'"
                rows="3"
              ></textarea>
            </label>
          </div>

          <div v-if="selectedStock" class="stock-preview">
            <span>当前库存：{{ selectedStock.quantity }}{{ selectedStock.unit }}</span>
            <span>可用库存：{{ selectedStock.availableQuantity }}{{ selectedStock.unit }}</span>
            <span v-if="!isInbound && form.quantity > selectedStock.availableQuantity" class="stock-preview__warning">
              出库数量不能超过可用库存
            </span>
          </div>

          <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="loading || submitDisabled">
              <span v-if="!loading">{{ isInbound ? '确认入库' : '确认出库' }}</span>
              <span v-else class="loading-text">
                <svg viewBox="0 0 24 24" class="loading-icon">
                  <circle cx="12" cy="12" r="9" />
                </svg>
                处理中...
              </span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { XMarkIcon } from '@heroicons/vue/24/outline'
import { inventoryApi, type StockItem, type StockOperationRequest } from '@/api/inventory'

interface Props {
  isOpen: boolean
  isInbound: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const loading = ref(false)
const errorMessage = ref('')
const stocks = ref<StockItem[]>([])

const form = ref<StockOperationRequest>({
  productId: 0,
  type: 'IN',
  subType: 'PURCHASE',
  quantity: 0,
  batchNo: '',
  productionDate: '',
  remark: ''
})

const operationTypes = ref([
  { value: 'PURCHASE', label: '采购入库' },
  { value: 'PRODUCTION', label: '生产入库' },
  { value: 'SALES', label: '销售出库' },
  { value: 'LOSS', label: '损耗出库' },
  { value: 'INVENTORY', label: '盘点调整' }
])

const selectedStock = computed(() => {
  return stocks.value.find((stock) => stock.productId === form.value.productId) || null
})

const submitDisabled = computed(() => {
  if (!form.value.productId || form.value.quantity <= 0) {
    return true
  }
  const stock = selectedStock.value
  return !props.isInbound
    && stock !== null
    && form.value.quantity > stock.availableQuantity
})

watch(() => props.isOpen, (isOpen) => {
  if (isOpen) {
    resetForm()
    loadStocks()
  }
})

watch(() => props.isInbound, () => {
  form.value.type = props.isInbound ? 'IN' : 'OUT'
  updateOperationTypes()
})

function updateOperationTypes() {
  if (props.isInbound) {
    operationTypes.value = [
      { value: 'PURCHASE', label: '采购入库' },
      { value: 'PRODUCTION', label: '生产入库' }
    ]
    form.value.subType = 'PURCHASE'
  } else {
    operationTypes.value = [
      { value: 'SALES', label: '销售出库' },
      { value: 'LOSS', label: '领用 / 损耗出库' },
      { value: 'INVENTORY', label: '盘亏出库' }
    ]
    form.value.subType = 'SALES'
  }
}

async function loadStocks() {
  try {
    const result = await inventoryApi.getAllStocks(0, 1000)
    stocks.value = result.content
  } catch (error) {
    console.error('加载库存商品失败:', error)
    errorMessage.value = '库存商品加载失败，请稍后重试'
  }
}

function resetForm() {
  errorMessage.value = ''
  form.value = {
    productId: 0,
    type: props.isInbound ? 'IN' : 'OUT',
    subType: props.isInbound ? 'PURCHASE' : 'SALES',
    quantity: 0,
    batchNo: '',
    productionDate: '',
    remark: ''
  }
  updateOperationTypes()
}

async function handleSubmit() {
  errorMessage.value = ''
  if (!form.value.productId || form.value.quantity <= 0) {
    errorMessage.value = '请选择商品并填写正确数量'
    return
  }
  if (submitDisabled.value) {
    errorMessage.value = '出库数量不能超过可用库存'
    return
  }

  loading.value = true
  try {
    const payload = normalizePayload()
    if (props.isInbound) {
      await inventoryApi.inbound(payload)
    } else {
      await inventoryApi.outbound(payload)
    }
    emit('success')
    closeModal()
  } catch (error) {
    console.error('库存操作失败:', error)
    errorMessage.value = error instanceof Error ? error.message : '库存操作失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function normalizePayload(): StockOperationRequest {
  const payload: StockOperationRequest = {
    productId: form.value.productId,
    type: props.isInbound ? 'IN' : 'OUT',
    subType: form.value.subType,
    quantity: form.value.quantity
  }

  if (props.isInbound && form.value.batchNo?.trim()) {
    payload.batchNo = form.value.batchNo.trim()
  }
  if (props.isInbound && form.value.productionDate) {
    payload.productionDate = form.value.productionDate
  }
  if (form.value.remark?.trim()) {
    payload.remark = form.value.remark.trim()
  }

  return payload
}

function closeModal() {
  emit('close')
}

onMounted(() => {
  updateOperationTypes()
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.15);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.modal-close {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  color: #64748b;
  transition: background-color 0.2s;
}

.modal-close:hover {
  background: #f1f5f9;
}

.close-icon {
  width: 20px;
  height: 20px;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
}

.form-grid {
  display: grid;
  gap: 16px;
}

.form-field {
  display: grid;
  gap: 8px;
}

.form-field span {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-field input,
.form-field select,
.form-field textarea {
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.form-field input:focus,
.form-field select:focus,
.form-field textarea:focus {
  border-color: #2563eb;
}

.form-field textarea {
  resize: vertical;
  min-height: 80px;
}

.stock-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-top: 16px;
  padding: 12px 14px;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
  background: #eff6ff;
  color: #1e3a8a;
  font-size: 13px;
  font-weight: 600;
}

.stock-preview__warning {
  color: #b91c1c;
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

.modal-actions {
  position: sticky;
  bottom: -24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
  margin-right: -24px;
  margin-bottom: -24px;
  margin-left: -24px;
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
  background: #ffffff;
  box-shadow: 0 -10px 24px rgba(15, 23, 42, 0.06);
}

.btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
}

.btn-primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

.loading-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-icon {
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
