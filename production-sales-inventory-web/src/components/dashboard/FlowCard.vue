<template>
  <div class="flow-card">
    <div class="card-header">
      <h3 class="card-title">今日业务概览</h3>
      <span v-if="loading" class="refresh-label">刷新中</span>
    </div>
    <div class="overview-grid">
      <div class="overview-item overview-item--inbound">
        <div class="overview-label">今日入库</div>
        <div class="overview-value">{{ formatNumber(overview?.inboundRecordCount) }}</div>
        <div class="overview-unit">笔 · 数量 {{ formatNumber(overview?.inboundQuantity) }}</div>
      </div>
      <div class="overview-item overview-item--outbound">
        <div class="overview-label">今日出库</div>
        <div class="overview-value">{{ formatNumber(overview?.outboundRecordCount) }}</div>
        <div class="overview-unit">笔 · 数量 {{ formatNumber(overview?.outboundQuantity) }}</div>
      </div>
      <div class="overview-item overview-item--loss">
        <div class="overview-label">损耗记录</div>
        <div class="overview-value">{{ formatNumber(overview?.lossRecordCount) }}</div>
        <div class="overview-unit">笔 · 数量 {{ formatNumber(overview?.lossQuantity) }}</div>
      </div>
      <div class="overview-item overview-item--orders">
        <div class="overview-label">待发货订单</div>
        <div class="overview-value">{{ formatNumber(overview?.pendingSalesOrderCount) }}</div>
        <div class="overview-unit">单</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type TodayBusinessOverview } from '@/api/inventory'

defineProps<{
  overview: TodayBusinessOverview | null
  loading: boolean
}>()

function formatNumber(value: number | null | undefined) {
  return value === null || value === undefined
    ? '--'
    : new Intl.NumberFormat('zh-CN').format(value)
}
</script>

<style scoped>
.flow-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 20px;
  min-height: 220px;
  height: 100%;
  display: flex;
  flex-direction: column;
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

.refresh-label {
  flex-shrink: 0;
  color: #64748B;
  font-size: 12px;
  font-weight: 700;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  align-content: start;
}

.overview-item {
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 100px;
}

.overview-item--inbound {
  background: #F0FDF4;
  border-color: #BBF7D0;
}

.overview-item--outbound {
  background: #EFF6FF;
  border-color: #BFDBFE;
}

.overview-item--loss {
  background: #FFF7ED;
  border-color: #FED7AA;
}

.overview-item--orders {
  background: #F8FAFC;
  border-color: #CBD5E1;
}

.overview-label {
  font-size: 13px;
  color: #6B7280;
}

.overview-value {
  font-size: 32px;
  font-weight: 700;
  color: #1F2937;
  line-height: 1;
}

.overview-unit {
  font-size: 12px;
  color: #9CA3AF;
}

@media (max-width: 768px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
