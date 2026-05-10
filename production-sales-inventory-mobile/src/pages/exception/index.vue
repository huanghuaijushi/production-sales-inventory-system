<template>
  <view class="container exception-page">
    <view class="hero card">
      <text class="hero__eyebrow">Exception Report</text>
      <text class="hero__title">异常上报</text>
      <text class="hero__subtitle">用于现场快速记录物料异常、数量异常、质量异常和设备异常。</text>
    </view>

    <view class="card section-card">
      <view class="section-head">
        <text class="section-title">1. 异常信息</text>
        <text class="section-tip">带 * 为必填项</text>
      </view>

      <view class="field-group">
        <text class="field-label">异常类型 *</text>
        <view class="segmented">
          <view
            v-for="option in typeOptions"
            :key="option.value"
            class="segmented__item"
            :class="{ 'segmented__item--active': form.type === option.value }"
            @click="form.type = option.value"
          >
            {{ option.label }}
          </view>
        </view>
      </view>

      <view class="field-group">
        <text class="field-label">关联单号 / 物料编码</text>
        <input v-model.trim="form.referenceNo" class="form-input mobile-input" placeholder="可填写单号或编码" />
      </view>

      <view class="field-group">
        <text class="field-label">异常描述 *</text>
        <textarea v-model.trim="form.description" class="remark-input" placeholder="请尽量描述清楚发生了什么" :maxlength="300" />
      </view>

      <view class="field-group">
        <text class="field-label">处理建议</text>
        <textarea v-model.trim="form.suggestion" class="remark-input" placeholder="可填写建议处理方式" :maxlength="200" />
      </view>
    </view>

    <view class="card section-card">
      <view class="section-head">
        <text class="section-title">2. 附加说明</text>
        <text class="section-tip">后续可扩展图片上传</text>
      </view>

      <view class="field-group">
        <text class="field-label">备注</text>
        <textarea v-model.trim="form.remark" class="remark-input" placeholder="可补充现场背景信息" :maxlength="200" />
      </view>
    </view>

    <view class="card notice-card" v-if="errorMessage">
      <text class="notice-card__text">{{ errorMessage }}</text>
    </view>

    <view class="card success-card" v-if="successMessage">
      <text class="success-card__text">{{ successMessage }}</text>
    </view>

    <view class="action-bar">
      <button class="secondary-button action-bar__btn" :disabled="submitting" @click="resetForm">重置</button>
      <button class="primary-button action-bar__btn" :disabled="submitting" @click="handleSubmit">
        {{ submitting ? '提交中...' : '提交异常' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'

type ExceptionType = 'MATERIAL' | 'QUANTITY' | 'QUALITY' | 'EQUIPMENT' | 'OTHER'

const typeOptions: Array<{ value: ExceptionType; label: string }> = [
  { value: 'MATERIAL', label: '物料异常' },
  { value: 'QUANTITY', label: '数量异常' },
  { value: 'QUALITY', label: '质量异常' },
  { value: 'EQUIPMENT', label: '设备异常' },
  { value: 'OTHER', label: '其他异常' }
]

const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const form = reactive({
  type: 'MATERIAL' as ExceptionType,
  referenceNo: '',
  description: '',
  suggestion: '',
  remark: ''
})

function resetForm() {
  form.type = 'MATERIAL'
  form.referenceNo = ''
  form.description = ''
  form.suggestion = ''
  form.remark = ''
  errorMessage.value = ''
  successMessage.value = ''
}

function validateForm() {
  if (!form.description.trim()) {
    errorMessage.value = '请输入异常描述'
    return false
  }

  errorMessage.value = ''
  return true
}

async function handleSubmit() {
  if (submitting.value || !validateForm()) {
    return
  }

  submitting.value = true
  successMessage.value = ''

  try {
    await new Promise((resolve) => setTimeout(resolve, 600))
    successMessage.value = '异常已提交，后续可接入后端接口'
    uni.showToast({ title: '提交成功', icon: 'success' })
    resetForm()
  } catch {
    errorMessage.value = '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.exception-page {
  padding-bottom: 140rpx;
}

.hero {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 24rpx;
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
  color: #ffffff;
}

.hero__eyebrow {
  width: fit-content;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.16);
  font-size: 22rpx;
  font-weight: 700;
}

.hero__title {
  font-size: 48rpx;
  font-weight: 800;
}

.hero__subtitle {
  font-size: 26rpx;
  line-height: 1.7;
  opacity: 0.92;
}

.section-card {
  margin-bottom: 24rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  align-items: flex-end;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #0f172a;
}

.section-tip {
  font-size: 24rpx;
  color: #94a3b8;
}

.field-group {
  margin-bottom: 24rpx;
}

.field-label {
  display: block;
  margin-bottom: 14rpx;
  font-size: 26rpx;
  color: #475569;
}

.mobile-input,
.remark-input {
  width: 100%;
  border-radius: 20rpx;
  border: 2rpx solid #dbe3f0;
  background: #f8fafc;
  font-size: 28rpx;
}

.mobile-input {
  height: 88rpx;
  padding: 0 24rpx;
}

.remark-input {
  min-height: 168rpx;
  padding: 24rpx;
}

.segmented {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
}

.segmented__item {
  min-height: 80rpx;
  padding: 16rpx 12rpx;
  border-radius: 18rpx;
  background: #f8fafc;
  border: 2rpx solid #dbe3f0;
  font-size: 24rpx;
  text-align: center;
  color: #334155;
}

.segmented__item--active {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border-color: transparent;
  color: #ffffff;
  font-weight: 700;
}

.notice-card {
  margin-bottom: 20rpx;
  background: #fff7ed;
  border: 2rpx solid #fdba74;
}

.notice-card__text {
  font-size: 26rpx;
  color: #c2410c;
}

.success-card {
  margin-bottom: 20rpx;
  background: #f0fdf4;
  border: 2rpx solid #86efac;
}

.success-card__text {
  font-size: 26rpx;
  color: #15803d;
}

.action-bar {
  position: fixed;
  left: 32rpx;
  right: 32rpx;
  bottom: 28rpx;
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 18rpx;
  padding: 18rpx;
  border-radius: 28rpx;
  background: rgba(245, 247, 251, 0.92);
  backdrop-filter: blur(14px);
  box-shadow: 0 -8rpx 28rpx rgba(15, 23, 42, 0.08);
}

.action-bar__btn {
  height: 88rpx;
  line-height: 88rpx;
}
</style>
