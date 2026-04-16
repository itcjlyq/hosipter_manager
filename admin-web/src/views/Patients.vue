<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">
        <el-icon class="mr"><Plus /></el-icon>
        新建患者
      </el-button>
      <el-button @click="load" :loading="loading">刷新</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" stripe border class="table" empty-text="暂无数据">
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column label="性别" width="90">
        <template #default="{ row }">{{ genderText(row.gender) }}</template>
      </el-table-column>
      <el-table-column prop="birthday" label="生日" width="120" />
      <el-table-column prop="idCardMask" label="证件尾号" width="120" />
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

    <el-dialog v-model="dlg.visible" :title="dlg.mode === 'create' ? '新建患者' : '编辑患者'" width="560px" destroy-on-close>
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
        <el-form-item label="性别" prop="gender">
          <el-select v-model="dlg.form.gender" placeholder="请选择" clearable style="width: 100%">
            <el-option :value="0" label="未知" />
            <el-option :value="1" label="男" />
            <el-option :value="2" label="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker v-model="dlg.form.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="证件尾号" prop="idCardMask">
          <el-input v-model="dlg.form.idCardMask" placeholder="可选，脱敏展示" />
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
import { listPatients, createPatient, updatePatient, deletePatient, resetPatientPassword } from '@/api/patients'
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
    gender: null,
    birthday: '',
    idCardMask: '',
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

function genderText(g) {
  if (g === 1) return '男'
  if (g === 2) return '女'
  return '未知'
}

async function load() {
  loading.value = true
  try {
    const res = await listPatients()
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
    gender: null,
    birthday: '',
    idCardMask: '',
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
    gender: row.gender ?? null,
    birthday: row.birthday || '',
    idCardMask: row.idCardMask || '',
    status: row.status ?? 1
  }
  dlg.visible = true
}

async function save() {
  await formRef.value?.validate().catch(() => Promise.reject())
  dlg.saving = true
  try {
    if (dlg.mode === 'create') {
      await createPatient({
        phone: dlg.form.phone.trim(),
        password: dlg.form.password,
        name: dlg.form.name.trim(),
        gender: dlg.form.gender ?? undefined,
        birthday: dlg.form.birthday || undefined,
        idCardMask: dlg.form.idCardMask || undefined
      })
      ElMessage.success('创建成功')
    } else {
      await updatePatient(dlg.userId, {
        name: dlg.form.name.trim(),
        gender: dlg.form.gender ?? undefined,
        birthday: dlg.form.birthday || undefined,
        idCardMask: dlg.form.idCardMask || undefined,
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
    await resetPatientPassword(pwdDlg.userId, pwdDlg.newPassword)
    ElMessage.success('密码已重置')
    pwdDlg.visible = false
  } finally {
    pwdDlg.saving = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除患者「${row.name}」吗？（软删除）`, '提示', { type: 'warning' })
  await deletePatient(row.userId)
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
