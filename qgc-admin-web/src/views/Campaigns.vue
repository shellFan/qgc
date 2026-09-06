<template>
  <div class="campaigns-page">
    <!-- 筛选 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已成功" value="SUCCESS" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="已过期" value="EXPIRED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input v-model="query.keyword" placeholder="标题/编号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="campaignNo" label="编号" width="140" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="creatorNickname" label="发起人" width="100" />
        <el-table-column label="目标金额" width="110">
          <template #default="{ row }">{{ formatMoney(row.targetAmount) }}</template>
        </el-table-column>
        <el-table-column label="已筹金额" width="110">
          <template #default="{ row }">
            <span style="color: #ff4500; font-weight: 500">{{ formatMoney(row.raisedAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="supportCount" label="支持人数" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
            <el-button v-if="row.status === 'ACTIVE'" size="small" type="warning" @click="handleFreeze(row)">冻结</el-button>
            <el-button v-if="row.status === 'FROZEN'" size="small" type="success" @click="handleUnfreeze(row)">解冻</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 16px; justify-content: flex-end" v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="fetchList" />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="筹款详情" width="600px">
      <div v-if="detail" class="detail-content">
        <p><b>编号:</b> {{ detail.campaignNo }}</p>
        <p><b>标题:</b> {{ detail.title }}</p>
        <p><b>描述:</b> {{ detail.description }}</p>
        <p><b>目标:</b> {{ formatMoney(detail.targetAmount) }}</p>
        <p><b>已筹:</b> {{ formatMoney(detail.raisedAmount) }}</p>
        <p><b>支持人数:</b> {{ detail.supportCount }}</p>
        <p><b>状态:</b> {{ statusText(detail.status) }}</p>
        <p v-if="detail.rejectReason"><b>拒绝原因:</b> {{ detail.rejectReason }}</p>
      </div>
    </el-dialog>

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
import { getCampaignList, approveCampaign, rejectCampaign, freezeCampaign, unfreezeCampaign } from '@/api'

function formatMoney(fen) {
  if (fen == null) return '¥0.00'
  return '¥' + (fen / 100).toFixed(2)
}
function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString()
}
function statusText(s) {
  const map = { DRAFT: '草稿', PENDING: '待审核', ACTIVE: '进行中', FROZEN: '已冻结', SUCCESS: '已成功', CLOSED: '已关闭', EXPIRED: '已过期', REJECTED: '已拒绝' }
  return map[s] || s
}
function statusType(s) {
  const map = { ACTIVE: 'success', FROZEN: 'warning', CLOSED: 'danger', EXPIRED: 'info', REJECTED: 'danger', PENDING: 'warning', SUCCESS: 'success' }
  return map[s] || 'info'
}

const query = ref({ status: '', keyword: '', page: 1, size: 20 })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const detail = ref(null)
const detailVisible = ref(false)
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectId = ref(null)

async function fetchList() {
  loading.value = true
  try {
    const res = await getCampaignList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function viewDetail(row) { detail.value = row; detailVisible.value = true }

async function handleApprove(row) {
  await ElMessageBox.confirm('确认通过审核？', '审核')
  await approveCampaign(row.id)
  ElMessage.success('审核通过')
  fetchList()
}

function handleReject(row) { rejectId.value = row.id; rejectReason.value = ''; rejectVisible.value = true }

async function confirmReject() {
  if (!rejectReason.value.trim()) { ElMessage.warning('请输入拒绝原因'); return }
  await rejectCampaign(rejectId.value, { reason: rejectReason.value })
  ElMessage.success('已拒绝')
  rejectVisible.value = false
  fetchList()
}

async function handleFreeze(row) {
  await ElMessageBox.confirm('确认冻结此筹款？', '冻结')
  await freezeCampaign(row.id)
  ElMessage.success('已冻结')
  fetchList()
}

async function handleUnfreeze(row) {
  await unfreezeCampaign(row.id)
  ElMessage.success('已解冻')
  fetchList()
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.detail-content p { margin-bottom: 8px; line-height: 1.6; }
</style>