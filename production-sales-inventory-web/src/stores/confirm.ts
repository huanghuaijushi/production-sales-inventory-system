import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ConfirmTone = 'default' | 'danger'

export interface ConfirmOptions {
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
  tone?: ConfirmTone
}

interface ConfirmState extends Required<Pick<ConfirmOptions, 'message'>> {
  title: string
  confirmText: string
  cancelText: string
  tone: ConfirmTone
  resolve: ((value: boolean) => void) | null
}

const defaultState = (): ConfirmState => ({
  title: '请确认操作',
  message: '',
  confirmText: '确认',
  cancelText: '取消',
  tone: 'default',
  resolve: null
})

export const useConfirmStore = defineStore('confirm', () => {
  const visible = ref(false)
  const state = ref<ConfirmState>(defaultState())

  function open(options: ConfirmOptions): Promise<boolean> {
    return new Promise((resolve) => {
      state.value = {
        title: options.title ?? '请确认操作',
        message: options.message,
        confirmText: options.confirmText ?? '确认',
        cancelText: options.cancelText ?? '取消',
        tone: options.tone ?? 'default',
        resolve
      }
      visible.value = true
    })
  }

  function resolve(value: boolean) {
    state.value.resolve?.(value)
    visible.value = false
  }

  function confirm() {
    resolve(true)
  }

  function cancel() {
    resolve(false)
  }

  return { visible, state, open, confirm, cancel }
})
