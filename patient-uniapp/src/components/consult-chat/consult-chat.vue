<template>
  <view class="consult-chat">
    <!-- 顶部对方信息卡片（与稿图一致：白卡片 + 头像 + 在线状态） -->
    <view class="chat-header">
      <view class="h-avatar">
        <text class="h-av-t">{{ peerAvatarText }}</text>
      </view>
      <view class="h-info">
        <text class="h-name">{{ peerName }}</text>
        <view class="h-sub-row">
          <text class="h-online" :style="{ color: accentColor }">在线</text>
          <text class="h-dot">·</text>
          <text class="h-sub">{{ peerSubtitle }}</text>
        </view>
      </view>
      <view class="h-info-btn" hover-class="h-info-btn-h" :style="{ background: accentColor }" @click="emit('info')">
        <text class="h-i">i</text>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="msg-scroll"
      :scroll-into-view="scrollIntoView"
      scroll-with-animation
      :show-scrollbar="false"
    >
      <view
        v-for="m in messages"
        :key="m.id"
        :id="'m-' + m.id"
        class="msg-row"
        :class="isMine(m) ? 'is-mine' : 'is-peer'"
      >
        <view v-if="!isMine(m)" class="avatar av-peer">
          <text>{{ peerAvatarText }}</text>
        </view>
        <view class="msg-block">
          <view v-if="m.type === 'text'" class="bubble" :class="isMine(m) ? 'b-mine' : 'b-peer'" :style="mineBubbleStyle(m)">
            <text class="bubble-t">{{ m.text }}</text>
          </view>
          <view v-else-if="m.type === 'image'" class="img-box">
            <image class="msg-img" :src="m.imageUrl" mode="widthFix" @click="preview(m.imageUrl)" />
            <view v-if="m.imageTag" class="img-tag">{{ m.imageTag }}</view>
          </view>
          <text class="time" :class="isMine(m) ? 't-mine' : 't-peer'">{{ m.time }}</text>
        </view>
        <view v-if="isMine(m)" class="avatar av-self">
          <text>{{ selfAvatarText }}</text>
        </view>
      </view>
      <view id="chat-end" class="scroll-end" />
    </scroll-view>

    <!-- 底部输入区 -->
    <view class="input-bar" :style="{ paddingBottom: barBottom + 'px' }">
      <view class="ib-icon" hover-class="ib-hover" @click="emit('attach')">
        <text class="ib-emoji">📎</text>
      </view>
      <view class="ib-icon" hover-class="ib-hover" @click="pickImage">
        <text class="ib-emoji">🖼</text>
      </view>
      <input
        v-model="draft"
        class="ib-input"
        type="text"
        confirm-type="send"
        placeholder="输入消息..."
        placeholder-class="ib-ph"
        @confirm="doSend"
      />
      <view class="ib-send" hover-class="ib-send-h" :style="{ background: accentColor }" @click="doSend">
        <text class="send-icon">➤</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'

const props = defineProps({
  /** 当前登录端：患者发消息 from=patient；医生发消息 from=doctor */
  selfRole: {
    type: String,
    required: true,
    validator: (v) => v === 'patient' || v === 'doctor'
  },
  peerName: { type: String, default: '医生' },
  peerSubtitle: { type: String, default: '图文咨询' },
  peerAvatarText: { type: String, default: '医' },
  selfAvatarText: { type: String, default: '我' },
  messages: { type: Array, default: () => [] },
  /** 己方气泡颜色（医生端可传蓝色系） */
  accentColor: { type: String, default: '#0d9488' }
})

const emit = defineEmits(['send', 'send-image', 'attach', 'info'])

const draft = ref('')
const scrollIntoView = ref('')

const barBottom = computed(() => {
  try {
    const s = uni.getSystemInfoSync()
    const v = s.safeAreaInsets && s.safeAreaInsets.bottom
    return v != null ? v : 0
  } catch {
    return 0
  }
})

function isMine(m) {
  return m.from === props.selfRole
}

function mineBubbleStyle(m) {
  if (!isMine(m)) return {}
  return { background: props.accentColor }
}

function doSend() {
  const t = (draft.value || '').trim()
  if (!t) return
  draft.value = ''
  emit('send', { text: t })
  scrollToEndSoon()
}

function pickImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const p = res.tempFilePaths && res.tempFilePaths[0]
      if (p) emit('send-image', { tempFilePath: p })
      scrollToEndSoon()
    }
  })
}

function preview(url) {
  if (!url) return
  uni.previewImage({ urls: [url], current: url })
}

function scrollToEndSoon() {
  nextTick(() => {
    scrollIntoView.value = 'chat-end'
    setTimeout(() => {
      scrollIntoView.value = ''
    }, 100)
  })
}

watch(
  () => props.messages.length,
  () => scrollToEndSoon()
)

defineExpose({ scrollToEndSoon })
</script>

<style lang="scss" scoped>
.consult-chat {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #eef2f7;
  box-sizing: border-box;
}

.chat-header {
  flex-shrink: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  margin: 16rpx 24rpx 12rpx;
  padding: 20rpx 22rpx;
  background: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 28rpx rgba(15, 118, 110, 0.08);
}

.h-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #99f6e4, #14b8a6);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.h-av-t {
  font-size: 36rpx;
  font-weight: 800;
  color: #ffffff;
}

.h-info {
  flex: 1;
  min-width: 0;
}

.h-name {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #1f2937;
}

.h-sub-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 8rpx;
}

.h-online {
  font-size: 24rpx;
  font-weight: 700;
}

.h-dot {
  font-size: 24rpx;
  color: #94a3b8;
  margin: 0 6rpx;
}

.h-sub {
  font-size: 24rpx;
  color: #64748b;
}

.h-info-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 999rpx;
  background: #0d9488;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 12rpx;
}

.h-info-btn-h {
  opacity: 0.88;
}

.h-i {
  font-size: 28rpx;
  font-weight: 900;
  font-style: italic;
  color: #ffffff;
}

.msg-scroll {
  flex: 1;
  height: 0;
  padding-bottom: 12rpx;
}

.msg-row {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  padding: 12rpx 24rpx;
}

.msg-row.is-peer {
  justify-content: flex-start;
}

.msg-row.is-mine {
  justify-content: flex-end;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 28rpx;
  font-weight: 800;
  color: #ffffff;
}

.av-peer {
  background: linear-gradient(135deg, #a5b4fc, #6366f1);
  margin-right: 16rpx;
}

.av-self {
  background: linear-gradient(135deg, #5eead4, #0d9488);
  margin-left: 16rpx;
}

.msg-block {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.is-peer .msg-block {
  align-items: flex-start;
}

.is-mine .msg-block {
  align-items: flex-end;
}

.bubble {
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  line-height: 1.55;
}

.b-peer {
  background: #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(15, 23, 42, 0.06);
}

.b-mine {
  /* background 由内联 accent 控制 */
}

.bubble-t {
  font-size: 28rpx;
  color: inherit;
}

.b-peer .bubble-t {
  color: #1f2937;
}

.b-mine .bubble-t {
  color: #ffffff;
}

.img-box {
  position: relative;
  border-radius: 16rpx;
  overflow: hidden;
  max-width: 480rpx;
}

.msg-img {
  display: block;
  width: 100%;
  vertical-align: top;
  border-radius: 16rpx;
}

.img-tag {
  position: absolute;
  right: 8rpx;
  bottom: 8rpx;
  padding: 4rpx 10rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  color: #ffffff;
  background: rgba(0, 0, 0, 0.45);
}

.time {
  font-size: 22rpx;
  color: #94a3b8;
  margin-top: 8rpx;
}

.t-peer {
  align-self: flex-start;
  padding-left: 4rpx;
}

.t-mine {
  align-self: flex-end;
  padding-right: 4rpx;
}

.scroll-end {
  height: 24rpx;
}

.input-bar {
  flex-shrink: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16rpx 20rpx 16rpx;
  background: #f8fafc;
  border-top: 1rpx solid #e2e8f0;
  gap: 12rpx;
}

.ib-icon {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
}

.ib-hover {
  background: #e2e8f0;
}

.ib-emoji {
  font-size: 36rpx;
}

.ib-input {
  flex: 1;
  height: 72rpx;
  padding: 0 28rpx;
  background: #ffffff;
  border-radius: 999rpx;
  font-size: 28rpx;
  border: 2rpx solid #e2e8f0;
  box-sizing: border-box;
}

.ib-ph {
  color: #94a3b8;
}

.ib-send {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: #0d9488;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ib-send-h {
  opacity: 0.9;
}

.send-icon {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: 700;
  transform: rotate(-45deg);
}
</style>
