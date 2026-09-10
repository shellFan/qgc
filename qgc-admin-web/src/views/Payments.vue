<template>
  <div class="page">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="订单号">
          <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待支付" value="PENDING" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已退款" value="REFUNDED" />
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
        <el-table-column prop="orderNo" label="订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="nickname" label="用户" width="120" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">¥{{ (row.amount / 100).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ typeMap[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'SUCCESS'" size="small" type="warning" text @click="handleRefund(row)">退款</el-button>
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

    <!-- 退款弹窗 -->
    <el-dialog v-model="refundVisible" title="退款" width="400px">
      <el-form :model="refundForm" label-width="80px">
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.amount" :min="1" :max="refundMax" :step="1" />
          <span style="margin-left: 8px; color: #999">分 (最大: {{ refundMax }})</span>
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" placeholder="退款原因" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPaymentList, refundPayment } from '@/api'

const typeMap = { DONATION: '捐赠', REWARD: '打赏', PURCHASE: '购买' }
function statusText(s) {
  const map = { PENDING: '待支付', SUCCESS: '成功', FAILED: '失败', REFUNDED: '已退款' }
  return map[s] || s
}
function statusType(s) {
  const map = { SUCCESS: 'success', FAILED: 'danger', REFUNDED: 'warning', PENDING: 'info' }
  return map[s] || 'info'
}
function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }

const query = ref({ orderNo: '', status: '', page: 1, size: 20 })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const refundVisible = ref(false)
const refundId = ref(null)
const refundMax = ref(0)
const refundForm = ref({ amount: 0, reason: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await getPaymentList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function handleRefund(row) {
  refundId.value = row.id
  refundMax.value = row.amount
  refundForm.value = { amount: row.amount, reason: '' }
  refundVisible.value = true
}

async function confirmRefund() {
  if (!refundForm.value.amount) { ElMessage.warning('请输入退款金额'); return }
  try {
    await refundPayment(refundId.value, { amount: refundForm.value.amount, reason: refundForm.value.reason })
    ElMessage.success('退款成功')
    refundVisible.value = false
    fetchList()
  } catch { ElMessage.error('退款失败') }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
</style>