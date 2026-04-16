<template>
  <view class="page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }" />
    <view class="top-pad">
      <!-- 用户卡片（登录用户资料来自 /api/patient/me） -->
      <view class="card profile">
        <view class="avatar-wrap">
          <text class="avatar-text">{{ nameInitial }}</text>
        </view>
        <view class="profile-main">
          <view class="row-top">
            <text class="name">{{ displayName }}</text>
            <view class="chip" :class="chipState.cls">{{ chipState.text }}</view>
          </view>
          <text class="sub-id">{{ displaySub }}</text>
          <text v-if="me && me.age != null" class="sub-extra">年龄 {{ me.age }} 岁</text>
        </view>
      </view>
      <view v-if="dashboardLoading" class="card tip-card">
        <text class="tip-t">正在同步账户信息…</text>
      </view>

      <!-- 绑定与就诊进度（数据来自 /api/patient/binds） -->
      <view class="card section">
        <view class="section-head">
          <view class="pulse" />
          <text class="section-title">我的医生与进度</text>
        </view>
        <view class="kv">
          <text class="k">主治医生</text>
          <text class="v">{{ visitLine.doctor }}</text>
        </view>
        <view class="kv">
          <text class="k">进度说明</text>
          <text class="v">{{ visitLine.topic }}</text>
        </view>
        <view class="kv">
          <text class="k">最近更新</text>
          <text class="v">{{ visitLine.updated }}</text>
        </view>
        <view v-if="!hasApprovedBind" class="bind-cta" @click="goBindDoctor">
          <text class="bind-cta-t">去绑定 / 管理医生</text>
          <text class="bind-cta-arrow">›</text>
        </view>
      </view>

      <!-- 快捷功能 -->
      <view class="section-title-row">
        <text class="section-title-strong">快捷功能</text>
      </view>
      <view class="grid">
        <view class="tile" v-for="item in quick" :key="item.key" hover-class="tile-hover" @click="go(item)">
          <view class="icon-circle" :style="{ background: item.tint }">
            <text class="icon-emoji">{{ item.icon }}</text>
          </view>
          <text class="tile-text">{{ item.title }}</text>
        </view>
      </view>

      <view class="bottom-spacer" />
    </view>

    <patient-home-footer :home-active="true" @back="onBack" @home="onHome" />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import PatientHomeFooter from '../../components/patient-home-footer/patient-home-footer.vue'
import { clearAuth, getUserRole, isLoggedIn, setUserRole, getToken } from '../../utils/auth.js'
import { parseJwtPayload } from '../../utils/jwt.js'
import { fetchPatientMe, listPatientBinds } from '../../utils/patientApi.js'
import { getStatusBarSafeTopPx } from '../../utils/safeArea.js'

const statusBarHeight = ref(getStatusBarSafeTopPx())

function refreshSafeTop() {
  statusBarHeight.value = getStatusBarSafeTopPx()
}

const me = ref(null)
const binds = ref([])
const dashboardLoading = ref(false)

const displayName = computed(() => (me.value && me.value.name) || '患者')

const displaySub = computed(() => {
  const m = me.value
  if (!m) return '登录后同步资料'
  const phone = m.phoneMask || '—'
  return `手机 ${phone} · 用户 ${m.userId}`
})

const nameInitial = computed(() => {
  const n = displayName.value || ''
  return n.length ? n.slice(0, 1) : '患'
})

function sortBindsDesc(list) {
  return [...(list || [])].sort((a, b) => {
    const ta = new Date(a.updatedAt || a.createdAt || 0).getTime()
    const tb = new Date(b.updatedAt || b.createdAt || 0).getTime()
    return tb - ta
  })
}

function fmtDay(iso) {
  if (!iso) return '—'
  const s = String(iso)
  return s.length >= 10 ? s.slice(0, 10) : s
}

const chipState = computed(() => {
  const list = binds.value || []
  if (list.some((b) => b.status === 1)) {
    return { text: '已绑定', cls: 'chip-ok' }
  }
  if (list.some((b) => b.status === 0)) {
    return { text: '审核中', cls: 'chip-wait' }
  }
  return { text: '未绑定', cls: 'chip-idle' }
})

/** 已有审核通过的医患绑定：隐藏「去绑定 / 管理医生」入口（在线咨询仍保留） */
const hasApprovedBind = computed(() => (binds.value || []).some((b) => b.status === 1))

const visitLine = computed(() => {
  const sorted = sortBindsDesc(binds.value)
  const approved = sorted.find((b) => b.status === 1)
  if (approved) {
    return {
      doctor: approved.doctorName || '—',
      topic: '医患绑定已生效，可开始使用相关服务',
      updated: fmtDay(approved.updatedAt)
    }
  }
  const pending = sorted.find((b) => b.status === 0)
  if (pending) {
    return {
      doctor: pending.doctorName || '—',
      topic: '已向医生发起申请，等待审核',
      updated: fmtDay(pending.updatedAt)
    }
  }
  return {
    doctor: '—',
    topic: '尚未绑定医生，请点击下方入口申请',
    updated: '—'
  }
})

const quick = computed(() => [
  { key: 'upload', title: '上传诊断档案', icon: '☁', tint: 'linear-gradient(135deg,#ccfbf1,#99f6e4)', path: '/pages/upload/upload' },
  { key: 'records', title: '就诊记录', icon: '🕒', tint: 'linear-gradient(135deg,#fef3c7,#fde68a)', path: '/pages/records/records' },
  {
    key: 'consult',
    title: '在线咨询',
    icon: '💬',
    tint: 'linear-gradient(135deg,#e0f2fe,#bae6fd)',
    path: '/pages/chat/chat'
  },
  { key: 'profile', title: '个人中心', icon: '👤', tint: 'linear-gradient(135deg,#ede9fe,#ddd6fe)', path: '/pages/profile/profile' }
])

onLoad(() => {
  refreshSafeTop()
})

async function loadPatientDashboard() {
  dashboardLoading.value = true
  try {
    const [m, b] = await Promise.all([fetchPatientMe(), listPatientBinds()])
    me.value = m
    binds.value = b
  } catch (e) {
    me.value = null
    binds.value = []
    uni.showToast({ title: e.message || '加载患者信息失败', icon: 'none' })
  } finally {
    dashboardLoading.value = false
  }
}

onShow(() => {
  refreshSafeTop()
  if (!isLoggedIn()) {
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
    return
  }
  loadPatientDashboard()
})

function goBindDoctor() {
  uni.navigateTo({ url: '/pages/patient/bind-doctor' })
}

function go(item) {
  if (!item.path) return
  uni.navigateTo({ url: item.path })
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
}

.avatar-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 999rpx;
  overflow: hidden;
  margin-right: 22rpx;
  background: linear-gradient(135deg, #99f6e4 0%, $color-teal 100%);
  border: 6rpx solid #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(13, 148, 136, 0.15);
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
  font-size: 36rpx;
  font-weight: 800;
  color: $text-primary;
}

.chip {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.chip.chip-ok {
  background: #d1fae5;
  color: #047857;
}

.chip.chip-wait {
  background: #fef3c7;
  color: #b45309;
}

.chip.chip-idle {
  background: #f1f5f9;
  color: #64748b;
}

.sub-id {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: $text-secondary;
}

.sub-extra {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #94a3b8;
}

.tip-card {
  padding: 20rpx 28rpx;
}

.tip-t {
  font-size: 26rpx;
  color: $text-secondary;
}

.bind-cta {
  margin-top: 18rpx;
  padding: 20rpx 22rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.bind-cta-t {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f766e;
}

.bind-cta-arrow {
  font-size: 36rpx;
  color: #14b8a6;
  font-weight: 700;
}

.section-head {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 18rpx;
}

.pulse {
  width: 14rpx;
  height: 14rpx;
  border-radius: 999rpx;
  background: $color-teal;
  box-shadow: 0 0 0 8rpx rgba(20, 184, 166, 0.18);
}

.section-title {
  font-size: 30rpx;
  font-weight: 800;
  color: $text-primary;
}

.kv {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
  padding: 14rpx 0;
  border-top: 1rpx solid #f3f4f6;
}

.kv:first-of-type {
  border-top: none;
  padding-top: 0;
}

.k {
  font-size: 26rpx;
  color: $text-secondary;
}

.v {
  max-width: 420rpx;
  text-align: right;
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 600;
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

.icon-circle {
  width: 112rpx;
  height: 112rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14rpx;
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
</style>
