<template>
  <div class="review-page">
    <el-tabs v-model="activeTab">
      <!-- 筹款审核 -->
      <el-tab-pane label="筹款审核" name="campaign">
        <el-table :data="campaigns" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column prop="creatorNickname" label="创建者" width="100" />
          <el-table-column prop="targetAmount" label="目标(元)" width="90">
            <template #default="{ row }">{{ ((row.targetAmount || 0) / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PENDING_REVIEW' ? 'warning' : (row.status === 'ACTIVE' ? 'success' : 'danger')" size="small">
                {{ statusMap[row.status] || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 'PENDING_REVIEW'" size="small" type="success" @click="reviewCampaign(row.id, true)">通过</el-button>
              <el-button v-if="row.status === 'PENDING_REVIEW'" size="small" type="danger" @click="reviewCampaign(row.id, false)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="campaignPage" :page-size="20" :total="campaignTotal" layout="prev, pager, next" @current-change="fetchCampaigns" style="margin-top: 12px" />
      </el-tab-pane>

      <!-- 评论审核 -->
      <el-tab-pane label="评论审核" name="comment">
        <el-table :data="comments" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="content" label="内容" show-overflow-tooltip />
          <el-table-column prop="nickname" label="评论者" width="100" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'warning' : (row.status === 1 ? 'success' : 'danger')" size="small">
                {{ commentStatusMap[row.status] || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="160" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" size="small" type="success" @click="reviewComment(row.id, 1)">通过</el-button>
              <el-button v-if="row.status === 0" size="small" type="danger" @click="reviewComment(row.id, 2)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 举报处理 -->
      <el-tab-pane label="举报处理" name="report">
        <el-table :data="reports" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="reason" label="举报原因" show-overflow-tooltip />
          <el-table-column prop="targetType" label="类型" width="80">
            <template #default="{ row }">{{ row.targetType === 'CAMPAIGN' ? '筹款' : '评论' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">{{ row.status === 0 ? '待处理' : '已处理' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="160" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" size="small" type="primary" @click="handleReport(row.id)">处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewCampaigns, reviewCampaign as reviewCampaignApi, getReviewComments, reviewComment as reviewCommentApi, getReports, handleReport as handleReportApi } from '@/api'

const activeTab = ref('campaign')
const statusMap = { PENDING_REVIEW: '待审核', ACTIVE: '已通过', REJECTED: '已拒绝', SUCCESS: '已成功', CLOSED: '已关闭', EXPIRED: '已过期' }
const commentStatusMap = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已删除' }

const campaigns = ref([])
const campaignPage = ref(1)
const campaignTotal = ref(0)

const comments = ref([])
const reports = ref([])

async function fetchCampaigns() {
  try {
    const res = await getReviewCampaigns({ page: campaignPage.value, size: 20 })
    campaigns.value = res?.data?.records || res?.data || []
    campaignTotal.value = res?.data?.total || 0
  } catch { /* error handled */ }
}

async function fetchComments() {
  try {
    const res = await getReviewComments({ page: 1, size: 50 })
    comments.value = res?.data?.records || res?.data || []
  } catch { /* error handled */ }
}

async function fetchReports() {
  try {
    const res = await getReports({ page: 1, size: 50 })
    reports.value = res?.data?.records || res?.data || []
  } catch { /* error handled */ }
}

async function reviewCampaign(id, approved) {
  try {
    if (!approved) {
      const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', { inputPlaceholder: '拒绝原因' })
      await reviewCampaignApi(id, { approved: false, rejectReason: value })
    } else {
      await reviewCampaignApi(id, { approved: true })
    }
    ElMessage.success(approved ? '已通过' : '已拒绝')
    fetchCampaigns()
  } catch { /* cancel */ }
}

async function reviewComment(id, status) {
  try {
    await reviewCommentApi(id, { status })
    ElMessage.success('操作成功')
    fetchComments()
  } catch { ElMessage.error('操作失败') }
}

async function handleReport(id) {
  try {
    await ElMessageBox.confirm('确认处理此举报?', '提示', { type: 'warning' })
    await handleReportApi(id)
    ElMessage.success('已处理')
    fetchReports()
  } catch { /* cancel */ }
}

onMounted(() => {
  fetchCampaigns()
  fetchComments()
  fetchReports()
})
</script>

<style scoped>
.review-page { padding: 0; }
</style>