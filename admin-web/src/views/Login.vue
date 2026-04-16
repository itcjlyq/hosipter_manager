<template>
  <div class="login-page">
    <div class="panel">
      <div class="panel-left">
        <h1>医院管理后台</h1>
        <p>医生 / 患者 / 医患绑定一站式维护</p>
        <ul>
          <li>清新界面，快速上手</li>
          <li>对接现有 Spring Boot 接口</li>
          <li>请使用 <strong>管理员账号</strong> 登录</li>
        </ul>
      </div>
      <div class="panel-right">
        <h2>登录</h2>
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" maxlength="11" placeholder="请输入手机号" clearable />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-button type="primary" class="btn" :loading="loading" native-type="submit" @click="submit">
            进入后台
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginPassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { parseJwtPayload } from '@/utils/jwt'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value?.validate().catch(() => Promise.reject())
  loading.value = true
  try {
    const res = await loginPassword({ phone: form.phone.trim(), password: form.password })
    const token = res.data?.accessToken
    const exp = res.data?.expiresAt
    if (!token) {
      throw new Error('登录响应缺少 token')
    }
    const payload = parseJwtPayload(token)
    if (payload?.role !== 'admin') {
      ElMessage.error('非管理员账号，无法进入管理后台')
      return
    }
    auth.setSession(token, exp)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.replace(typeof redirect === 'string' ? redirect : '/')
  } catch (e) {
    // request 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: radial-gradient(1200px 600px at 10% 10%, #ccfbf1 0%, transparent 55%),
    radial-gradient(900px 500px at 90% 20%, #e0f2fe 0%, transparent 50%), #f8fafc;
}

.panel {
  width: min(960px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 0;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.panel-left {
  padding: 36px 32px;
  background: linear-gradient(145deg, #14b8a6 0%, #0d9488 55%, #0f766e 100%);
  color: #ecfeff;
}

.panel-left h1 {
  margin: 0 0 12px;
  font-size: 26px;
  letter-spacing: 0.5px;
}

.panel-left p {
  margin: 0 0 18px;
  opacity: 0.95;
  line-height: 1.6;
}

.panel-left ul {
  margin: 0;
  padding-left: 18px;
  line-height: 1.9;
  opacity: 0.95;
}

.panel-right {
  padding: 36px 32px 40px;
}

.panel-right h2 {
  margin: 0 0 18px;
  font-size: 22px;
  color: #0f172a;
}

.btn {
  width: 100%;
  margin-top: 8px;
  height: 44px;
  font-weight: 700;
  border-radius: 999px;
}

@media (max-width: 820px) {
  .panel {
    grid-template-columns: 1fr;
  }
  .panel-left {
    padding-bottom: 24px;
  }
}
</style>
