<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onLaunch(async () => {
  const restored = await authStore.restoreSession()

  if (restored) {
    uni.reLaunch({ url: '/pages/home/index' })
    return
  }

  uni.reLaunch({ url: '/pages/login/index' })
})

onShow(() => {
  // App show lifecycle reserved for later message refresh and version checking.
})
</script>

<style lang="scss">
@use './styles/global.scss';
</style>
