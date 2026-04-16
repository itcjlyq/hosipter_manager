<template>
  <view class="page">
    <consult-chat
      self-role="doctor"
      :peer-name="peerName"
      :peer-subtitle="peerSubtitle"
      :peer-avatar-text="peerAvatarText"
      self-avatar-text="医"
      :messages="messages"
      accent-color="#2563eb"
      @send="onSend"
      @send-image="onSendImage"
      @attach="onAttach"
      @info="onInfo"
    />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import ConsultChat from '../../components/consult-chat/consult-chat.vue'
import { clearAuth, getToken, getUserRole, setUserRole } from '../../utils/auth.js'
import { parseJwtPayload } from '../../utils/jwt.js'

let msgId = 1

const messages = ref([])

const peerName = '张女士'
const peerSubtitle = '在线 · 面部隆鼻咨询'
const peerAvatarText = '张'

function nowTime() {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function seedDemo() {
  if (messages.value.length) return
  messages.value = [
    {
      id: msgId++,
      from: 'patient',
      type: 'text',
      text: '医生您好，我想咨询一下隆鼻手术的相关事宜。',
      time: '09:12'
    },
    {
      id: msgId++,
      from: 'doctor',
      type: 'text',
      text: '您好，已了解您的诉求。建议先补充鼻部照片与既往过敏史，便于评估。',
      time: '09:15'
    }
  ]
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
    uni.showToast({ title: '请使用医生端', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/gate/gate' }), 400)
    return false
  }
  return true
}

onShow(() => {
  if (!ensureDoctor()) return
  seedDemo()
})

function onSend({ text }) {
  messages.value.push({
    id: msgId++,
    from: 'doctor',
    type: 'text',
    text,
    time: nowTime()
  })
}

function onSendImage({ tempFilePath }) {
  messages.value.push({
    id: msgId++,
    from: 'doctor',
    type: 'image',
    imageUrl: tempFilePath,
    time: nowTime()
  })
}

function onAttach() {
  uni.showToast({ title: '附件功能可对接业务后开放', icon: 'none' })
}

function onInfo() {
  uni.showModal({
    title: '患者信息',
    content: '演示会话：患者卡片信息可对接档案接口后展示。',
    showCancel: false
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
}
</style>
