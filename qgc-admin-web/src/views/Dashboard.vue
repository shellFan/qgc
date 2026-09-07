<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon">{{ card.icon }}</div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>待处理事项</span></template>
          <div class="pending-list">
            <div class="pending-item" v-for="item in pendingItems" :key="item.label" @click="goPending(item.route)">
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

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>7日趋势</span></template>
          <div class="trend-chart">
            <div class="trend-row" v-for="t in trendData" :key="t.date">
              <span class="trend-date">{{ t.date }}</span>
              <div class="trend-bar-wrap">
                <div class="trend-bar" :style="{ width: t.barWidth + '%' }"></div>
              </div>
              <span class="trend-val">{{ t.count }}笔 / {{ t.amount }}元</span>
            </div>
            <van-empty v-if="trendData.length === 0" description="暂无趋势数据" :image-size="60" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>实时概况</span></template>
          <div class="realtime-stats">
            <div class="rt-item">
              <span class="rt-label">今日新增用户</span>
              <span class="rt-value">{{ realtime.todayNewUsers || 0 }}</span>
            </div>
            <div class="rt-item">
              <span class="rt-label">今日新增筹款</span>
              <span class="rt-value">{{ realtime.todayNewCampaigns || 0 }}</span>
            </div>
            <div class="rt-item">
              <span class="rt-label">今日投喂笔数</span>
              <span class="rt-value">{{ realtime.todaySupports || 0 }}</span>
            </div>
            <div class="rt-item">
              <span class="rt-label">今日投喂金额</span>
              <span class="rt-value">{{ ((realtime.todayAmount || 0) / 100).toFixed(2) }}元</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardOverview, getDashboardPendingCounts, getDashboardCategoryStats, getDashboardTrend } from '@/api'

const router = useRouter()

const statCards = ref([
  { label: '总用户数', value: 0, icon: '👥' },
  { label: '活跃筹款', value: 0, icon: '📋' },
  { label: '总筹款金额(元)', value: '0.00', icon: '💰' },
  { label: '今日新增', value: 0, icon: '🆕' }
])

const pendingItems = ref([
  { label: '待审核筹款', count: 0, route: '/review' },
  { label: '待审核提现', count: 0, route: '/withdraws' },
  { label: '待处理举报', count: 0, route: '/review' }
])

const categoryStats = ref([])
const trendData = ref([])
const realtime = ref({})

async function fetchOverview() {
  try {
    const res = await getDashboardOverview()
    const d = res.data
    statCards.value = [
      { label: '总用户数', value: d.totalUsers || 0, icon: '👥' },
      { label: '活跃筹款', value: d.activeCampaigns || 0, icon: '📋' },
      { label: '总筹款金额(元)', value: ((d.totalAmount || 0) / 100).toFixed(2), icon: '💰' },
      { label: '今日新增', value: d.todayNew || 0, icon: '🆕' }
    ]
    realtime.value = d
  } catch { /* ignore */ }
}

async function fetchPending() {
  try {
    const res = await getDashboardPendingCounts()
    const d = res.data
    pendingItems.value = [
      { label: '待审核筹款', count: d.pendingCampaigns || 0, route: '/review' },
      { label: '待审核提现', count: d.pendingWithdraws || 0, route: '/withdraws' },
      { label: '待处理举报', count: d.pendingReports || 0, route: '/review' }
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

async function fetchTrend() {
  try {
    const res = await getDashboardTrend({ days: 7 })
    const list = res.data || []
    const maxCount = Math.max(...list.map(t => t.count || 0), 1)
    trendData.value = list.map(t => ({
      date: t.date ? t.date.substring(5) : '',
      count: t.count || 0,
      amount: ((t.amount || 0) / 100).toFixed(2),
      barWidth: Math.max(5, ((t.count || 0) / maxCount) * 100)
    }))
  } catch { /* ignore */ }
}

function goPending(route) {
  if (route) router.push(route)
}

onMounted(() => {
  fetchOverview()
  fetchPending()
  fetchCategoryStats()
  fetchTrend()
})
</script>

<style scoped>
.stat-card { display: flex; align-items: center; gap: 12px; }
.stat-icon { font-size: 28px; }
.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: bold; color: #333; }
.stat-label { font-size: 12px; color: #999; margin-top: 4px; }
.pending-list { display: flex; flex-direction: column; gap: 10px; }
.pending-item { display: flex; justify-content: space-between; align-items: center; cursor: pointer; padding: 4px 0; }
.pending-item:hover { color: #409eff; }
.category-stats { display: flex; flex-direction: column; gap: 10px; }
.category-item { display: flex; align-items: center; gap: 8px; }
.trend-chart { max-height: 300px; overflow-y: auto; }
.trend-row { display: flex; align-items: center; gap: 8px; padding: 6px 0; }
.trend-date { width: 50px; font-size: 12px; color: #999; flex-shrink: 0; }
.trend-bar-wrap { flex: 1; height: 16px; background: #f5f5f5; border-radius: 8px; overflow: hidden; }
.trend-bar { height: 100%; background: linear-gradient(90deg, #ff4500, #ff8c00); border-radius: 8px; transition: width 0.3s; }
.trend-val { font-size: 12px; color: #666; white-space: nowrap; }
.realtime-stats { display: flex; flex-direction: column; gap: 12px; }
.rt-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.rt-label { font-size: 14px; color: #666; }
.rt-value { font-size: 18px; font-weight: bold; color: #ff4500; }
</style>