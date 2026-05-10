<template>
  <view class="field-block">
    <text class="field-block__label">{{ label }}</text>
    <slot />
    <text v-if="error" class="field-block__error">{{ error }}</text>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  error?: string
}>()
</script>

<style scoped lang="scss">
.field-block {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.field-block__label {
  font-size: 26rpx;
  color: #475569;
}

.field-block__error {
  font-size: 24rpx;
  color: #dc2626;
}
</style>
