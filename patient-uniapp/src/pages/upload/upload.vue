<template>
  <view class="page">
    <scroll-view scroll-y class="scroll" :show-scrollbar="false">
      <diagnosis-archive-form @success="onSuccess" />
      <view class="to-archives" hover-class="to-archives-h" @click="goArchives">
        <text class="to-archives-t">查看已上传档案</text>
        <text class="to-archives-a">›</text>
      </view>
      <view class="bottom-pad" />
    </scroll-view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import DiagnosisArchiveForm from '../../components/diagnosis-archive-form/diagnosis-archive-form.vue'
import { clearAuth, getToken, getUserRole, setUserRole } from '../../utils/auth.js'
import { parseJwtPayload } from '../../utils/jwt.js'

onShow(() => {
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
  if (role !== 'patient') {
    clearAuth()
    uni.showToast({ title: '请使用患者端登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/gate/gate' }), 400)
  }
})

function goArchives() {
  uni.navigateTo({ url: '/pages/patient/archives' })
}

function onSuccess() {
  setTimeout(() => {
    uni.navigateTo({ url: '/pages/patient/archives' })
  }, 600)
}
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.page {
  min-height: 100vh;
  background: $upload-page-bg;
}

.scroll {
  height: 100vh;
  box-sizing: border-box;
}

.to-archives {
  margin: 0 28rpx 8rpx;
  padding: 24rpx 20rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.75);
  border-radius: 16rpx;
  border: 2rpx solid $upload-border;
}

.to-archives-h {
  opacity: 0.9;
}

.to-archives-t {
  font-size: 28rpx;
  font-weight: 700;
  color: $upload-teal;
}

.to-archives-a {
  font-size: 36rpx;
  color: $upload-teal-soft;
  font-weight: 300;
}

.bottom-pad {
  height: 48rpx;
}
</style>
