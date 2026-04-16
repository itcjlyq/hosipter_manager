<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="status" placeholder="全部状态" clearable style="width: 180px" @change="load">
        <el-option label="待审核" value="pending" />
        <el-option label="已通过" value="approved" />
        <el-option label="已拒绝" value="rejected" />
      </el-select>
      <el-button @click="load" :loading="loading">刷新</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" stripe border class="table" empty-text="暂无数据">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="doctorName" label="医生" min-width="120" />
      <el-table-column prop="doctorUserId" label="医生用户ID" width="120" />
      <el-table-column prop="patientName" label="患者" min-width="120" />
      <el-table-column prop="patientUserId" label="患者用户ID" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发起方" width="100">
        <template #default="{ row }">{{ row.initiator === 1 ? '患者' : '医生' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { listBinds } from '@/api/binds'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const rows = ref([])
const status = ref('')

function statusText(s) {
  if (s === 0) return '待审核'
  if (s === 1) return '已通过'
  if (s === 2) return '已拒绝'
  return String(s)
}

function statusType(s) {
  if (s === 0) return 'warning'
  if (s === 1) return 'success'
  if (s === 2) return 'info'
  return ''
}

async function load() {
  loading.value = true
  try {
    const res = await listBinds(status.value || undefined)
    rows.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  align-items: center;
}

.table {
  border-radius: 14px;
  overflow: hidden;
}
</style>
