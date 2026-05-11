<template>
  <section class="inventory-filter-card">
    <div class="filter-section">
      <label class="filter-field">
        <span>搜索库存</span>
        <div class="filter-input-group">
          <input
            type="text"
            v-model="localFilters.query"
            placeholder="按商品名称或 SKU 搜索"
            @keyup.enter="onSearch"
          />
          <button type="button" class="filter-button" @click="onSearch">搜索</button>
        </div>
      </label>

      <label class="filter-field">
        <span>分类</span>
        <select v-model="localFilters.category" @change="onUpdate">
          <option value="all">全部分类</option>
          <option value="原料">原料</option>
          <option value="半成品">半成品</option>
          <option value="成品">成品</option>
          <option value="包装">包装</option>
        </select>
      </label>

      <label class="filter-field">
        <span>状态</span>
        <select v-model="localFilters.status" @change="onUpdate">
          <option value="all">全部状态</option>
          <option value="normal">正常</option>
          <option value="warning">预警</option>
          <option value="out-of-stock">缺货</option>
        </select>
      </label>
    </div>

    <div class="filter-actions">
      <button type="button" class="btn btn-primary" @click="handleInbound">新增入库</button>
      <button type="button" class="btn btn-outline" @click="handleOutbound">新增出库</button>
      <button type="button" class="btn btn-outline" @click="handleLoss">新增报损</button>
      <button type="button" class="btn btn-outline" @click="handleCheck">库存盘点</button>
      <button type="button" class="btn btn-secondary" @click="handleExport">导出报表</button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { type PropType, watch, reactive } from 'vue'

interface InventoryFilters {
  query: string
  category: string
  status: string
}

const props = defineProps({
  filters: {
    type: Object as PropType<InventoryFilters>,
    required: true
  }
})

const emit = defineEmits<{
  (e: 'update:filters', filters: InventoryFilters): void
  (e: 'search'): void
  (e: 'inbound'): void
  (e: 'outbound'): void
  (e: 'loss'): void
  (e: 'check'): void
  (e: 'export'): void
}>()

const localFilters = reactive({
  query: props.filters.query,
  category: props.filters.category,
  status: props.filters.status
})

watch(
  () => props.filters,
  (next) => {
    localFilters.query = next.query
    localFilters.category = next.category
    localFilters.status = next.status
  },
  { deep: true }
)

function onUpdate() {
  emit('update:filters', {
    query: localFilters.query,
    category: localFilters.category,
    status: localFilters.status
  })
}

function onSearch() {
  emit('update:filters', {
    query: localFilters.query,
    category: localFilters.category,
    status: localFilters.status
  })
  emit('search')
}

function handleInbound() {
  emit('inbound')
}

function handleOutbound() {
  emit('outbound')
}

function handleLoss() {
  emit('loss')
}

function handleCheck() {
  emit('check')
}

function handleExport() {
  emit('export')
}
</script>

<style scoped>
.inventory-filter-card {
  display: grid;
  gap: 14px;
  padding: 14px 16px;
  margin-bottom: 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.filter-section {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
}

.filter-field {
  display: grid;
  gap: 6px;
  color: #475569;
  font-size: 13px;
}

.filter-field span {
  font-weight: 600;
}

.filter-input-group {
  display: flex;
  gap: 8px;
}

.filter-input-group input,
.filter-field select {
  flex: 1;
  min-height: 38px;
  padding: 0 11px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #ffffff;
  color: #0f172a;
  font-size: 14px;
}

.filter-button {
  min-width: 78px;
  padding: 0 13px;
  border: none;
  border-radius: 8px;
  background: #2563eb;
  color: #ffffff;
  cursor: pointer;
}

.filter-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.btn {
  min-width: 108px;
  height: 38px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.btn-primary {
  background: #2563eb;
  color: #fff;
  border: 1px solid transparent;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.btn-outline {
  background: #ffffff;
  border: 1px solid #cbd5e1;
  color: #0f172a;
}

.btn-outline:hover {
  background: #f8fafc;
}

.btn-secondary {
  background: #eff6ff;
  border: 1px solid transparent;
  color: #2563eb;
}

.btn-secondary:hover {
  background: #e0efff;
}
</style>
