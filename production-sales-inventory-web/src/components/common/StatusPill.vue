<template>
  <span class="status-pill" :class="`status-pill--${semantic}`">{{ label }}</span>
</template>

<script setup lang="ts">
import type { StatusSemantic } from '@/utils/statusSemantic'

defineProps<{
  semantic: StatusSemantic
  label: string
}>()
</script>

<style scoped>
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 62px;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.status-pill--active {
  color: #2563eb;
  background: #eff6ff;
}

.status-pill--pending {
  color: #d97706;
  background: #fff7ed;
}

.status-pill--completed {
  color: #047857;
  background: #ecfdf5;
}

.status-pill--cancelled {
  color: #64748b;
  background: #f1f5f9;
}

.status-pill--danger {
  color: #dc2626;
  background: #fef2f2;
}
</style>
