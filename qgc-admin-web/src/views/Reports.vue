<template>
  <div class="page">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已忽略" value="IGNORED" />
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
        <el-table-column prop="reporterNickname" label="举报者" width="120" />
        <el-table-column label="目标类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ targetTypeMap[row.targetType] || row.targetType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="100" />
        <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
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
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" type="danger" text @click="handleReport(row, 'CONFIRMED')">确认</el-button>
              <el-button size="small" text @click="handleReport(row, 'IGNORED')">忽略</el-button>
            </template>
            <span v-else style="color: #999">已处理</span>
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
import { getReportList, handleReportAdmin } from '@/api'

const targetTypeMap = { CAMPAIGN: '筹款', COMMENT: '评论', PROOF: '返图', USER: '用户' }
function statusText(s) {
  const map = { PENDING: '待处理', CONFIRMED: '已确认', IGNORED: '已忽略' }
  return map[s] || s
}
function statusType(s) {
  const map = { CONFIRMED: 'danger', IGNORED: 'info', PENDING: 'warning' }
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
    const res = await getReportList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

async function handleReport(row, action) {
  const label = action === 'CONFIRMED' ? '确认' : '忽略'
  try {
    await ElMessageBox.confirm(`确认${label}此举报？`, '处理举报', { type: 'warning' })
    await handleReportAdmin(row.id, { action })
    ElMessage.success(`已${label}`)
    fetchList()
  } catch { /* cancel */ }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
</style>