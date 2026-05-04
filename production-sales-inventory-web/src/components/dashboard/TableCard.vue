<template>
  <div class="table-card">
    <div class="card-header">
      <h3 class="card-title">库存预警</h3>
      <RouterLink to="/inventory" class="header-link">查看全部</RouterLink>
    </div>
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>产品名称</th>
            <th>当前库存</th>
            <th>预警值</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="4" class="empty-cell">正在加载库存预警...</td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="4" class="empty-cell">暂无库存预警。</td>
          </tr>
          <tr v-for="item in items" v-else :key="item.id">
            <td>
              <div class="product-name">{{ item.productName }}</div>
              <div class="product-code">{{ item.productCode }}</div>
            </td>
            <td :class="{ 'text-danger': item.quantity <= item.alertQuantity }">
              {{ item.quantity }}{{ item.unit }}
            </td>
            <td>{{ item.alertQuantity }}{{ item.unit }}</td>
            <td>
              <span class="status-badge" :class="statusClass(item)">{{ statusText(item) }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type StockItem } from '@/api/inventory'

defineProps<{
  items: StockItem[]
  loading: boolean
}>()

function statusClass(item: StockItem) {
  return item.quantity === 0 ? 'danger' : 'warning'
}

function statusText(item: StockItem) {
  return item.quantity === 0 ? '缺货' : '预警'
}
</script>

<style scoped>
.table-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 300px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
}

.header-link {
  color: #2563eb;
  text-decoration: none;
  font-size: 13px;
  font-weight: 700;
}

.table-container {
  flex: 1;
  overflow-y: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 8px;
  text-align: left;
  border-bottom: 1px solid #F3F4F6;
  font-size: 14px;
}

.data-table th {
  font-weight: 600;
  color: #6B7280;
  background: #F9FAFB;
}

.data-table td {
  color: #1F2937;
}

.text-danger {
  color: #FF4D4F;
  font-weight: 600;
}

.product-name {
  color: #111827;
  font-weight: 700;
}

.product-code {
  margin-top: 4px;
  color: #9CA3AF;
  font-size: 12px;
}

.empty-cell {
  padding: 28px 8px;
  text-align: center;
  color: #9CA3AF;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.warning {
  background: #FEF3C7;
  color: #D97706;
}

.status-badge.danger {
  background: #FEE2E2;
  color: #DC2626;
}

</style>
