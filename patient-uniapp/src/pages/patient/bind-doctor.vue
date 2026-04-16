<template>
  <view class="page">
    <view class="head">
      <view class="section">
        <text class="section-title">绑定医生</text>
        <text class="section-desc">以下为院内可绑定医生列表，点「申请绑定」提交后，需医生在工作台审核通过方可建立关系。</text>
      </view>
      <view class="search-wrap">
        <input
          class="search-input"
          type="text"
          confirm-type="search"
          placeholder="搜索姓名、科室、职称、工号"
          placeholder-class="search-ph"
          v-model="keyword"
        />
      </view>
    </view>

    <view class="body">
      <view v-if="loadingList" class="hint">正在加载医生列表…</view>
      <view v-else-if="!filteredDoctors.length" class="empty">
        {{ doctors.length ? '无匹配医生，请调整搜索词' : '暂无可选医生，请联系管理员维护医生档案。' }}
      </view>
      <view v-else class="doc-list">
        <view v-for="d in filteredDoctors" :key="d.userId" class="doc-card">
          <view class="doc-main">
            <text class="doc-name">{{ d.name || '未命名' }}</text>
            <text class="doc-sub">{{ d.dept || '科室未填' }} · {{ d.title || '职称未填' }}</text>
            <text v-if="d.doctorNo" class="doc-no">工号 {{ d.doctorNo }}</text>
            <view v-if="badgeFor(d.userId)" class="doc-badge">{{ badgeFor(d.userId) }}</view>
          </view>
          <button
            class="btn-bind"
            :class="bindBtnClass(d.userId)"
            hover-class="btn-bind-hover"
            :disabled="bindBtnDisabled(d.userId) || applying === d.userId"
            @click="onBindDoctor(d)"
          >
            {{ bindBtnLabel(d.userId) }}
          </button>
        </view>
      </view>

      <view class="section section-mt">
        <text class="section-title">我的绑定记录</text>
        <text class="section-desc">可查看各申请状态；待审核时支持撤销。</text>
      </view>
      <view v-if="loadingBinds" class="hint">加载记录…</view>
      <view v-else-if="!binds.length" class="empty">暂无记录。</view>
      <view v-else class="bind-list">
        <view v-for="b in bindsSorted" :key="b.id" class="bind-card">
          <view class="bind-row">
            <text class="bind-doc">{{ b.doctorName || '医生' }}</text>
            <view :class="['bind-tag', tagClass(b.status)]">{{ statusText(b.status) }}</view>
          </view>
          <text class="bind-meta">更新于 {{ fmtDate(b.updatedAt) }}</text>
          <button
            v-if="b.status === 0"
            class="btn-cancel"
            size="mini"
            hover-class="btn-cancel-hover"
            :disabled="acting === b.id"
            @click="onCancel(b)"
          >
            撤销申请
          </button>
        </view>
      </view>
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import {
  applyPatientBind,
  cancelPatientBind,
  listPatientBinds,
  listPatientDoctors
} from '../../utils/patientApi.js'
import { clearAuth, getToken, getUserRole, isLoggedIn, setUserRole } from '../../utils/auth.js'
import { parseJwtPayload } from '../../utils/jwt.js'

const doctors = ref([])
const binds = ref([])
const loadingList = ref(true)
const loadingBinds = ref(true)
const acting = ref(null)
const applying = ref(null)
const keyword = ref('')

const bindsSorted = computed(() => {
  const arr = [...(binds.value || [])]
  arr.sort((a, b) => {
    const ta = new Date(a.updatedAt || a.createdAt || 0).getTime()
    const tb = new Date(b.updatedAt || b.createdAt || 0).getTime()
    return tb - ta
  })
  return arr
})

/** 取该医生最新一条未删除绑定（列表已过滤 deleted，按更新时间第一条即可） */
function latestBindForDoctor(doctorUserId) {
  const list = bindsSorted.value.filter((b) => b.doctorUserId === doctorUserId)
  return list[0] || null
}

function doctorRelation(doctorUserId) {
  const b = latestBindForDoctor(doctorUserId)
  if (!b) return null
  if (b.status === 0) return 'pending'
  if (b.status === 1) return 'approved'
  if (b.status === 2) return 'rejected'
  return null
}

const filteredDoctors = computed(() => {
  const k = (keyword.value || '').trim().toLowerCase()
  const arr = doctors.value || []
  if (!k) return arr
  return arr.filter((d) => {
    const parts = [d.name, d.dept, d.title, d.doctorNo, d.userId != null ? String(d.userId) : '']
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return parts.includes(k)
  })
})

function badgeFor(doctorUserId) {
  const r = doctorRelation(doctorUserId)
  if (r === 'pending') return '待医生审核'
  if (r === 'approved') return '已建立绑定'
  if (r === 'rejected') return '曾拒绝，可再次申请'
  return ''
}

function bindBtnLabel(doctorUserId) {
  const r = doctorRelation(doctorUserId)
  if (r === 'pending') return '审核中'
  if (r === 'approved') return '已绑定'
  if (r === 'rejected') return '再次申请'
  return '申请绑定'
}

function bindBtnDisabled(doctorUserId) {
  const r = doctorRelation(doctorUserId)
  return r === 'pending' || r === 'approved'
}

function bindBtnClass(doctorUserId) {
  const r = doctorRelation(doctorUserId)
  if (r === 'approved') return 'is-done'
  if (r === 'pending') return 'is-wait'
  return ''
}

function ensurePatient() {
  if (!isLoggedIn()) {
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
  if (role !== 'patient') {
    clearAuth()
    uni.showToast({ title: '请使用患者端', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/gate/gate' }), 400)
    return false
  }
  return true
}

async function loadDoctors(quiet = false) {
  if (!quiet) loadingList.value = true
  try {
    doctors.value = await listPatientDoctors()
  } catch (e) {
    uni.showToast({ title: e.message || '加载医生列表失败', icon: 'none' })
    doctors.value = []
  } finally {
    loadingList.value = false
  }
}

async function loadBinds(quiet = false) {
  if (!quiet) loadingBinds.value = true
  try {
    binds.value = await listPatientBinds()
  } catch (e) {
    uni.showToast({ title: e.message || '加载绑定记录失败', icon: 'none' })
    binds.value = []
  } finally {
    loadingBinds.value = false
  }
}

onPullDownRefresh(async () => {
  if (!ensurePatient()) {
    uni.stopPullDownRefresh()
    return
  }
  try {
    await Promise.all([loadDoctors(true), loadBinds(true)])
  } finally {
    uni.stopPullDownRefresh()
  }
})

onShow(() => {
  if (!ensurePatient()) return
  loadDoctors()
  loadBinds()
})

function statusText(s) {
  if (s === 1) return '已绑定'
  if (s === 0) return '待审核'
  if (s === 2) return '已拒绝'
  return '—'
}

function tagClass(s) {
  if (s === 1) return 't-ok'
  if (s === 0) return 't-wait'
  if (s === 2) return 't-no'
  return ''
}

function fmtDate(iso) {
  if (!iso) return '—'
  const x = String(iso)
  return x.length >= 10 ? x.slice(0, 10) : x
}

function onBindDoctor(d) {
  if (bindBtnDisabled(d.userId)) {
    const r = doctorRelation(d.userId)
    uni.showToast({
      title: r === 'pending' ? '该医生申请正在审核中' : '已与该医生建立绑定',
      icon: 'none'
    })
    return
  }
  uni.showModal({
    title: '确认申请',
    content: `向「${d.name || '医生'}」发送绑定申请？`,
    success: async (res) => {
      if (!res.confirm) return
      applying.value = d.userId
      try {
        await applyPatientBind(d.userId)
        uni.showToast({ title: '已提交申请', icon: 'success' })
        await loadBinds()
      } catch (e) {
        uni.showToast({ title: e.message || '申请失败', icon: 'none' })
      } finally {
        applying.value = null
      }
    }
  })
}

function onCancel(b) {
  uni.showModal({
    title: '撤销申请',
    content: '确定撤销该待审核申请吗？',
    success: async (res) => {
      if (!res.confirm) return
      acting.value = b.id
      try {
        await cancelPatientBind(b.id)
        uni.showToast({ title: '已撤销', icon: 'none' })
        await loadBinds()
      } catch (e) {
        uni.showToast({ title: e.message || '撤销失败', icon: 'none' })
      } finally {
        acting.value = null
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
  box-sizing: border-box;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.head {
  padding: 20rpx 24rpx 12rpx;
}

.body {
  padding: 0 24rpx;
  box-sizing: border-box;
}

.section {
  margin-bottom: 16rpx;
}

.section-mt {
  margin-top: 8rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
  color: $text-primary;
  margin-bottom: 8rpx;
}

.section-desc {
  font-size: 24rpx;
  color: $text-secondary;
  line-height: 1.5;
}

.search-wrap {
  margin-bottom: 8rpx;
}

.search-input {
  height: 72rpx;
  padding: 0 24rpx;
  background: $card-white;
  border-radius: 999rpx;
  font-size: 28rpx;
  border: 2rpx solid #e5e7eb;
  box-sizing: border-box;
}

.search-ph {
  color: #9ca3af;
}

.hint,
.empty {
  text-align: center;
  font-size: 26rpx;
  color: $text-secondary;
  padding: 32rpx 12rpx;
}

.doc-list,
.bind-list {
  margin-bottom: 24rpx;
}

.doc-card {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
  background: $card-white;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  padding: 22rpx 20rpx;
  margin-bottom: 16rpx;
}

.doc-main {
  flex: 1;
  min-width: 0;
}

.doc-name {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: $text-primary;
}

.doc-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: $text-secondary;
}

.doc-no {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #94a3b8;
}

.doc-badge {
  display: inline-block;
  margin-top: 10rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 600;
  color: #64748b;
  background: #f1f5f9;
}

.btn-bind {
  flex-shrink: 0;
  width: 168rpx;
  height: 64rpx;
  line-height: 64rpx;
  padding: 0;
  margin: 0;
  font-size: 26rpx;
  font-weight: 700;
  border-radius: 12rpx;
  border: none;
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%);
  color: #ffffff;
}

.btn-bind::after {
  border: none;
}

.btn-bind.is-wait {
  background: #e2e8f0;
  color: #64748b;
}

.btn-bind.is-done {
  background: #d1fae5;
  color: #047857;
}

.btn-bind-hover {
  opacity: 0.9;
}

.bind-card {
  background: $card-white;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  padding: 22rpx;
  margin-bottom: 16rpx;
}

.bind-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
}

.bind-doc {
  font-size: 30rpx;
  font-weight: 800;
  color: $text-primary;
  flex: 1;
  min-width: 0;
}

.bind-tag {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.bind-tag.t-ok {
  background: #d1fae5;
  color: #047857;
}

.bind-tag.t-wait {
  background: #fef3c7;
  color: #b45309;
}

.bind-tag.t-no {
  background: #fee2e2;
  color: #b91c1c;
}

.bind-meta {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: $text-secondary;
}

.btn-cancel {
  margin-top: 16rpx;
  background: #f1f5f9;
  color: #475569;
  font-size: 24rpx;
}

.btn-cancel-hover {
  opacity: 0.9;
}

.bottom-pad {
  height: 48rpx;
}
</style>
