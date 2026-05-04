<template>
  <Teleport to="body">
    <div v-if="isOpen && item" class="modal-overlay" @click="handleClose">
      <section class="modal-content" @click.stop>
        <header class="modal-header">
          <div>
            <p class="modal-eyebrow">库存编辑</p>
            <h2>{{ item.productName }}</h2>
          </div>
          <button type="button" class="icon-button" aria-label="关闭" @click="handleClose">
            ×
          </button>
        </header>

        <form class="modal-body" @submit.prevent="handleSubmit">
          <div class="stock-summary">
            <span>{{ item.productCode }}</span>
            <span>{{ item.category }}</span>
            <span>{{ item.specification || '-' }}</span>
          </div>

          <div class="form-grid">
            <label class="form-field">
              <span>库存数量 *</span>
              <input
                v-model.number="form.quantity"
                type="number"
                min="0"
                step="1"
                required
              />
            </label>

            <label class="form-field">
              <span>警戒库存 *</span>
              <input
                v-model.number="form.alertQuantity"
                type="number"
                min="0"
                step="1"
                required
              />
            </label>

            <label class="form-field full-width">
              <span>盘点备注</span>
              <textarea
                v-model="form.remark"
                rows="3"
                maxlength="500"
                placeholder="例如：库存列表盘点调整"
              ></textarea>
            </label>
          </div>

          <p v-if="errorMessage" class="form-message form-message--error">{{ errorMessage }}</p>

          <footer class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="handleClose">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ loading ? '保存中...' : '保存修改' }}
            </button>
          </footer>
        </form>
      </section>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { inventoryApi, type StockItem } from '@/api/inventory'

const props = defineProps<{
  isOpen: boolean
  item: StockItem | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const loading = ref(false)
const errorMessage = ref('')
const form = reactive({
  quantity: 0,
  alertQuantity: 0,
  remark: ''
})

watch(
  () => [props.isOpen, props.item] as const,
  () => {
    if (!props.isOpen || !props.item) {
      return
    }
    form.quantity = props.item.quantity
    form.alertQuantity = props.item.alertQuantity
    form.remark = ''
    errorMessage.value = ''
  },
  { immediate: true }
)

function handleClose() {
  if (loading.value) {
    return
  }
  emit('close')
}

async function handleSubmit() {
  if (!props.item) {
    return
  }
  if (!Number.isInteger(form.quantity) || form.quantity < 0) {
    errorMessage.value = '库存数量必须是大于等于 0 的整数。'
    return
  }
  if (!Number.isInteger(form.alertQuantity) || form.alertQuantity < 0) {
    errorMessage.value = '警戒库存必须是大于等于 0 的整数。'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    await inventoryApi.updateStock(props.item.id, {
      quantity: form.quantity,
      alertQuantity: form.alertQuantity,
      remark: form.remark.trim() || undefined
    })
    emit('success')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '库存保存失败，请稍后重试。'
  } finally {
    loading.value = false
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
  padding: 24px;
  background: rgba(15, 23, 42, 0.52);
}

.modal-content {
  width: min(560px, 100%);
  max-height: 90vh;
  overflow: auto;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.28);
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-eyebrow {
  margin: 0 0 6px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.modal-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.icon-button {
  width: 34px;
  height: 34px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #ffffff;
  color: #334155;
  cursor: pointer;
  font-size: 22px;
  line-height: 1;
}

.modal-body {
  padding: 24px;
}

.stock-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
}

.stock-summary span {
  padding: 6px 10px;
  border-radius: 8px;
  background: #f1f5f9;
  color: #334155;
  font-size: 12px;
  font-weight: 700;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-field.full-width {
  grid-column: 1 / -1;
}

.form-field span {
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.form-field input,
.form-field textarea {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 11px 12px;
  color: #0f172a;
  font-size: 14px;
}

.form-field input:focus,
.form-field textarea:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.form-message {
  margin: 16px 0 0;
  font-size: 13px;
}

.form-message--error {
  color: #b91c1c;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 22px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  padding: 0 16px;
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #2563eb;
  color: #ffffff;
}

.btn-secondary {
  border-color: #cbd5e1;
  background: #ffffff;
  color: #334155;
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
