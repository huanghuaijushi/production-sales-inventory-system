<template>
  <div class="quick-actions-card">
    <h3 class="card-title">快捷操作</h3>
    <div class="actions-grid">
      <button
        class="action-btn"
        v-for="action in actions"
        :key="action.id"
        type="button"
        @click="handleAction(action.type)"
      >
        <component :is="action.iconComponent" class="action-icon" />
        <div class="action-text">{{ action.text }}</div>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  ArrowDownIcon,
  ArrowUpIcon,
  ArrowPathIcon,
  ChartBarIcon
} from '@heroicons/vue/24/outline'

type QuickActionType = 'inbound' | 'outbound' | 'stocktake' | 'report'

const emit = defineEmits<{
  (event: 'inbound'): void
  (event: 'outbound'): void
  (event: 'stocktake'): void
  (event: 'report'): void
}>()

const actions = ref([
  { id: 1, iconComponent: ArrowDownIcon, text: '新增入库', type: 'inbound' as const },
  { id: 2, iconComponent: ArrowUpIcon, text: '新增出库', type: 'outbound' as const },
  { id: 3, iconComponent: ArrowPathIcon, text: '盘点', type: 'stocktake' as const },
  { id: 4, iconComponent: ChartBarIcon, text: '报表', type: 'report' as const }
])

function handleAction(type: QuickActionType) {
  if (type === 'inbound') {
    emit('inbound')
    return
  }
  if (type === 'outbound') {
    emit('outbound')
    return
  }
  if (type === 'stocktake') {
    emit('stocktake')
    return
  }
  emit('report')
}
</script>

<style scoped>
.quick-actions-card {
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

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 16px;
}

.actions-grid {
  display: flex;
  gap: 20px;
  justify-content: space-around;
  flex: 1;
  align-items: center;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 16px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.action-btn:hover {
  background: #F3F4F6;
}

.action-icon {
  width: 32px;
  height: 32px;
  color: #6B7280;
}

.action-text {
  font-size: 14px;
  color: #6B7280;
}
</style>
