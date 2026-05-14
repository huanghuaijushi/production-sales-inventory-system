<template>
  <view v-if="!networkStore.isOnline" class="network-banner">
    <text class="network-banner__dot">●</text>
    <text class="network-banner__text">网络已断开，提交将失败</text>
  </view>
</template>

<script setup lang="ts">
import { useNetworkStore } from '@/stores/network'

const networkStore = useNetworkStore()
</script>

<style scoped lang="scss">
.network-banner {
  position: fixed;
  top: env(safe-area-inset-top);
  left: 0;
  right: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 14rpx 24rpx;
  background: #b91c1c;
  color: #ffffff;
  font-size: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(185, 28, 28, 0.3);
}

.network-banner__dot {
  font-size: 18rpx;
  color: #fef2f2;
  animation: pulse 1.4s ease-in-out infinite;
}

.network-banner__text {
  font-weight: 600;
  letter-spacing: 0.02em;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.4;
  }
  50% {
    opacity: 1;
  }
}
</style>
