<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <span>角色管理</span>
      </template>

      <el-table :data="list" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名" width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="权限数" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.permissions?.length || 0 }} 项</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="openDialog(row)">编辑权限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑权限弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑角色权限" width="600px">
      <div v-if="editing">
        <p style="margin-bottom: 16px; font-weight: 500">{{ editing.name }} - {{ editing.description }}</p>
        <el-tree
          ref="treeRef"
          :data="permissionTree"
          show-checkbox
          node-key="key"
          :default-checked-keys="checkedKeys"
          :props="{ label: 'label', children: 'children' }"
        />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoleList, getRoleDetail, updateRole } from '@/api'

const permissionTree = [
  {
    label: '筹款管理',
    children: [
      { label: '查看列表', key: 'CAMPAIGN_LIST' },
      { label: '审核', key: 'CAMPAIGN_REVIEW' },
      { label: '冻结/解冻', key: 'CAMPAIGN_FREEZE' },
      { label: '关闭', key: 'CAMPAIGN_CLOSE' }
    ]
  },
  {
    label: '用户管理',
    children: [
      { label: '查看列表', key: 'USER_LIST' },
      { label: '封禁/解封', key: 'USER_DISABLE' }
    ]
  },
  {
    label: '提现管理',
    children: [
      { label: '查看列表', key: 'WITHDRAW_LIST' },
      { label: '审批', key: 'WITHDRAW_APPROVE' },
      { label: '标记打款', key: 'WITHDRAW_PAY' }
    ]
  },
  {
    label: '内容管理',
    children: [
      { label: '评论管理', key: 'COMMENT_MANAGE' },
      { label: '返图管理', key: 'PROOF_MANAGE' },
      { label: '举报处理', key: 'REPORT_MANAGE' }
    ]
  },
  {
    label: '系统管理',
    children: [
      { label: '管理员管理', key: 'MANAGER_MANAGE' },
      { label: '角色管理', key: 'ROLE_MANAGE' },
      { label: '系统配置', key: 'CONFIG_MANAGE' },
      { label: '审计日志', key: 'AUDIT_LOG' }
    ]
  }
]

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const checkedKeys = ref([])
const treeRef = ref(null)

async function fetchList() {
  loading.value = true
  try {
    const res = await getRoleList()
    list.value = res.data || []
  } catch { /* ignore */ } finally { loading.value = false }
}

async function openDialog(row) {
  editing.value = row
  try {
    const res = await getRoleDetail(row.id)
    const detail = res.data || row
    checkedKeys.value = detail.permissions || []
    dialogVisible.value = true
    await nextTick()
    treeRef.value?.setCheckedKeys(checkedKeys.value)
  } catch {
    checkedKeys.value = row.permissions || []
    dialogVisible.value = true
  }
}

async function savePermissions() {
  if (!treeRef.value || !editing.value) return
  const checked = treeRef.value.getCheckedKeys()
  const halfChecked = treeRef.value.getHalfCheckedKeys()
  const permissions = [...checked, ...halfChecked]
  try {
    await updateRole(editing.value.id, { permissions })
    ElMessage.success('权限已更新')
    dialogVisible.value = false
    fetchList()
  } catch { ElMessage.error('保存失败') }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
</style>