<template>
  <div class="timeline-card">
    <div class="card-header">
      <h3 class="card-title">操作日志</h3>
      <RouterLink to="/inventory" class="header-link">查看流水</RouterLink>
    </div>
    <div class="timeline-container">
      <div v-if="loading" class="empty-state">正在加载操作日志...</div>
      <div v-else-if="records.length === 0" class="empty-state">暂无操作日志。</div>
      <div class="timeline-item" v-for="record in records" v-else :key="record.id">
        <div class="timeline-dot" :class="dotClass(record)"></div>
        <div class="timeline-content">
          <div class="timeline-time">{{ formatTime(record.createdAt) }}</div>
          <div class="timeline-text">{{ logText(record) }}</div>
          <div v-if="record.remark" class="timeline-remark">{{ record.remark }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type StockRecord } from '@/api/inventory'

defineProps<{
  records: StockRecord[]
  loading: boolean
}>()

const subTypeText: Record<StockRecord['subType'], string> = {
  PURCHASE: '采购',
  PRODUCTION: '生产',
  SALES: '销售',
  LOSS: '领用/损耗',
  INVENTORY: '盘点'
}

function dotClass(record: StockRecord) {
  if (record.type === 'IN') return 'in'
  if (record.type === 'OUT') return 'out'
  return 'check'
}

function logText(record: StockRecord) {
  const actionText = record.type === 'IN' ? '入库' : record.type === 'OUT' ? '出库' : '调整'
  const quantityText = `${record.quantity > 0 ? '+' : ''}${record.quantity}${record.productUnit}`
  return `${record.operatorName} ${subTypeText[record.subType]}${actionText} ${record.productName} ${quantityText}`
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}
</script>

<style scoped>
.timeline-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 260px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
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

.timeline-container {
  flex: 1;
  position: relative;
  padding-left: 20px;
  overflow-y: auto;
}

.timeline-container::before {
  content: '';
  position: absolute;
  left: 15px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #E5E7EB;
}

.timeline-item {
  position: relative;
  margin-bottom: 14px;
  display: flex;
  align-items: flex-start;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  position: absolute;
  left: -6px;
  top: 6px;
  border: 2px solid white;
}

.timeline-dot.in {
  background: #52C41A;
}

.timeline-dot.out {
  background: #FF4D4F;
}

.timeline-dot.check {
  background: #FAAD14;
}

.timeline-content {
  margin-left: 16px;
}

.timeline-time {
  font-size: 12px;
  color: #9CA3AF;
  margin-bottom: 4px;
}

.timeline-text {
  font-size: 13px;
  color: #1F2937;
  line-height: 1.4;
}

.timeline-remark {
  margin-top: 4px;
  color: #6B7280;
  font-size: 12px;
  line-height: 1.4;
}

.empty-state {
  padding: 28px 0;
  color: #9CA3AF;
  text-align: center;
  font-size: 14px;
}
</style>
