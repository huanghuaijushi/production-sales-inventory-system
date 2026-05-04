<template>
  <div class="data-card">
    <div class="card-icon" :style="{ backgroundColor: color }">
      <component :is="iconComponent" class="icon-svg" />
    </div>
    <div class="card-content">
      <div class="card-title">{{ title }}</div>
      <div class="card-value">{{ value }}</div>
      <div class="card-subtitle">{{ subtitle }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  CubeIcon,
  ExclamationTriangleIcon,
  ExclamationCircleIcon,
  CheckCircleIcon
} from '@heroicons/vue/24/solid'

interface Props {
  title: string
  value: string
  subtitle: string
  icon: string
  color: string
}

const props = defineProps<Props>()

const iconComponent = computed(() => {
  switch (props.icon) {
    case 'cube': return CubeIcon
    case 'warning': return ExclamationTriangleIcon
    case 'danger': return ExclamationCircleIcon
    case 'success': return CheckCircleIcon
    default: return CubeIcon
  }
})
</script>

<style scoped>
.data-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  height: 120px;
}

.card-icon {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-svg {
  width: 21px;
  height: 21px;
  color: white;
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 4px;
}

.card-value {
  font-size: 26px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 4px;
}

.card-subtitle {
  font-size: 12px;
  color: #9CA3AF;
}
</style>
