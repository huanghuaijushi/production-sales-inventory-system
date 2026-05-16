<template>
  <Teleport to="body">
    <div class="toast-container" role="status" aria-live="polite">
      <TransitionGroup name="toast">
        <div
          v-for="item in store.toasts"
          :key="item.id"
          class="toast-item"
          :class="`toast-item--${item.type}`"
          @click="store.dismiss(item.id)"
        >
          <span class="toast-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <path v-if="item.type === 'success'" d="M5 12.5 9.2 16.8 19 7" />
              <path v-else-if="item.type === 'error'" d="M6 6l12 12M18 6 6 18" />
              <path v-else-if="item.type === 'warning'" d="M12 4 22 20H2L12 4Zm0 7v4m0 3v0" />
              <path v-else d="M12 8v0m0 4v4" />
            </svg>
          </span>
          <span class="toast-message">{{ item.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { useToastStore } from '@/stores/toast'

const store = useToastStore()
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1100;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast-item {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 240px;
  max-width: min(80vw, 460px);
  padding: 12px 16px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 14px 38px rgba(15, 23, 42, 0.18);
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  cursor: pointer;
  pointer-events: auto;
  border-left: 4px solid #cbd5e1;
}

.toast-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.toast-icon svg {
  width: 20px;
  height: 20px;
  fill: none;
  stroke-width: 2.2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.toast-message {
  line-height: 1.5;
  word-break: break-word;
}

.toast-item--success {
  border-left-color: #16a34a;
}
.toast-item--success .toast-icon {
  color: #16a34a;
  stroke: #16a34a;
}

.toast-item--error {
  border-left-color: #dc2626;
}
.toast-item--error .toast-icon {
  color: #dc2626;
  stroke: #dc2626;
}

.toast-item--warning {
  border-left-color: #d97706;
}
.toast-item--warning .toast-icon {
  color: #d97706;
  stroke: #d97706;
}

.toast-item--info {
  border-left-color: #2563eb;
}
.toast-item--info .toast-icon {
  color: #2563eb;
  stroke: #2563eb;
}

.toast-enter-active,
.toast-leave-active {
  transition: transform 0.22s ease, opacity 0.22s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(0.96);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.98);
}
</style>
