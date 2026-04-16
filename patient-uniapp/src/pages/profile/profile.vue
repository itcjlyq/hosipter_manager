<template>
  <view class="wrap">
    <view v-if="loading" class="card">
      <text class="p">加载中…</text>
    </view>
    <template v-else>
      <view class="card">
        <text class="t">个人信息</text>
        <view class="kv">
          <text class="k">姓名</text>
          <text class="v">{{ me?.name || '—' }}</text>
        </view>
        <view class="kv">
          <text class="k">手机</text>
          <text class="v">{{ me?.phoneMask || '—' }}</text>
        </view>
        <view class="kv">
          <text class="k">用户 ID</text>
          <text class="v">{{ me?.userId ?? '—' }}</text>
        </view>
        <view class="kv">
          <text class="k">年龄</text>
          <text class="v">{{ me?.age != null ? me.age + ' 岁' : '—' }}</text>
        </view>
      </view>

      <view class="card">
        <text class="t">诊断档案</text>
        <text class="p">查看、预览或删除已提交给医生的资料。</text>
        <button class="btn secondary" hover-class="btn-h" @click="goArchives">我的诊断档案</button>
      </view>

      <view class="card">
        <text class="t">医患绑定</text>
        <text class="p">在下方管理主治医生申请与记录。</text>
        <button class="btn" hover-class="btn-h" @click="goBind">绑定 / 管理医生</button>
      </view>

      <view v-if="binds.length" class="card">
        <text class="t">最近记录</text>
        <view v-for="b in bindsTop" :key="b.id" class="bind-line">
          <text class="bn">{{ b.doctorName }}</text>
          <text :class="['bs', tagClass(b.status)]">{{ statusText(b.status) }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { fetchPatientMe, listPatientBinds } from '../../utils/patientApi.js'
import { clearAuth, getToken, getUserRole, isLoggedIn, setUserRole } from '../../utils/auth.js'
import { parseJwtPayload } from '../../utils/jwt.js'

const me = ref(null)
const binds = ref([])
const loading = ref(true)

const bindsTop = computed(() => {
  const arr = [...(binds.value || [])]
  arr.sort((a, b) => new Date(b.updatedAt || 0) - new Date(a.updatedAt || 0))
  return arr.slice(0, 5)
})

function statusText(s) {
  if (s === 1) return '已绑定'
  if (s === 0) return '待审核'
  if (s === 2) return '已拒绝'
  return '—'
}

function tagClass(s) {
  if (s === 1) return 's-ok'
  if (s === 0) return 's-wait'
  if (s === 2) return 's-no'
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
    uni.navigateBack({ fail: () => uni.reLaunch({ url: '/pages/gate/gate' }) })
    return false
  }
  return true
}

async function load() {
  if (!ensurePatient()) return
  loading.value = true
  try {
    const [m, b] = await Promise.all([fetchPatientMe(), listPatientBinds()])
    me.value = m
    binds.value = b
  } catch (e) {
    me.value = null
    binds.value = []
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onShow(() => {
  load()
})

function goBind() {
  uni.navigateTo({ url: '/pages/patient/bind-doctor' })
}

function goArchives() {
  uni.navigateTo({ url: '/pages/patient/archives' })
}
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.wrap {
  min-height: 100vh;
  padding: 24rpx;
  background: $bg-page;
}

.card {
  background: $card-white;
  border-radius: $radius-card;
  padding: 28rpx;
  box-shadow: $shadow-card;
  margin-bottom: 22rpx;
}

.t {
  font-size: 32rpx;
  font-weight: 800;
  color: $text-primary;
  display: block;
  margin-bottom: 16rpx;
}

.p {
  font-size: 26rpx;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: 20rpx;
}

.kv {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
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
  font-size: 26rpx;
  font-weight: 600;
  color: $text-primary;
  max-width: 420rpx;
  text-align: right;
}

.btn {
  margin-top: 8rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%);
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 700;
  border: none;
}

.btn::after {
  border: none;
}

.btn.secondary {
  background: #ffffff;
  color: $color-teal-dark;
  border: 2rpx solid $color-teal-dark;
}

.btn-h {
  opacity: 0.92;
}

.bind-line {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-top: 1rpx solid #f3f4f6;
}

.bind-line:first-of-type {
  border-top: none;
}

.bn {
  font-size: 28rpx;
  font-weight: 700;
  color: $text-primary;
}

.bs {
  font-size: 22rpx;
  font-weight: 700;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
}

.bs.s-ok {
  background: #d1fae5;
  color: #047857;
}

.bs.s-wait {
  background: #fef3c7;
  color: #b45309;
}

.bs.s-no {
  background: #fee2e2;
  color: #b91c1c;
}
</style>
