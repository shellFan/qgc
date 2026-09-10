<template>
  <div class="page">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="通过" value="APPROVED" />
            <el-option label="拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="nickname" label="评论者" width="120" />
        <el-table-column label="目标" width="150">
          <template #default="{ row }">
            <span>{{ row.targetType || '-' }} #{{ row.targetId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'APPROVED'" size="small" type="success" text @click="handleStatus(row, 'APPROVED')">通过</el-button>
            <el-button v-if="row.status !== 'REJECTED'" size="small" type="danger" text @click="handleStatus(row, 'REJECTED')">拒绝</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommentList, updateCommentStatus } from '@/api'

function statusText(s) {
  const map = { PENDING: '待审核', APPROVED: '通过', REJECTED: '拒绝' }
  return map[s] || s
}
function statusType(s) {
  const map = { APPROVED: 'success', REJECTED: 'danger', PENDING: 'warning' }
  return map[s] || 'info'
}
function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }

const query = ref({ status: '', page: 1, size: 20 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const res = await getCommentList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

async function handleStatus(row, status) {
  const action = status === 'APPROVED' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确认${action}此评论？`, action, { type: 'warning' })
    await updateCommentStatus(row.id, { status })
    ElMessage.success(`已${action}`)
    fetchList()
  } catch { /* cancel */ }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
</style>