<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="8">
        <el-card class="stat" shadow="hover">
          <div class="stat-title">医生账号</div>
          <div class="stat-value">{{ stats.doctors }}</div>
          <div class="stat-sub">含档案信息</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat" shadow="hover">
          <div class="stat-title">患者账号</div>
          <div class="stat-value">{{ stats.patients }}</div>
          <div class="stat-sub">含档案信息</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat" shadow="hover">
          <div class="stat-title">绑定记录</div>
          <div class="stat-value">{{ stats.binds }}</div>
          <div class="stat-sub">全部状态合计</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="tips" shadow="never">
      <template #header>
        <span class="tips-title">快速提示</span>
      </template>
      <ul>
        <li>首次部署请确认已执行数据库脚本 <code>001</code>～<code>003</code>，并已启动后端 <code>8080</code>。</li>
        <li>本页数据为实时拉取列表长度；生产可替换为专用统计接口。</li>
        <li>管理员账号默认见后端 <code>application.yml</code> 的 <code>app.bootstrap.admin</code>。</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { listDoctors } from '@/api/doctors'
import { listPatients } from '@/api/patients'
import { listBinds } from '@/api/binds'

const stats = reactive({
  doctors: '—',
  patients: '—',
  binds: '—'
})

onMounted(async () => {
  try {
    const [d, p, b] = await Promise.all([listDoctors(), listPatients(), listBinds()])
    stats.doctors = String(d.data?.length ?? 0)
    stats.patients = String(p.data?.length ?? 0)
    stats.binds = String(b.data?.length ?? 0)
  } catch {
    stats.doctors = '—'
    stats.patients = '—'
    stats.binds = '—'
  }
})
</script>

<style scoped lang="scss">
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat {
  border-radius: 16px;
  border: 1px solid rgba(20, 184, 166, 0.15);
  background: linear-gradient(180deg, #ffffff 0%, #f0fdfa 100%);
}

.stat-title {
  font-size: 14px;
  color: #64748b;
}

.stat-value {
  margin-top: 8px;
  font-size: 34px;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: 0.5px;
}

.stat-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.tips {
  border-radius: 16px;
}

.tips-title {
  font-weight: 800;
}

.tips ul {
  margin: 0;
  padding-left: 18px;
  color: #475569;
  line-height: 1.8;
}

.tips code {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 6px;
}
</style>
