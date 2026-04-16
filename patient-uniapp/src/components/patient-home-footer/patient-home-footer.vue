<template>
  <view class="footer-wrap" :style="{ paddingBottom: safeBottom + 'px' }">
    <view class="footer-inner">
      <button class="btn ghost" hover-class="btn-hover" @click="onBack">返回</button>
      <button class="btn primary" :class="{ active: homeActive }" hover-class="btn-hover" @click="onHome">
        首页
      </button>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  homeActive: { type: Boolean, default: true }
})

const sys = uni.getSystemInfoSync()
const safeBottom = computed(() => {
  const v = sys.safeAreaInsets && sys.safeAreaInsets.bottom
  return v != null ? v : 0
})

const emit = defineEmits(['back', 'home'])

function onBack() {
  emit('back')
}

function onHome() {
  emit('home')
}
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.footer-wrap {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -8rpx 32rpx rgba(15, 23, 42, 0.06);
  border-top: 1rpx solid #eef2f7;
  z-index: 50;
}

.footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 32rpx 12rpx;
  gap: 24rpx;
}

.btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 999rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
}

.btn::after {
  border: none;
}

.ghost {
  background: #ffffff;
  color: $text-secondary;
  border: 2rpx solid #e5e7eb !important;
}

.primary {
  background: $color-mint;
  color: $color-teal-dark;
}

.primary.active {
  background: linear-gradient(135deg, $color-teal 0%, $color-teal-dark 100%);
  color: #ffffff;
}

.btn-hover {
  opacity: 0.9;
}
</style>
