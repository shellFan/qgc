<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>待处理事项</span></template>
          <div class="pending-list">
            <div class="pending-item" v-for="item in pendingItems" :key="item.label">
              <span>{{ item.label }}</span>
              <el-tag :type="item.count > 0 ? 'danger' : 'success'">{{ item.count }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>分类统计</span></template>
          <div class="category-stats">
            <div class="category-item" v-for="cat in categoryStats" :key="cat.name">
              <span>{{ cat.name }}</span>
              <el-progress :percentage="cat.percentage" :show-pivot="false" style="flex:1; margin: 0 12px" />
              <span>{{ cat.count }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboardOverview, getDashboardPendingCounts, getDashboardCategoryStats } from '@/api'

const statCards = ref([
  { label: '总用户数', value: 0 },
  { label: '活跃筹款', value: 0 },
  { label: '总筹款金额(元)', value: '0.00' },
  { label: '今日新增', value: 0 }
])

const pendingItems = ref([
  { label: '待审核筹款', count: 0 },
  { label: '待审核提现', count: 0 },
  { label: '待处理举报', count: 0 }
])

const categoryStats = ref([])

async function fetchOverview() {
  try {
    const res = await getDashboardOverview()
    const d = res.data
    statCards.value = [
      { label: '总用户数', value: d.totalUsers || 0 },
      { label: '活跃筹款', value: d.activeCampaigns || 0 },
      { label: '总筹款金额(元)', value: ((d.totalAmount || 0) / 100).toFixed(2) },
      { label: '今日新增', value: d.todayNew || 0 }
    ]
  } catch { /* ignore */ }
}

async function fetchPending() {
  try {
    const res = await getDashboardPendingCounts()
    const d = res.data
    pendingItems.value = [
      { label: '待审核筹款', count: d.pendingCampaigns || 0 },
      { label: '待审核提现', count: d.pendingWithdraws || 0 },
      { label: '待处理举报', count: d.pendingReports || 0 }
    ]
  } catch { /* ignore */ }
}

async function fetchCategoryStats() {
  try {
    const res = await getDashboardCategoryStats()
    categoryStats.value = (res.data || []).map(c => ({
      name: c.name, count: c.count, percentage: c.percentage || 0
    }))
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchOverview()
  fetchPending()
  fetchCategoryStats()
})
</script>

<style scoped>
.stat-cards { margin-bottom: 0; }
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }
.pending-list { display: flex; flex-direction: column; gap: 12px; }
.pending-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.category-stats { display: flex; flex-direction: column; gap: 10px; }
.category-item { display: flex; align-items: center; font-size: 14px; }
</style>