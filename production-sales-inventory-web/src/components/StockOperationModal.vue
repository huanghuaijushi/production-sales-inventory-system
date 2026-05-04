<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>出入库操作</h2>
        <button class="modal-close" @click="$emit('close')">&times;</button>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-body">
        <div v-if="error" class="error-message">{{ error }}</div>

        <div class="form-group">
          <label>产品 *</label>
          <select v-model="form.productId" required>
            <option value="">请选择产品</option>
            <option v-for="product in products" :key="product.id" :value="product.id">
              {{ product.productCode }} - {{ product.productName }} (库存: {{ product.quantity }}{{ product.unit }})
            </option>
          </select>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>操作类型 *</label>
            <select v-model="form.type" required>
              <option value="IN">入库</option>
              <option value="OUT">出库</option>
              <option value="ADJUST">调整</option>
            </select>
          </div>

          <div class="form-group">
            <label>业务类型 *</label>
            <select v-model="form.subType" required>
              <option value="PRODUCTION">生产入库</option>
              <option value="PURCHASE">采购入库</option>
              <option value="SALES">销售出库</option>
              <option value="LOSS">损耗</option>
              <option value="INVENTORY">盘点调整</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label>数量 *</label>
          <input v-model.number="form.quantity" type="number" min="1" required placeholder="请输入数量" />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>批次号</label>
            <input v-model="form.batchNo" type="text" placeholder="选填" />
          </div>

          <div class="form-group">
            <label>生产日期</label>
            <input v-model="form.productionDate" type="date" />
          </div>
        </div>

        <div class="form-group">
          <label>备注</label>
          <textarea v-model="form.remark" rows="3" placeholder="选填"></textarea>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn-secondary" @click="$emit('close')">取消</button>
          <button type="submit" class="btn-primary" :disabled="submitting">
            {{ submitting ? '提交中...' : '确认' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { inventoryApi, type StockItem, type StockOperationRequest } from '@/api/inventory'

const emit = defineEmits<{
  close: []
  success: []
}>()

const products = ref<StockItem[]>([])
const loading = ref(true)
const error = ref('')
const submitting = ref(false)

const form = ref<StockOperationRequest>({
  productId: 0,
  type: 'IN',
  subType: 'PRODUCTION',
  quantity: 1,
  batchNo: '',
  productionDate: '',
  remark: ''
})

async function loadProducts() {
  try {
    const result = await inventoryApi.getAllStocks(0, 1000) // 获取所有库存用于选择
    products.value = result.content
  } catch (err: any) {
    error.value = '加载产品列表失败'
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.value.productId) {
    error.value = '请选择产品'
    return
  }

  submitting.value = true
  error.value = ''

  try {
    await inventoryApi.performStockOperation(form.value)
    emit('success')
  } catch (err: any) {
    error.value = err.message || '操作失败'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
}

.modal-close {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #6b7280;
  line-height: 1;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.error-message {
  background: #fee;
  color: #c00;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.btn-primary,
.btn-secondary {
  padding: 8px 16px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.btn-primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover {
  background: #e5e7eb;
}
</style>
