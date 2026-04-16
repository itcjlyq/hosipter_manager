<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">
        <el-icon class="mr"><Plus /></el-icon>
        新建医生
      </el-button>
      <el-button @click="load" :loading="loading">刷新</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" stripe border class="table" empty-text="暂无数据">
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="doctorNo" label="工号" width="120" />
      <el-table-column prop="dept" label="科室" width="120" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="260">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="openPwd(row)">重置密码</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlg.visible" :title="dlg.mode === 'create' ? '新建医生' : '编辑医生'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="dlg.form" :rules="formRules" label-width="96px">
        <el-form-item v-if="dlg.mode === 'create'" label="手机号" prop="phone">
          <el-input v-model="dlg.form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item v-if="dlg.mode === 'create'" label="初始密码" prop="password">
          <el-input v-model="dlg.form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="dlg.form.name" />
        </el-form-item>
        <el-form-item label="工号" prop="doctorNo">
          <el-input v-model="dlg.form.doctorNo" placeholder="可选" />
        </el-form-item>
        <el-form-item label="科室" prop="dept">
          <el-input v-model="dlg.form.dept" placeholder="可选" />
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-input v-model="dlg.form.title" placeholder="可选" />
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input v-model="dlg.form.intro" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
        <el-form-item v-if="dlg.mode === 'edit'" label="状态" prop="status">
          <el-radio-group v-model="dlg.form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg.visible = false">取消</el-button>
        <el-button type="primary" :loading="dlg.saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdDlg.visible" title="重置密码" width="420px" destroy-on-close>
      <el-form ref="pwdRef" :model="pwdDlg" :rules="pwdRules" label-width="96px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdDlg.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDlg.visible = false">取消</el-button>
        <el-button type="primary" :loading="pwdDlg.saving" @click="savePwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listDoctors, createDoctor, updateDoctor, deleteDoctor, resetDoctorPassword } from '@/api/doctors'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const rows = ref([])
const formRef = ref()
const pwdRef = ref()

const dlg = reactive({
  visible: false,
  mode: 'create',
  saving: false,
  userId: null,
  form: {
    phone: '',
    password: '',
    name: '',
    doctorNo: '',
    dept: '',
    title: '',
    intro: '',
    status: 1
  }
})

const rulesCreate = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}
const rulesEdit = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}
const formRules = computed(() => (dlg.mode === 'create' ? rulesCreate : rulesEdit))

const pwdDlg = reactive({
  visible: false,
  saving: false,
  userId: null,
  newPassword: ''
})

const pwdRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await listDoctors()
    rows.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  dlg.mode = 'create'
  dlg.userId = null
  dlg.form = {
    phone: '',
    password: '',
    name: '',
    doctorNo: '',
    dept: '',
    title: '',
    intro: '',
    status: 1
  }
  dlg.visible = true
}

function openEdit(row) {
  dlg.mode = 'edit'
  dlg.userId = row.userId
  dlg.form = {
    phone: row.phone,
    password: '',
    name: row.name,
    doctorNo: row.doctorNo || '',
    dept: row.dept || '',
    title: row.title || '',
    intro: row.intro || '',
    status: row.status ?? 1
  }
  dlg.visible = true
}

async function save() {
  await formRef.value?.validate().catch(() => Promise.reject())
  dlg.saving = true
  try {
    if (dlg.mode === 'create') {
      await createDoctor({
        phone: dlg.form.phone.trim(),
        password: dlg.form.password,
        name: dlg.form.name.trim(),
        doctorNo: dlg.form.doctorNo || undefined,
        dept: dlg.form.dept || undefined,
        title: dlg.form.title || undefined,
        intro: dlg.form.intro || undefined
      })
      ElMessage.success('创建成功')
    } else {
      await updateDoctor(dlg.userId, {
        name: dlg.form.name.trim(),
        doctorNo: dlg.form.doctorNo || undefined,
        dept: dlg.form.dept || undefined,
        title: dlg.form.title || undefined,
        intro: dlg.form.intro || undefined,
        status: dlg.form.status
      })
      ElMessage.success('保存成功')
    }
    dlg.visible = false
    await load()
  } finally {
    dlg.saving = false
  }
}

function openPwd(row) {
  pwdDlg.userId = row.userId
  pwdDlg.newPassword = ''
  pwdDlg.visible = true
}

async function savePwd() {
  await pwdRef.value?.validate().catch(() => Promise.reject())
  pwdDlg.saving = true
  try {
    await resetDoctorPassword(pwdDlg.userId, pwdDlg.newPassword)
    ElMessage.success('密码已重置')
    pwdDlg.visible = false
  } finally {
    pwdDlg.saving = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除医生「${row.name}」吗？（软删除）`, '提示', { type: 'warning' })
  await deleteDoctor(row.userId)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.mr {
  margin-right: 6px;
}

.table {
  border-radius: 14px;
  overflow: hidden;
}
</style>
