<template>
  <Teleport to="body">
    <Transition name="confirm-fade">
      <div v-if="store.visible" class="confirm-overlay" @click.self="store.cancel()">
        <div class="confirm-dialog" role="dialog" aria-modal="true">
          <header class="confirm-header">
            <h2>{{ store.state.title }}</h2>
          </header>
          <p class="confirm-message">{{ store.state.message }}</p>
          <footer class="confirm-footer">
            <button type="button" class="confirm-btn confirm-btn--cancel" @click="store.cancel()">
              {{ store.state.cancelText }}
            </button>
            <button
              type="button"
              class="confirm-btn"
              :class="store.state.tone === 'danger' ? 'confirm-btn--danger' : 'confirm-btn--primary'"
              autofocus
              @click="store.confirm()"
            >
              {{ store.state.confirmText }}
            </button>
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, watch } from 'vue'
import { useConfirmStore } from '@/stores/confirm'

const store = useConfirmStore()

function handleKeydown(event: KeyboardEvent) {
  if (!store.visible) return
  if (event.key === 'Escape') {
    event.preventDefault()
    store.cancel()
  } else if (event.key === 'Enter') {
    event.preventDefault()
    store.confirm()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})

watch(
  () => store.visible,
  (open) => {
    if (open) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
  }
)
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(2px);
}

.confirm-dialog {
  width: min(100%, 420px);
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.25);
  overflow: hidden;
}

.confirm-header {
  padding: 20px 24px 0;
}

.confirm-header h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.confirm-message {
  margin: 12px 24px 24px;
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.confirm-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px;
  background: #f8fafc;
  border-top: 1px solid #eef2f7;
}

.confirm-btn {
  min-width: 80px;
  height: 36px;
  padding: 0 16px;
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.18s ease;
}

.confirm-btn--cancel {
  background: #ffffff;
  color: #334155;
  border-color: #cbd5e1;
}

.confirm-btn--cancel:hover {
  background: #f1f5f9;
}

.confirm-btn--primary {
  background: #2563eb;
  color: #ffffff;
}

.confirm-btn--primary:hover {
  background: #1d4ed8;
}

.confirm-btn--danger {
  background: #dc2626;
  color: #ffffff;
}

.confirm-btn--danger:hover {
  background: #b91c1c;
}

.confirm-fade-enter-active,
.confirm-fade-leave-active {
  transition: opacity 0.18s ease;
}

.confirm-fade-enter-active .confirm-dialog,
.confirm-fade-leave-active .confirm-dialog {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.confirm-fade-enter-from,
.confirm-fade-leave-to {
  opacity: 0;
}

.confirm-fade-enter-from .confirm-dialog,
.confirm-fade-leave-to .confirm-dialog {
  transform: translateY(8px) scale(0.98);
  opacity: 0;
}
</style>
