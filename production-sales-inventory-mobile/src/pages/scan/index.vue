<template>
  <view class="container">
    <view class="card placeholder">
      <text class="page-title">扫码</text>
      <text class="page-subtitle">一期先预留扫码入口，下一步接入 uni.scanCode 并调用后端解析接口。</text>
      <button class="primary-button" @click="showPending">开始扫码</button>
    </view>
  </view>
</template>

<script setup lang="ts">
function showPending() {
  uni.showToast({ title: '扫码功能下一步接入', icon: 'none' })
}
</script>

<style scoped lang="scss">
.placeholder {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}
</style>
