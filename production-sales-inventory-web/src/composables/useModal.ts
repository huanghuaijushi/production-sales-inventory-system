import { computed, ref } from 'vue'

export function useModal<T extends string>() {
  const active = ref<T | null>(null)

  function open(name: T) {
    active.value = name
  }

  function close() {
    active.value = null
  }

  function isOpen(name: T) {
    return computed(() => active.value === name)
  }

  const isAnyOpen = computed(() => active.value !== null)

  return { active, open, close, isOpen, isAnyOpen }
}
