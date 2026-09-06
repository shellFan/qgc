<template>
  <div class="withdraws-page">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已到账" value="SUCCESS" />
            <el-option label="失败" value="FAIL" />
            <el-option label="已拒绝" value="REJECTED" />
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
        <el-table-column prop="withdrawNo" label="提现单号" width="160" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column label="提现金额" width="110">
          <template #default="{ row }">
            <span style="color: #ff4500; font-weight: 500">{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="手续费" width="90">
          <template #default="{ row }">{{ formatMoney(row.fee) }}</template>
        </el-table-column>
        <el-table-column label="实际到账" width="110">
          <template #default="{ row }">{{ formatMoney(row.actualAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
            <el-button v-if="row.status === 'PROCESSING'" size="small" type="success" @click="handlePaid(row)">已打款</el-button>
            <el-button v-if="row.status === 'PROCESSING'" size="small" type="danger" @click="handlePayFail(row)">打款失败</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 16px; justify-content: flex-end" v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="fetchList" />
    </el-card>

    <!-- 拒绝弹窗 -->
    <el-dialog v-model="rejectVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" placeholder="请输入拒绝原因" rows="3" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWithdrawList, approveWithdraw, rejectWithdraw, markWithdrawPaid, markWithdrawPayFail } from '@/api'

function formatMoney(fen) {
  if (fen == null) return '¥0.00'
  return '¥' + (fen / 100).toFixed(2)
}
function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }
function statusText(s) {
  const map = { PENDING: '待审核', PROCESSING: '处理中', SUCCESS: '已到账', FAIL: '失败', REJECTED: '已拒绝' }
  return map[s] || s
}
function statusType(s) {
  const map = { SUCCESS: 'success', FAIL: 'danger', REJECTED: 'danger', PROCESSING: 'primary', PENDING: 'warning' }
  return map[s] || 'info'
}

const query = ref({ status: '', page: 1, size: 20 })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectId = ref(null)

async function fetchList() {
  loading.value = true
  try {
    const res = await getWithdrawList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

async function handleApprove(row) {
  await ElMessageBox.confirm('确认通过此提现申请？', '审核')
  await approveWithdraw(row.id)
  ElMessage.success('已通过')
  fetchList()
}

function handleReject(row) { rejectId.value = row.id; rejectReason.value = ''; rejectVisible.value = true }

async function confirmReject() {
  if (!rejectReason.value.trim()) { ElMessage.warning('请输入拒绝原因'); return }
  await rejectWithdraw(rejectId.value, { reason: rejectReason.value })
  ElMessage.success('已拒绝')
  rejectVisible.value = false
  fetchList()
}

async function handlePaid(row) {
  await ElMessageBox.confirm('确认已打款？', '打款确认')
  await markWithdrawPaid(row.id)
  ElMessage.success('已标记打款成功')
  fetchList()
}

async function handlePayFail(row) {
  await ElMessageBox.confirm('确认打款失败？', '打款失败')
  await markWithdrawPayFail(row.id)
  ElMessage.success('已标记打款失败')
  fetchList()
}

onMounted(() => { fetchList() })
</script>