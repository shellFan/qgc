<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>管理员管理</span>
          <el-button type="primary" size="small" @click="openDialog()">新增管理员</el-button>
        </div>
      </template>

      <el-table :data="list" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ roleMap[row.role] || row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled === 1" @change="handleToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" text @click="handleResetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchList"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑管理员' : '新增管理员'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="!!editing" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="显示昵称" />
        </el-form-item>
        <el-form-item v-if="!editing" label="密码">
          <el-input v-model="form.password" type="password" placeholder="登录密码" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="选择角色" style="width: 100%">
            <el-option v-for="(label, key) in roleMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdVisible" title="重置密码" width="400px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPwd">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getManagerList, createManager, updateManager, resetManagerPassword, toggleManager } from '@/api'

const roleMap = { SUPER_ADMIN: '超级管理员', ADMIN: '管理员', OPERATOR: '运营' }
function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }

const query = ref({ page: 1, size: 20 })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const pwdVisible = ref(false)
const editing = ref(null)
const resetId = ref(null)
const form = ref({ username: '', nickname: '', password: '', role: 'OPERATOR', enabled: true })
const pwdForm = ref({ newPassword: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await getManagerList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function openDialog(row) {
  if (row) {
    editing.value = row
    form.value = { username: row.username, nickname: row.nickname, password: '', role: row.role, enabled: row.enabled === 1 }
  } else {
    editing.value = null
    form.value = { username: '', nickname: '', password: '', role: 'OPERATOR', enabled: true }
  }
  dialogVisible.value = true
}

async function saveForm() {
  if (!form.value.username.trim()) { ElMessage.warning('请输入用户名'); return }
  if (!editing.value && !form.value.password.trim()) { ElMessage.warning('请输入密码'); return }
  try {
    const payload = { ...form.value, enabled: form.value.enabled ? 1 : 0 }
    if (editing.value) {
      delete payload.password
      await updateManager(editing.value.id, payload)
    } else {
      await createManager(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch { ElMessage.error('保存失败') }
}

async function handleToggle(row) {
  try {
    await toggleManager(row.id)
    row.enabled = row.enabled === 1 ? 0 : 1
    ElMessage.success('状态已更新')
  } catch { ElMessage.error('操作失败') }
}

function handleResetPwd(row) {
  resetId.value = row.id
  pwdForm.value = { newPassword: '' }
  pwdVisible.value = true
}

async function confirmResetPwd() {
  if (!pwdForm.value.newPassword.trim()) { ElMessage.warning('请输入新密码'); return }
  try {
    await resetManagerPassword(resetId.value, pwdForm.value)
    ElMessage.success('密码已重置')
    pwdVisible.value = false
  } catch { ElMessage.error('重置失败') }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>