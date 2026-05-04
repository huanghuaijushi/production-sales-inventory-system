<template>
  <section class="inventory-table-card">
    <header class="table-card-header">
      <div>
        <p class="subtle-label">库存列表</p>
        <h2>存货明细</h2>
      </div>
      <p class="table-card-note">共有 {{ items.length }} 条符合条件的库存记录</p>
    </header>

    <div class="table-wrapper">
      <table class="inventory-table">
        <thead>
          <tr>
            <th>商品名称</th>
            <th>SKU</th>
            <th>分类</th>
            <th>单位</th>
            <th>库存数量</th>
            <th>警戒库存</th>
            <th class="actions-column">状态 / 操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading" v-for="index in 5" :key="`skeleton-${index}`" class="table-row skeleton-row">
            <td colspan="7">
              <div class="skeleton-cell"></div>
            </td>
          </tr>

          <tr v-else-if="items.length === 0" class="table-row empty-row">
            <td colspan="7">当前没有匹配的库存数据，请调整筛选条件。</td>
          </tr>

          <tr v-else v-for="item in items" :key="item.id" class="table-row">
            <td>
              <div class="product-cell">
                <div class="product-thumb">仓</div>
                <div>
                  <p class="product-name">{{ item.productName }}</p>
                  <p class="product-meta">{{ item.category }}</p>
                </div>
              </div>
            </td>
            <td>{{ item.productCode }}</td>
            <td>{{ item.category }}</td>
            <td>{{ item.unit }}</td>
            <td>{{ item.quantity }}</td>
            <td>{{ item.alertQuantity }}</td>
            <td class="actions-column">
              <span :class="['status-tag', statusClass(item)]">{{ statusLabel(item) }}</span>
              <button type="button" class="action-link" @click="emit('edit', item)">编辑</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { type StockItem } from '@/api/inventory'

const props = defineProps<{
  items: StockItem[]
  loading: boolean
  pageSize: number
}>()

const emit = defineEmits<{
  (e: 'edit', item: StockItem): void
}>()

function statusClass(item: StockItem) {
  if (item.quantity === 0) return 'status-out'
  if (item.isLowStock) return 'status-warning'
  return 'status-normal'
}

function statusLabel(item: StockItem) {
  if (item.quantity === 0) return '缺货'
  if (item.isLowStock) return '预警'
  return '正常'
}
</script>

<style scoped>
.inventory-table-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.table-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.subtle-label {
  margin: 0 0 6px;
  color: #64748b;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.inventory-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 780px;
}

.inventory-table th,
.inventory-table td {
  padding: 12px 14px;
  text-align: left;
  color: #0f172a;
  font-size: 13px;
}

.inventory-table th {
  background: #f8fafc;
  color: #334155;
  font-weight: 700;
  border-bottom: 1px solid #e2e8f0;
}

.inventory-table tbody tr:hover {
  background: #f8fafc;
}

.table-row.empty-row td {
  padding: 32px;
  text-align: center;
  color: #64748b;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-thumb {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #e0f2fe;
  color: #0c4a6e;
  font-weight: 700;
}

.product-name {
  margin: 0 0 4px;
  font-weight: 700;
}

.product-meta {
  margin: 0;
  color: #64748b;
  font-size: 12px;
}

.actions-column {
  position: sticky;
  right: 0;
  background: #ffffff;
  min-width: 150px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  padding: 5px 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.status-normal {
  background: #ecfdf5;
  color: #16a34a;
}

.status-warning {
  background: #fef3c7;
  color: #b45309;
}

.status-out {
  background: #fee2e2;
  color: #b91c1c;
}

.action-link {
  padding: 6px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #ffffff;
  color: #0f172a;
  cursor: pointer;
}

.action-link:hover {
  background: #f8fafc;
}

.table-wrapper {
  overflow-x: auto;
}

.skeleton-row td {
  padding: 18px;
}

.skeleton-cell {
  height: 16px;
  width: 100%;
  border-radius: 10px;
  background: linear-gradient(90deg, #eef2ff 25%, #e2e8f0 50%, #eef2ff 75%);
  background-size: 400% 100%;
  animation: shimmer 1.2s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -400px 0;
  }
  100% {
    background-position: 400px 0;
  }
}
</style>
