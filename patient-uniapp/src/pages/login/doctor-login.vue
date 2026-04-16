<template>
  <view class="page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }" />
    <view class="nav-row">
      <text class="nav-back" @click="goBack">‹ 返回</text>
    </view>
    <view class="hero">
      <view class="hero-badge doctor">医生端</view>
      <view class="hero-title">医生工作台</view>
      <view class="hero-sub">使用手机号与密码登录</view>
    </view>

    <view class="card">
      <view class="field">
        <text class="label">手机号</text>
        <input
          class="input"
          type="number"
          maxlength="11"
          placeholder="请输入手机号"
          placeholder-class="ph"
          v-model="phone"
        />
      </view>
      <view class="field">
        <text class="label">密码</text>
        <input class="input" password placeholder="请输入密码" placeholder-class="ph" v-model="password" />
      </view>

      <button
        class="btn-login"
        :loading="loading"
        :disabled="loading"
        hover-class="btn-login-hover"
        @click="onLogin"
      >
        登录
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { DEV_PREFILL_LOGIN, DEV_LOGIN_DOCTOR } from '../../utils/config.js'
import { loginWithPassword } from '../../utils/loginHelper.js'
import { getStatusBarSafeTopPx } from '../../utils/safeArea.js'

const statusBarHeight = ref(getStatusBarSafeTopPx())
const phone = ref(DEV_PREFILL_LOGIN ? DEV_LOGIN_DOCTOR.phone : '')
const password = ref(DEV_PREFILL_LOGIN ? DEV_LOGIN_DOCTOR.password : '')
const loading = ref(false)

function goBack() {
  uni.navigateBack({ fail: () => uni.reLaunch({ url: '/pages/gate/gate' }) })
}

async function onLogin() {
  const p = (phone.value || '').trim()
  const w = (password.value || '').trim()
  if (!/^1\d{10}$/.test(p)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  if (!w) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await loginWithPassword(p, w, 'doctor')
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/doctor/home' })
    }, 200)
  } catch (e) {
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #dbeafe 0%, $bg-page 42%, $bg-page 100%);
}

.status-bar {
  width: 100%;
}

.nav-row {
  padding: 8rpx 28rpx 0;
}

.nav-back {
  font-size: 28rpx;
  color: $text-secondary;
  font-weight: 600;
}

.hero {
  padding: 24rpx 40rpx 24rpx;
}

.hero-badge {
  display: inline-block;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.85);
  color: #1d4ed8;
  font-size: 22rpx;
  font-weight: 600;
  margin-bottom: 18rpx;
}

.hero-title {
  font-size: 48rpx;
  font-weight: 800;
  color: $text-primary;
}

.hero-sub {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: $text-secondary;
}

.card {
  margin: 8rpx 28rpx 0;
  padding: 36rpx 28rpx 40rpx;
  background: $card-white;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
}

.field {
  margin-bottom: 28rpx;
}

.label {
  display: block;
  font-size: 24rpx;
  color: $text-secondary;
  margin-bottom: 12rpx;
}

.input {
  height: 92rpx;
  padding: 0 24rpx;
  border-radius: 18rpx;
  background: #f9fafb;
  border: 2rpx solid #eef2f7;
  font-size: 30rpx;
  color: $text-primary;
}

.ph {
  color: #9ca3af;
}

.btn-login {
  margin-top: 12rpx;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 700;
  border: none;
}

.btn-login::after {
  border: none;
}

.btn-login-hover {
  opacity: 0.92;
}
</style>
