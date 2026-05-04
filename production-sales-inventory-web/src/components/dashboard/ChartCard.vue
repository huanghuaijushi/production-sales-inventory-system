<template>
  <div class="chart-card">
    <div class="card-header">
      <h3 class="card-title">{{ title }}</h3>
      <select
        v-if="type === 'line'"
        class="filter-select"
        :value="trendDays ?? 7"
        @change="handleTrendDaysChange"
      >
        <option :value="7">最近7天</option>
        <option :value="30">最近30天</option>
        <option :value="90">最近90天</option>
      </select>
    </div>
    <div class="chart-container">
      <LineChart v-if="type === 'line'" :items="trend ?? []" :loading="loading ?? false" />
      <PieChart v-else :items="distribution ?? []" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { type InventoryDistributionItem, type StockTrendItem } from '@/api/inventory'
import LineChart from './LineChart.vue'
import PieChart from './PieChart.vue'

interface Props {
  type: 'line' | 'pie'
  distribution?: InventoryDistributionItem[]
  trend?: StockTrendItem[]
  trendDays?: number
  loading?: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  trendDaysChange: [days: number]
}>()

const title = computed(() => props.type === 'line' ? '库存趋势' : '库存分布')

function handleTrendDaysChange(event: Event) {
  const target = event.target as HTMLSelectElement
  emit('trendDaysChange', Number(target.value))
}
</script>

<style scoped>
.chart-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 16px;
  height: 240px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  margin: 0;
}

.filter-select {
  padding: 4px 8px;
  border: 1px solid #D1D5DB;
  border-radius: 4px;
  font-size: 12px;
  color: #6B7280;
  background: white;
}

.chart-container {
  flex: 1;
  min-height: 0;
}
</style>
