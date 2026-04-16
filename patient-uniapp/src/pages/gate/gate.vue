<template>
  <view class="page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }" />
    <view class="hero">
      <view class="brand">整形医院</view>
      <view class="title">请选择登录入口</view>
      <view class="sub">患者与医生使用不同工作台，请按身份进入</view>
    </view>

    <view class="entries">
      <view class="entry card-patient" hover-class="entry-hover" @click="goPatient">
        <view class="entry-icon patient">患</view>
        <view class="entry-body">
          <text class="entry-title">患者端登录</text>
          <text class="entry-desc">查询档案、上传资料、咨询医生</text>
        </view>
        <text class="chevron">›</text>
      </view>

      <view class="entry card-doctor" hover-class="entry-hover" @click="goDoctor">
        <view class="entry-icon doctor">医</view>
        <view class="entry-body">
          <text class="entry-title">医生端登录</text>
          <text class="entry-desc">患者管理、诊疗与随访</text>
        </view>
        <text class="chevron">›</text>
      </view>
    </view>

    <view class="foot-hint">若账号无法登录，请联系管理员开通权限</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { getStatusBarSafeTopPx } from '../../utils/safeArea.js'

const statusBarHeight = ref(getStatusBarSafeTopPx())

function goPatient() {
  uni.navigateTo({ url: '/pages/login/patient-login' })
}

function goDoctor() {
  uni.navigateTo({ url: '/pages/login/doctor-login' })
}
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #ecfdf5 0%, $bg-page 45%, $bg-page 100%);
  box-sizing: border-box;
}
/* #ifdef H5 */
.page {
  min-height: 100vh;
  width: 100%;
}
/* #endif */

.status-bar {
  width: 100%;
}

.hero {
  padding: 56rpx 40rpx 36rpx;
}

.brand {
  font-size: 26rpx;
  font-weight: 700;
  color: $color-teal-dark;
  letter-spacing: 2rpx;
  margin-bottom: 16rpx;
}

.title {
  font-size: 44rpx;
  font-weight: 900;
  color: $text-primary;
}

.sub {
  margin-top: 14rpx;
  font-size: 26rpx;
  color: $text-secondary;
  line-height: 1.5;
}

.entries {
  padding: 0 28rpx;
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.entry {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 28rpx 24rpx;
  border-radius: $radius-card;
  background: $card-white;
  box-shadow: $shadow-card;
}

.entry-hover {
  opacity: 0.96;
  transform: scale(0.998);
}

.card-patient {
  border: 2rpx solid rgba(20, 184, 166, 0.18);
}

.card-doctor {
  border: 2rpx solid rgba(59, 130, 246, 0.2);
}

.entry-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: 900;
  color: #ffffff;
  margin-right: 20rpx;
}

.entry-icon.patient {
  background: linear-gradient(135deg, $color-teal 0%, $color-teal-dark 100%);
}

.entry-icon.doctor {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
}

.entry-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.entry-title {
  font-size: 32rpx;
  font-weight: 800;
  color: $text-primary;
}

.entry-desc {
  font-size: 24rpx;
  color: $text-secondary;
  line-height: 1.35;
}

.chevron {
  font-size: 44rpx;
  color: #d1d5db;
  font-weight: 300;
  padding-left: 8rpx;
}

.foot-hint {
  margin-top: 36rpx;
  padding: 0 40rpx;
  font-size: 22rpx;
  color: #9ca3af;
  text-align: center;
  line-height: 1.5;
}
</style>
