import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface ToastItem {
  id: number
  type: ToastType
  message: string
  duration: number
}

export interface ToastOptions {
  duration?: number
}

const DEFAULT_DURATION = 3000

export const useToastStore = defineStore('toast', () => {
  const toasts = ref<ToastItem[]>([])
  let seed = 0
  const timers = new Map<number, number>()

  function push(type: ToastType, message: string, options: ToastOptions = {}) {
    const id = ++seed
    const duration = options.duration ?? DEFAULT_DURATION
    toasts.value.push({ id, type, message, duration })
    if (duration > 0) {
      const timer = window.setTimeout(() => dismiss(id), duration)
      timers.set(id, timer)
    }
    return id
  }

  function dismiss(id: number) {
    const timer = timers.get(id)
    if (timer) {
      window.clearTimeout(timer)
      timers.delete(id)
    }
    toasts.value = toasts.value.filter((item) => item.id !== id)
  }

  function success(message: string, options?: ToastOptions) {
    return push('success', message, options)
  }

  function error(message: string, options?: ToastOptions) {
    return push('error', message, options)
  }

  function warning(message: string, options?: ToastOptions) {
    return push('warning', message, options)
  }

  function info(message: string, options?: ToastOptions) {
    return push('info', message, options)
  }

  return { toasts, push, dismiss, success, error, warning, info }
})
