<template>
  <view class="daf">
    <view class="daf-head">
      <text class="daf-title">{{ titleText }}</text>
      <text class="daf-sub">{{ subtitleText }}</text>
    </view>

    <view class="daf-field">
      <text class="daf-label">档案标题</text>
      <input
        v-model="title"
        class="daf-input"
        type="text"
        maxlength="80"
        placeholder="请输入档案标题，例如：术前检查报告"
        placeholder-class="daf-ph"
      />
    </view>

    <view class="daf-field">
      <text class="daf-label">档案描述</text>
      <textarea
        v-model="description"
        class="daf-textarea"
        maxlength="2000"
        placeholder="可补充说明检查时间、医院、注意事项等"
        placeholder-class="daf-ph"
        auto-height
      />
    </view>

    <view class="daf-field">
      <text class="daf-label">上传图片资料</text>
      <view class="daf-img-grid">
        <view v-for="(p, idx) in images" :key="idx" class="daf-thumb-wrap">
          <image class="daf-thumb" :src="p" mode="aspectFill" @click="previewAt(idx)" />
          <view class="daf-thumb-x" @click.stop="removeImage(idx)">
            <text>×</text>
          </view>
        </view>
        <view v-if="images.length < maxImages" class="daf-add" hover-class="daf-add-h" @click="pickImages">
          <text class="daf-add-plus">+</text>
          <text class="daf-add-t">添加图片</text>
        </view>
      </view>
      <text class="daf-hint">支持 JPG、PNG 格式，最多可上传 {{ maxImages }} 张图片</text>
    </view>

    <view class="daf-field">
      <text class="daf-label">其他文件</text>
      <view class="daf-file-card" hover-class="daf-file-h" @click="pickDoc">
        <text class="daf-doc-ic">📄</text>
        <view class="daf-file-mid">
          <text class="daf-file-main">{{ docName }}</text>
          <text class="daf-file-sub">支持 PDF、Word 文档</text>
        </view>
        <text class="daf-upload-ic">⬆</text>
      </view>
      <view v-if="docPath" class="daf-doc-clear" @click="clearDoc">
        <text>移除已选文件</text>
      </view>
    </view>

    <view class="daf-check" @click="confirmed = !confirmed">
      <view :class="['daf-cb', confirmed && 'on']">
        <text v-if="confirmed" class="daf-cb-t">✓</text>
      </view>
      <text class="daf-check-t">我确认所上传资料真实有效</text>
    </view>

    <view
      class="daf-submit"
      :class="{ disabled: !canSubmit || submitting }"
      hover-class="daf-submit-h"
      @click="onSubmit"
    >
      <text v-if="submitting">提交中…</text>
      <text v-else>{{ submitLabel }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { uploadPatientFile } from '../../utils/patientFileApi.js'

const props = defineProps({
  titleText: { type: String, default: '上传诊断档案' },
  subtitleText: { type: String, default: '请上传您的相关诊断资料，方便医生查看' },
  submitLabel: { type: String, default: '提交给医生' },
  maxImages: { type: Number, default: 9 }
})

const emit = defineEmits(['success', 'fail'])

const title = ref('')
const description = ref('')
const images = ref([])
const docPath = ref('')
const docDisplayName = ref('')
const confirmed = ref(false)
const submitting = ref(false)

const docName = computed(() => (docPath.value ? docDisplayName.value || '已选择文件' : '选择文件'))

const canSubmit = computed(() => {
  const t = (title.value || '').trim()
  if (!t) return false
  if (!confirmed.value) return false
  const desc = (description.value || '').trim()
  const hasFile = images.value.length > 0 || !!docPath.value
  if (hasFile) return true
  return desc.length >= 6
})

function userDataPath() {
  try {
    if (typeof wx !== 'undefined' && wx.env && wx.env.USER_DATA_PATH) {
      return wx.env.USER_DATA_PATH
    }
  } catch {
    /* ignore */
  }
  return ''
}

function pickImages() {
  const remain = props.maxImages - images.value.length
  if (remain <= 0) return
  uni.chooseImage({
    count: remain,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const paths = res.tempFilePaths || []
      images.value = [...images.value, ...paths].slice(0, props.maxImages)
    }
  })
}

function removeImage(i) {
  images.value.splice(i, 1)
}

function previewAt(i) {
  uni.previewImage({ urls: images.value, current: images.value[i] })
}

function pickDoc() {
  uni.chooseFile({
    count: 1,
    type: 'file',
    extension: ['.pdf', '.doc', '.docx'],
    success: (res) => {
      const files = res.tempFiles || []
      const f = files[0] || null
      const path = f?.path || (res.tempFilePaths && res.tempFilePaths[0])
      if (!path) return
      const name = f?.name || String(path).split('/').pop() || '附件'
      if (!/\.(pdf|doc|docx)$/i.test(name)) {
        uni.showToast({ title: '请选择 PDF 或 Word 文件', icon: 'none' })
        return
      }
      docPath.value = path
      docDisplayName.value = name
    },
    fail: (err) => {
      const msg = err?.errMsg || ''
      if (msg.includes('cancel')) return
      uni.showToast({ title: '选择文件失败', icon: 'none' })
    }
  })
}

function clearDoc() {
  docPath.value = ''
  docDisplayName.value = ''
}

function writeNoteFile(t, desc) {
  const base = userDataPath()
  if (!base) return Promise.resolve(null)
  const fs = uni.getFileSystemManager()
  const filePath = `${base}/diagnosis_note_${Date.now()}.txt`
  const content = `档案标题：${t}\n\n档案描述：\n${desc || '（未填写）'}\n\n提交时间：${new Date().toLocaleString('zh-CN')}\n`
  return new Promise((resolve, reject) => {
    fs.writeFile({
      filePath,
      data: content,
      encoding: 'utf8',
      success: () => resolve(filePath),
      fail: reject
    })
  })
}

async function onSubmit() {
  if (submitting.value || !canSubmit.value) {
    if (!confirmed.value) {
      uni.showToast({ title: '请先勾选确认项', icon: 'none' })
    } else if (!(title.value || '').trim()) {
      uni.showToast({ title: '请填写档案标题', icon: 'none' })
    } else {
      uni.showToast({ title: '请上传图片/文件或完善描述（至少6字）', icon: 'none' })
    }
    return
  }
  submitting.value = true
  const t = (title.value || '').trim()
  const desc = (description.value || '').trim()
  try {
    let notePath = null
    try {
      notePath = await writeNoteFile(t, desc)
    } catch {
      /* 无法写入本地说明文件时仍上传附件 */
    }
    if (notePath) {
      await uploadPatientFile(notePath, { bizType: 'diagnosis_note' })
    }
    for (let i = 0; i < images.value.length; i++) {
      await uploadPatientFile(images.value[i], { bizType: 'diagnosis_image' })
    }
    if (docPath.value) {
      await uploadPatientFile(docPath.value, { bizType: 'diagnosis_doc' })
    }
    uni.showToast({ title: '提交成功', icon: 'success' })
    emit('success', { title: t, description: desc, imageCount: images.value.length, hasDoc: !!docPath.value })
    title.value = ''
    description.value = ''
    images.value = []
    clearDoc()
    confirmed.value = false
  } catch (e) {
    const msg = e?.message || '提交失败'
    uni.showToast({ title: msg.length > 20 ? '提交失败，请重试' : msg, icon: 'none' })
    emit('fail', e)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.daf {
  padding: 28rpx 28rpx 48rpx;
}

.daf-head {
  margin-bottom: 36rpx;
}

.daf-title {
  display: block;
  font-size: 44rpx;
  font-weight: 900;
  color: $upload-teal;
  letter-spacing: 1rpx;
}

.daf-sub {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
  color: $upload-teal-soft;
  line-height: 1.5;
}

.daf-field {
  margin-bottom: 36rpx;
}

.daf-label {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: $upload-teal;
  margin-bottom: 16rpx;
}

.daf-input {
  height: 88rpx;
  padding: 0 24rpx;
  background: $card-white;
  border-radius: 16rpx;
  border: 2rpx solid $upload-border;
  font-size: 28rpx;
  color: $text-primary;
  box-sizing: border-box;
}

.daf-textarea {
  min-height: 200rpx;
  padding: 20rpx 24rpx;
  background: $card-white;
  border-radius: 16rpx;
  border: 2rpx solid $upload-border;
  font-size: 28rpx;
  color: $text-primary;
  line-height: 1.55;
  width: 100%;
  box-sizing: border-box;
}

.daf-ph {
  color: #9cb0b0;
}

.daf-img-grid {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 16rpx;
}

.daf-thumb-wrap {
  position: relative;
  width: 200rpx;
  height: 200rpx;
}

.daf-thumb {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  background: #e2e8f0;
}

.daf-thumb-x {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 44rpx;
  height: 44rpx;
  border-radius: 999rpx;
  background: rgba(15, 23, 42, 0.65);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  font-weight: 700;
  line-height: 1;
}

.daf-add {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  border: 2rpx dashed #b8cccc;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.daf-add-h {
  opacity: 0.88;
  background: #ffffff;
}

.daf-add-plus {
  font-size: 64rpx;
  font-weight: 300;
  color: $color-teal-dark;
  line-height: 1;
}

.daf-add-t {
  font-size: 24rpx;
  color: $text-secondary;
}

.daf-hint {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  color: $upload-teal-soft;
}

.daf-file-card {
  flex-direction: row;
  display: flex;
  align-items: center;
  padding: 24rpx 22rpx;
  background: $card-white;
  border-radius: 16rpx;
  border: 2rpx solid $upload-border;
  gap: 18rpx;
}

.daf-file-h {
  opacity: 0.92;
}

.daf-doc-ic {
  font-size: 44rpx;
  opacity: 0.75;
}

.daf-file-mid {
  flex: 1;
  min-width: 0;
}

.daf-file-main {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.daf-file-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: $text-secondary;
}

.daf-upload-ic {
  font-size: 40rpx;
  color: $color-teal-dark;
  font-weight: 800;
}

.daf-doc-clear {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: $color-teal-dark;
  text-decoration: underline;
}

.daf-check {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 36rpx;
  padding: 8rpx 0;
}

.daf-cb {
  width: 36rpx;
  height: 36rpx;
  border-radius: 8rpx;
  border: 2rpx solid $upload-teal;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #fff;
}

.daf-cb.on {
  background: $color-teal-dark;
  border-color: $color-teal-dark;
}

.daf-cb-t {
  font-size: 24rpx;
  color: #fff;
  font-weight: 800;
}

.daf-check-t {
  flex: 1;
  font-size: 28rpx;
  color: $upload-teal;
  line-height: 1.45;
}

.daf-submit {
  height: 96rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, $color-teal-dark, #0a7a6f);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 800;
  color: #ffffff;
}

.daf-submit.disabled {
  opacity: 0.45;
  pointer-events: none;
}

.daf-submit-h {
  opacity: 0.92;
}
</style>
