import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNetworkStore = defineStore('network', () => {
  const isOnline = ref(true)
  const networkType = ref<string>('unknown')
  let initialized = false

  function init() {
    if (initialized) return
    initialized = true

    uni.getNetworkType({
      success: (res) => {
        networkType.value = res.networkType
        isOnline.value = res.networkType !== 'none'
      }
    })

    uni.onNetworkStatusChange((res) => {
      networkType.value = res.networkType
      isOnline.value = res.isConnected
    })
  }

  return {
    isOnline,
    networkType,
    init
  }
})
