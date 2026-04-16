<template>
  <view class="page">
    <view v-if="loading && !list.length" class="hint">
      <text>加载中…</text>
    </view>
    <view v-else-if="errHint" class="hint err">
      <text>{{ errHint }}</text>
      <button class="retry" size="mini" @click="load">重试</button>
    </view>
    <scroll-view v-else scroll-y class="scroll" :show-scrollbar="false">
      <view v-if="!list.length" class="empty">
        <text class="empty-t">暂无已上传档案</text>
        <text class="empty-p">在「上传诊断档案」中提交资料后，将显示在这里</text>
        <view class="empty-btn" hover-class="empty-btn-h" @click="goUpload">
          <text>去上传</text>
        </view>
      </view>
      <view v-else>
        <view v-for="item in list" :key="item.id" class="card">
          <view class="card-top">
            <view class="icon-wrap" :class="iconCls(item)">
              <text class="icon-t">{{ iconText(item) }}</text>
            </view>
            <view class="card-main">
              <text class="name">{{ item.originalName || '未命名' }}</text>
              <text class="meta">{{ bizTypeLabel(item.bizType) }} · {{ formatFileSize(item.sizeBytes) }}</text>
              <text class="time">{{ formatTime(item.createdAt) }}</text>
            </view>
          </view>
          <view class="card-actions">
            <view class="act outline" hover-class="act-h" @click="onOpen(item)">
              <text>查看</text>
            </view>
            <view class="act danger" hover-class="act-h" @click="onDelete(item)">
              <text>删除</text>
            </view>
          </view>
        </view>
        <view class="bottom-pad" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import {
  bizTypeLabel,
  deletePatientFile,
  downloadPatientFile,
  formatFileSize,
  listPatientFiles
} from '../../utils/patientFileApi.js'
import { clearAuth, getToken, getUserRole, setUserRole } from '../../utils/auth.js'
import { parseJwtPayload } from '../../utils/jwt.js'

const list = ref([])
const loading = ref(true)
const errHint = ref('')
const openingId = ref(null)

function ensurePatient() {
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
  if (role !== 'patient') {
    clearAuth()
    uni.showToast({ title: '请使用患者端登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/gate/gate' }), 400)
    return false
  }
  return true
}

function formatTime(s) {
  if (s == null) return '—'
  const str = typeof s === 'string' ? s : String(s)
  return str.replace('T', ' ').slice(0, 19)
}

function iconCls(item) {
  if (item.bizType === 'diagnosis_image' || isImageName(item.originalName, item.contentType)) return 'ic-img'
  if (item.bizType === 'diagnosis_note') return 'ic-note'
  return 'ic-doc'
}

function iconText(item) {
  if (item.bizType === 'diagnosis_image' || isImageName(item.originalName, item.contentType)) return '图'
  if (item.bizType === 'diagnosis_note') return '文'
  return '档'
}

function isImageName(name, ct) {
  if (ct && String(ct).toLowerCase().startsWith('image/')) return true
  if (!name) return false
  return /\.(jpe?g|png|gif|webp|bmp)$/i.test(name)
}

async function load() {
  if (!ensurePatient()) return
  errHint.value = ''
  loading.value = true
  try {
    list.value = await listPatientFiles()
  } catch (e) {
    errHint.value = e?.message || '加载失败'
    list.value = []
  } finally {
    loading.value = false
  }
}

onShow(() => {
  if (ensurePatient()) load()
})

onPullDownRefresh(async () => {
  await load()
  uni.stopPullDownRefresh()
})

function goUpload() {
  uni.navigateTo({ url: '/pages/upload/upload' })
}

async function onOpen(item) {
  if (openingId.value != null) return
  openingId.value = item.id
  uni.showLoading({ title: '打开中…', mask: true })
  try {
    const path = await downloadPatientFile(item.id)
    const img =
      item.bizType === 'diagnosis_image' ||
      isImageName(item.originalName, item.contentType)
    if (img) {
      uni.previewImage({ urls: [path], current: path })
    } else {
      await new Promise((resolve, reject) => {
        uni.openDocument({
          filePath: path,
          showMenu: true,
          success: resolve,
          fail: reject
        })
      })
    }
  } catch (e) {
    uni.showToast({ title: e?.message || '无法打开文件', icon: 'none' })
  } finally {
    uni.hideLoading()
    openingId.value = null
  }
}

function onDelete(item) {
  uni.showModal({
    title: '删除档案',
    content: `确定删除「${item.originalName || '该文件'}」吗？删除后不可恢复。`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deletePatientFile(item.id)
        uni.showToast({ title: '已删除', icon: 'success' })
        await load()
      } catch (e) {
        uni.showToast({ title: e?.message || '删除失败', icon: 'none' })
      }
    }
  })
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

.hint {
  padding: 80rpx 32rpx;
  text-align: center;
  font-size: 28rpx;
  color: $text-secondary;
}

.hint.err {
  color: #b91c1c;
}

.retry {
  margin-top: 24rpx;
}

.empty {
  padding: 100rpx 40rpx 60rpx;
  text-align: center;
}

.empty-t {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: $upload-teal;
}

.empty-p {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  color: $upload-teal-soft;
  line-height: 1.5;
}

.empty-btn {
  margin: 40rpx auto 0;
  width: 280rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 999rpx;
  background: $color-teal-dark;
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
}

.empty-btn-h {
  opacity: 0.9;
}

.card {
  margin: 20rpx 24rpx;
  padding: 24rpx;
  background: $card-white;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  border: 2rpx solid $upload-border;
}

.card-top {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 20rpx;
}

.icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ic-img {
  background: linear-gradient(135deg, #ccfbf1, #5eead4);
}

.ic-doc {
  background: linear-gradient(135deg, #e0e7ff, #a5b4fc);
}

.ic-note {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
}

.icon-t {
  font-size: 32rpx;
  font-weight: 900;
  color: #fff;
}

.card-main {
  flex: 1;
  min-width: 0;
}

.name {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: $text-primary;
  word-break: break-all;
}

.meta {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $text-secondary;
}

.time {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #94a3b8;
}

.card-actions {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  margin-top: 22rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #e2e8f0;
}

.act {
  flex: 1;
  height: 72rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
}

.act.outline {
  border: 2rpx solid $color-teal-dark;
  color: $color-teal-dark;
  background: #fff;
}

.act.danger {
  border: 2rpx solid #fecaca;
  color: #b91c1c;
  background: #fef2f2;
}

.act-h {
  opacity: 0.88;
}

.bottom-pad {
  height: 48rpx;
}
</style>
