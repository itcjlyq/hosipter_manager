<template>
  <view class="page">
    <consult-chat
      v-if="canConsult"
      self-role="patient"
      :peer-name="peerName"
      :peer-subtitle="peerSubtitle"
      :peer-avatar-text="peerAvatarText"
      :self-avatar-text="selfAvatarText"
      :messages="messages"
      accent-color="#0d9488"
      @send="onSend"
      @send-image="onSendImage"
      @attach="onAttach"
      @info="onInfo"
    />
    <view v-else class="gate-tip">
      <text class="gate-t">请先绑定医生后再使用在线咨询</text>
      <button class="gate-btn" hover-class="gate-btn-h" @click="goBind">去绑定医生</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import ConsultChat from '../../components/consult-chat/consult-chat.vue'
import { listPatientBinds, fetchPatientMe } from '../../utils/patientApi.js'
import { clearAuth, getToken, getUserRole, isLoggedIn, setUserRole } from '../../utils/auth.js'
import { parseJwtPayload } from '../../utils/jwt.js'

let msgId = 1

const binds = ref([])
const me = ref(null)
const messages = ref([])

const hasApprovedBind = computed(() => (binds.value || []).some((b) => b.status === 1))

const primaryDoctor = computed(() => {
  const list = [...(binds.value || [])].filter((b) => b.status === 1)
  list.sort((a, b) => new Date(b.updatedAt || 0) - new Date(a.updatedAt || 0))
  return list[0] || null
})

const canConsult = computed(() => hasApprovedBind.value)

const peerName = computed(() => {
  const n = primaryDoctor.value?.doctorName
  return n ? `${n}医生` : '主治医生'
})

const peerSubtitle = computed(() => '图文咨询 · 复诊与用药问题请遵医嘱')

const peerAvatarText = computed(() => {
  const n = primaryDoctor.value?.doctorName || '医'
  return n.slice(0, 1)
})

const selfAvatarText = computed(() => {
  const n = (me.value && me.value.name) || '患'
  return n.slice(0, 1)
})

function nowTime() {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function seedDemoIfEmpty() {
  if (messages.value.length) return
  messages.value = [
    {
      id: msgId++,
      from: 'patient',
      type: 'text',
      text: '医生您好，我想咨询一下隆鼻手术的相关事宜。我对自己鼻子的形状不太满意，想了解一下手术相关的信息。',
      time: '09:12'
    },
    {
      id: msgId++,
      from: 'doctor',
      type: 'text',
      text: '您好，我是您的主治医生。隆鼻术式与恢复周期因人而异，建议先上传正面/侧面照片，便于初步评估。',
      time: '09:15'
    },
    {
      id: msgId++,
      from: 'patient',
      type: 'text',
      text: '好的，我这边先整理一下照片发给您。',
      time: '09:18'
    }
  ]
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

async function load() {
  if (!ensurePatient()) return
  try {
    const [m, b] = await Promise.all([fetchPatientMe(), listPatientBinds()])
    me.value = m
    binds.value = b
    if (hasApprovedBind.value) seedDemoIfEmpty()
  } catch (e) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  }
}

onShow(() => {
  load()
})

function onSend({ text }) {
  messages.value.push({
    id: msgId++,
    from: 'patient',
    type: 'text',
    text,
    time: nowTime()
  })
}

function onSendImage({ tempFilePath }) {
  messages.value.push({
    id: msgId++,
    from: 'patient',
    type: 'image',
    imageUrl: tempFilePath,
    time: nowTime(),
    imageTag: ''
  })
}

function onAttach() {
  uni.showToast({ title: '附件功能可对接业务后开放', icon: 'none' })
}

function onInfo() {
  uni.showModal({
    title: '咨询说明',
    content: '本页为演示会话，消息仅存于本地；接入 IM 后由服务端同步。',
    showCancel: false
  })
}

function goBind() {
  uni.navigateTo({ url: '/pages/patient/bind-doctor' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #eef2f7;
}

.gate-tip {
  padding: 120rpx 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.gate-t {
  font-size: 28rpx;
  color: #64748b;
  text-align: center;
  line-height: 1.6;
  margin-bottom: 40rpx;
}

.gate-btn {
  width: 400rpx;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #14b8a6, #0d9488);
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 700;
  border: none;
}

.gate-btn::after {
  border: none;
}

.gate-btn-h {
  opacity: 0.92;
}
</style>
