<template>
  <div class="empty-state" :class="size === 'compact' ? 'empty-state--compact' : ''">
    <div class="empty-state__icon" aria-hidden="true">
      <svg viewBox="0 0 64 64" focusable="false">
        <rect x="10" y="18" width="44" height="32" rx="4" />
        <path d="M10 26h44" />
        <path d="M22 18v-4a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v4" />
        <path d="M28 38h8" />
      </svg>
    </div>
    <h3 class="empty-state__title">{{ title }}</h3>
    <p v-if="description" class="empty-state__description">{{ description }}</p>
    <button
      v-if="actionLabel"
      type="button"
      class="empty-state__action"
      @click="$emit('action')"
    >
      {{ actionLabel }}
    </button>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string
  description?: string
  actionLabel?: string
  size?: 'default' | 'compact'
}

withDefaults(defineProps<Props>(), {
  size: 'default'
})

defineEmits<{ action: [] }>()
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 24px;
  color: #475569;
  text-align: center;
}

.empty-state--compact {
  padding: 28px 20px;
  gap: 8px;
}

.empty-state__icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.empty-state--compact .empty-state__icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
}

.empty-state__icon svg {
  width: 30px;
  height: 30px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.empty-state--compact .empty-state__icon svg {
  width: 24px;
  height: 24px;
}

.empty-state__title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.empty-state__description {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  max-width: 320px;
  line-height: 1.6;
}

.empty-state__action {
  margin-top: 4px;
  height: 36px;
  padding: 0 18px;
  border: none;
  border-radius: 8px;
  background: #2563eb;
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  transition: background 0.18s ease;
}

.empty-state__action:hover {
  background: #1d4ed8;
}
</style>
