<template>
  <view class="page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }" />
    <view class="top-pad">
      <view class="card profile">
        <view class="avatar-wrap">
          <text class="avatar-text">医</text>
        </view>
        <view class="profile-main">
          <view class="row-top">
            <text class="name">医生工作台</text>
            <view class="chip">已登录</view>
          </view>
          <text class="sub-id">后续可接入医生姓名、科室与工号</text>
        </view>
      </view>

      <view class="section-title-row">
        <text class="section-title-strong">快捷入口</text>
      </view>
      <view class="grid">
        <view class="tile" v-for="item in quick" :key="item.key" hover-class="tile-hover" @click="onQuick(item)">
          <view class="icon-wrap">
            <view class="icon-circle" :style="{ background: item.tint }">
              <text class="icon-emoji">{{ item.icon }}</text>
            </view>
            <view v-if="item.badge > 0" class="tile-badge">{{ item.badge > 99 ? '99+' : item.badge }}</view>
          </view>
          <text class="tile-text">{{ item.title }}</text>
        </view>
      </view>

      <view class="bottom-spacer" />
    </view>

    <view class="footer-wrap" :style="{ paddingBottom: safeBottom + 'px' }">
      <view class="footer-inner">
        <button class="btn ghost" hover-class="btn-hover" @click="onBack">返回</button>
        <button class="btn primary active" hover-class="btn-hover" @click="onHome">首页</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { countPendingDoctorBinds } from '../../utils/doctorBinds.js'
import { clearAuth, getToken, getUserRole, setUserRole } from '../../utils/auth.js'
import { parseJwtPayload } from '../../utils/jwt.js'
import { getStatusBarSafeTopPx } from '../../utils/safeArea.js'

const statusBarHeight = ref(getStatusBarSafeTopPx())

function refreshSafeTop() {
  statusBarHeight.value = getStatusBarSafeTopPx()
}

const sys = uni.getSystemInfoSync()
const safeBottom = computed(() => {
  const v = sys.safeAreaInsets && sys.safeAreaInsets.bottom
  return v != null ? v : 0
})

const pendingPatientCount = ref(0)

const quick = computed(() => [
  {
    key: 'patients',
    title: '患者管理',
    icon: '👥',
    tint: 'linear-gradient(135deg,#ccfbf1,#99f6e4)',
    badge: pendingPatientCount.value
  },
  { key: 'msg', title: '在线咨询', icon: '💬', tint: 'linear-gradient(135deg,#e0f2fe,#bae6fd)', badge: 0 },
  {
    key: 'records',
    title: '诊断记录',
    icon: '📋',
    tint: 'linear-gradient(135deg,#d1fae5,#6ee7b7)',
    badge: 0
  },
  { key: 'me', title: '设置', icon: '⚙', tint: 'linear-gradient(135deg,#fef3c7,#fde68a)', badge: 0 }
])

async function refreshPendingBadge() {
  try {
    pendingPatientCount.value = await countPendingDoctorBinds()
  } catch {
    pendingPatientCount.value = 0
  }
}

onLoad(() => {
  refreshSafeTop()
})

onShow(() => {
  refreshSafeTop()
  if (!getToken()) {
    uni.reLaunch({ url: '/pages/gate/gate' })
    return
  }
  let role = getUserRole()
  if (!role && getToken()) {
    const p = parseJwtPayload(getToken())
    if (p?.role) {
      setUserRole(p.role)
      role = p.role
    }
  }
  if (role !== 'doctor') {
    clearAuth()
    uni.showToast({ title: '请使用医生端登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/gate/gate' }), 400)
    return
  }
  refreshPendingBadge()
})

function onQuick(item) {
  if (item.key === 'patients') {
    uni.navigateTo({ url: '/pages/doctor/my-patients' })
    return
  }
  if (item.key === 'msg') {
    uni.navigateTo({ url: '/pages/doctor/chat' })
    return
  }
  if (item.key === 'records') {
    uni.navigateTo({ url: '/pages/doctor/records' })
    return
  }
  uni.showToast({ title: `${item.title} 功能开发中`, icon: 'none' })
}

function onHome() {
  uni.pageScrollTo({ scrollTop: 0, duration: 200 })
}

function onBack() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    success: (res) => {
      if (res.confirm) {
        clearAuth()
        uni.reLaunch({ url: '/pages/gate/gate' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 160rpx;
}

.status-bar {
  background: transparent;
}

.top-pad {
  padding: 8rpx 28rpx 0;
}

.card {
  background: $card-white;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  padding: 28rpx;
  margin-bottom: 22rpx;
}

.profile {
  display: flex;
  flex-direction: row;
  align-items: center;
  border: 2rpx solid rgba(59, 130, 246, 0.15);
}

.avatar-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 999rpx;
  margin-right: 22rpx;
  background: linear-gradient(135deg, #93c5fd 0%, #2563eb 100%);
  border: 6rpx solid #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 44rpx;
  font-weight: 900;
  color: #ffffff;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.row-top {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 14rpx;
}

.name {
  font-size: 34rpx;
  font-weight: 800;
  color: $text-primary;
}

.chip {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 22rpx;
  font-weight: 700;
}

.sub-id {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: $text-secondary;
}

.section-title-row {
  margin: 8rpx 4rpx 16rpx;
}

.section-title-strong {
  font-size: 30rpx;
  font-weight: 900;
  color: $text-primary;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 22rpx;
  align-items: stretch;
}

.tile {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  min-width: 0;
  background: $card-white;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  padding: 26rpx 18rpx 22rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.tile-hover {
  transform: translateY(-2rpx);
}

.icon-wrap {
  position: relative;
  margin-bottom: 14rpx;
}

.tile-badge {
  position: absolute;
  top: -6rpx;
  right: -10rpx;
  min-width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  padding: 0 10rpx;
  border-radius: 999rpx;
  background: #ef4444;
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 800;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.35);
}

.icon-circle {
  width: 112rpx;
  height: 112rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-emoji {
  font-size: 44rpx;
}

.tile-text {
  font-size: 26rpx;
  font-weight: 700;
  color: $text-primary;
  text-align: center;
  line-height: 1.25;
}

.bottom-spacer {
  height: 40rpx;
}

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
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: #ffffff;
}

.primary.active {
  opacity: 1;
}

.btn-hover {
  opacity: 0.9;
}
</style>
