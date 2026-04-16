<template>
  <diagnosis-records-screen :records="list" party-prefix="患者" />
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import DiagnosisRecordsScreen from '../../components/diagnosis-records/diagnosis-records-screen.vue'
import { getDemoDiagnosisRecords } from '../../utils/diagnosisRecordMock.js'
import { clearAuth, getToken, getUserRole, setUserRole } from '../../utils/auth.js'
import { parseJwtPayload } from '../../utils/jwt.js'

const list = ref(getDemoDiagnosisRecords())

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
  if (role !== 'doctor') {
    clearAuth()
    uni.showToast({ title: '请使用医生端登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/gate/gate' }), 400)
  }
})
</script>
