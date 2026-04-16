<template>
  <view class="page">
    <view class="tabs">
      <view
        v-for="(t, idx) in tabDefs"
        :key="t.key"
        :class="['tab', activeTab === idx && 'tab-active']"
        @click="switchTab(idx)"
      >
        <text class="tab-text">{{ t.label }}</text>
        <view v-if="t.key === 'pending' && pendingCount > 0" class="tab-badge">
          {{ pendingCount > 99 ? '99+' : pendingCount }}
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="scroll" :show-scrollbar="false">
      <view v-if="loading" class="hint">加载中…</view>
      <template v-else>
        <view v-for="item in list" :key="item.id" class="archive-card">
          <view class="archive-accent" />
          <view class="archive-top">
            <view class="archive-title-row">
              <text class="archive-label">患者管理</text>
              <view :class="['status-chip', chipClass(item.status)]">{{ statusLabel(item.status) }}</view>
            </view>
            <text class="archive-no">档案编号：{{ item.id }}</text>
          </view>

          <view class="archive-body">
            <view class="avatar">
              <text class="avatar-t">{{ nameInitial(item.patientName) }}</text>
            </view>
            <view class="archive-main">
              <text class="p-name">{{ item.patientName || '未命名' }}</text>
              <view class="kv">
                <text class="kv-k">性别</text>
                <text class="kv-v">{{ genderText(item.patientGender) }}</text>
              </view>
              <view class="kv">
                <text class="kv-k">年龄</text>
                <text class="kv-v">{{ item.patientAge != null ? item.patientAge + ' 岁' : '未填' }}</text>
              </view>
              <view class="kv">
                <text class="kv-k">联系电话</text>
                <text class="kv-v">{{ item.patientPhoneMask || '—' }}</text>
              </view>
              <view class="kv">
                <text class="kv-k">申请日期</text>
                <text class="kv-v">{{ formatDate(item.createdAt) }}</text>
              </view>
              <view class="kv">
                <text class="kv-k">最近更新</text>
                <text class="kv-v">{{ formatDate(item.updatedAt) }}</text>
              </view>
            </view>
          </view>

          <view class="remark-block">
            <text class="remark-k">关联诊疗</text>
            <text class="remark-v">暂无登记（后续可对接业务）</text>
          </view>

          <view v-if="item.status === 0" class="audit-row">
            <button class="mini ghost" hover-class="btn-h" :disabled="actingId === item.id" @click="onApprove(item)">
              通过
            </button>
            <button class="mini danger" hover-class="btn-h" :disabled="actingId === item.id" @click="onReject(item)">
              拒绝
            </button>
          </view>
          <view class="btn-row">
            <button class="act ghost" hover-class="btn-h" @click="toastDev('电子档案')">查看电子档案</button>
            <button class="act primary" hover-class="btn-h" @click="showDetail(item)">档案详情</button>
          </view>
        </view>
        <view v-if="!list.length" class="empty">暂无患者信息</view>
      </template>
      <view class="bottom-pad" />
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  approveDoctorBind,
  countPendingDoctorBinds,
  listDoctorBinds,
  rejectDoctorBind
} from '../../utils/doctorBinds.js'
import { clearAuth, getToken, getUserRole, setUserRole } from '../../utils/auth.js'
import { parseJwtPayload } from '../../utils/jwt.js'

const tabDefs = [
  { key: 'approved', label: '已绑定' },
  { key: 'pending', label: '待审核' },
  { key: 'rejected', label: '已拒绝' }
]

const activeTab = ref(0)
const list = ref([])
const loading = ref(false)
const pendingCount = ref(0)
const actingId = ref(null)

function statusKey(idx) {
  return tabDefs[idx]?.key || 'approved'
}

function switchTab(idx) {
  activeTab.value = idx
  loadList()
}

async function refreshPendingCount() {
  try {
    pendingCount.value = await countPendingDoctorBinds()
  } catch {
    pendingCount.value = 0
  }
}

async function loadList() {
  loading.value = true
  try {
    list.value = await listDoctorBinds(statusKey(activeTab.value))
  } catch (e) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
    list.value = []
  } finally {
    loading.value = false
  }
}

function ensureDoctor() {
  if (!getToken()) {
    uni.reLaunch({ url: '/pages/gate/gate' })
    return false
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
    return false
  }
  return true
}

onShow(() => {
  if (!ensureDoctor()) return
  refreshPendingCount()
  loadList()
})

function nameInitial(name) {
  const n = (name || '').trim()
  return n.length ? n.slice(0, 1) : '患'
}

function formatDate(iso) {
  if (!iso) return '—'
  const s = String(iso)
  return s.length >= 10 ? s.slice(0, 10) : s
}

function genderText(g) {
  if (g === 1) return '男'
  if (g === 2) return '女'
  return '未知'
}

function statusLabel(status) {
  if (status === 1) return '已绑定'
  if (status === 0) return '待审核'
  if (status === 2) return '已拒绝'
  return '—'
}

function chipClass(status) {
  if (status === 1) return 'chip-bound'
  if (status === 0) return 'chip-pending'
  if (status === 2) return 'chip-reject'
  return ''
}

function showDetail(item) {
  const initiator = item.initiator === 1 ? '患者发起' : item.initiator === 2 ? '医生发起' : '—'
  uni.showModal({
    title: (item.patientName || '患者') + ' · 档案',
    content: `档案编号：${item.id}\n患者用户 ID：${item.patientUserId}\n发起方：${initiator}\n申请时间：${formatDate(item.createdAt)}`,
    showCancel: false
  })
}

function toastDev(name) {
  uni.showToast({ title: `${name}功能开发中`, icon: 'none' })
}

async function onApprove(item) {
  actingId.value = item.id
  try {
    await approveDoctorBind(item.id)
    uni.showToast({ title: '已通过', icon: 'success' })
    await refreshPendingCount()
    await loadList()
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  } finally {
    actingId.value = null
  }
}

async function onReject(item) {
  const ok = await new Promise((resolve) => {
    uni.showModal({
      title: '拒绝申请',
      content: '确定拒绝该患者的绑定申请吗？',
      success: (r) => resolve(!!r.confirm)
    })
  })
  if (!ok) return
  actingId.value = item.id
  try {
    await rejectDoctorBind(item.id)
    uni.showToast({ title: '已拒绝', icon: 'none' })
    await refreshPendingCount()
    await loadList()
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  } finally {
    actingId.value = null
  }
}
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: $bg-page;
}

.tabs {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16rpx 24rpx 12rpx;
  gap: 16rpx;
  background: #ffffff;
  border-bottom: 1rpx solid #eef2f7;
}

.tab {
  position: relative;
  flex: 1;
  text-align: center;
  padding: 18rpx 8rpx;
  border-radius: 999rpx;
  background: #f3f4f6;
}

.tab-active {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
}

.tab-text {
  font-size: 28rpx;
  font-weight: 700;
  color: $text-primary;
}

.tab-badge {
  position: absolute;
  top: 4rpx;
  right: 8rpx;
  min-width: 34rpx;
  height: 34rpx;
  line-height: 34rpx;
  padding: 0 8rpx;
  border-radius: 999rpx;
  background: #ef4444;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 700;
  text-align: center;
}

.scroll {
  flex: 1;
  height: 0;
  padding: 20rpx 24rpx 0;
  box-sizing: border-box;
}

.hint,
.empty {
  text-align: center;
  color: $text-secondary;
  font-size: 28rpx;
  padding: 48rpx 0;
}

.archive-card {
  position: relative;
  overflow: hidden;
  background: $card-white;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  padding: 22rpx 22rpx 22rpx 26rpx;
  margin-bottom: 22rpx;
}

.archive-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 8rpx;
  background: linear-gradient(180deg, #0d9488 0%, #5eead4 100%);
  border-radius: $radius-card 0 0 $radius-card;
}

.archive-top {
  margin-bottom: 18rpx;
  padding-left: 8rpx;
}

.archive-title-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
}

.archive-label {
  font-size: 26rpx;
  font-weight: 900;
  color: #0f766e;
  letter-spacing: 2rpx;
}

.archive-no {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $text-secondary;
}

.archive-body {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  padding-left: 8rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #93c5fd 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.avatar-t {
  font-size: 40rpx;
  font-weight: 800;
  color: #ffffff;
}

.archive-main {
  flex: 1;
  min-width: 0;
}

.p-name {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: $text-primary;
  margin-bottom: 12rpx;
}

.kv {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10rpx 0;
  border-top: 1rpx solid #f3f4f6;
}

.kv:first-of-type {
  border-top: none;
  padding-top: 0;
}

.kv-k {
  font-size: 24rpx;
  color: $text-secondary;
  flex-shrink: 0;
  margin-right: 16rpx;
}

.kv-v {
  font-size: 24rpx;
  color: $text-primary;
  font-weight: 600;
  text-align: right;
  flex: 1;
  min-width: 0;
}

.remark-block {
  margin-top: 16rpx;
  margin-left: 8rpx;
  padding: 16rpx 18rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  border: 1rpx solid #eef2f7;
}

.remark-k {
  display: block;
  font-size: 22rpx;
  font-weight: 700;
  color: #64748b;
  margin-bottom: 6rpx;
}

.remark-v {
  font-size: 24rpx;
  color: $text-secondary;
  line-height: 1.45;
}

.status-chip {
  display: inline-block;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.chip-bound {
  background: #d1fae5;
  color: #047857;
}

.chip-pending {
  background: #fef3c7;
  color: #b45309;
}

.chip-reject {
  background: #fee2e2;
  color: #b91c1c;
}

.audit-row {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-top: 16rpx;
}

.mini {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 26rpx;
  border-radius: 12rpx;
  border: none;
  margin: 0;
}

.mini::after {
  border: none;
}

.mini.ghost {
  background: #ecfdf5;
  color: #047857;
  font-weight: 600;
}

.mini.danger {
  background: #fef2f2;
  color: #b91c1c;
  font-weight: 600;
}

.btn-row {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-top: 20rpx;
}

.act {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  font-size: 26rpx;
  font-weight: 600;
  border-radius: 14rpx;
  border: none;
  margin: 0;
}

.act::after {
  border: none;
}

.act.ghost {
  background: #ecfdf5;
  color: #047857;
  border: 2rpx solid #a7f3d0;
}

.act.primary {
  background: linear-gradient(135deg, #0d9488 0%, #0f766e 100%);
  color: #ffffff;
}

.btn-h {
  opacity: 0.88;
}

.bottom-pad {
  height: 48rpx;
}
</style>
