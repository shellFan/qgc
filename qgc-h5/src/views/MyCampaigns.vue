<template>
  <div class="my-campaigns-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="我发起的" fixed placeholder />

    <!-- 状态筛选 -->
    <van-tabs v-model:active="activeTab" @change="onTabChange" sticky offset-top="46">
      <van-tab title="全部" name="ALL" />
      <van-tab title="进行中" name="ACTIVE" />
      <van-tab title="已成功" name="SUCCESS" />
      <van-tab title="已关闭" name="CLOSED" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <div class="campaign-list">
          <div v-for="item in list" :key="item.id" class="campaign-card" @click="goDetail(item)">
            <div class="card-header">
              <div class="card-title">{{ item.title }}</div>
              <van-tag :type="statusType(item.status)" size="small" round>{{ statusText(item.status) }}</van-tag>
            </div>
            <div class="card-body">
              <van-progress :percentage="progressPercent(item)" :show-pivot="false" color="var(--qgc-primary)" track-color="var(--qgc-primary-light)" stroke-width="8" />
              <div class="card-stats">
                <span class="stat-amount">已筹 {{ formatMoney(item.raisedAmount) }}</span>
                <span class="stat-target">目标 {{ formatMoney(item.targetAmount) }}</span>
                <span class="stat-count">{{ item.supportCount }}人投喂</span>
              </div>
            </div>
            <div class="card-footer">
              <span class="card-time">{{ formatDate(item.createTime) }}</span>
              <div class="card-actions">
                <van-button size="small" plain type="primary" @click.stop="goManage(item)">管理</van-button>
                <van-button v-if="item.status === 'ACTIVE'" size="small" plain @click.stop="goProof(item)">返图</van-button>
                <van-button v-if="item.status === 'ACTIVE'" size="small" plain type="warning" @click.stop="goShare(item)">分享</van-button>
              </div>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- 内联空状态 -->
    <div v-if="!loading && list.length === 0" class="empty-state">
      <div class="empty-emoji">📭</div>
      <div class="empty-text">还没有发起过筹款</div>
      <van-button type="primary" round size="small" @click="$router.push('/campaign/create')">去发起</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyCampaigns } from '@/api'
import { formatMoney } from '@/utils/money'

const router = useRouter()

const activeTab = ref('ALL')
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

const statusMap = {
  PENDING_REVIEW: '待审核', ACTIVE: '进行中', REJECTED: '已拒绝',
  SUCCESS: '已成功', CLOSED: '已关闭', EXPIRED: '已过期',
  FROZEN: '已冻结'
}
const statusTypeMap = {
  PENDING_REVIEW: 'warning', ACTIVE: 'primary', REJECTED: 'danger',
  SUCCESS: 'success', CLOSED: 'default', EXPIRED: 'default', FROZEN: 'danger'
}

function statusText(s) { return statusMap[s] || s }
function statusType(s) { return statusTypeMap[s] || 'default' }

function progressPercent(item) {
  if (!item.targetAmount || item.targetAmount === 0) return 0
  return Math.min(100, Math.round((item.raisedAmount / item.targetAmount) * 100))
}

function formatDate(t) {
  if (!t) return ''
  return new Date(t).toLocaleDateString()
}

async function fetchList(reset = false) {
  if (reset) {
    page.value = 1
    list.value = []
    finished.value = false
  }
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (activeTab.value !== 'ALL') params.status = activeTab.value
    const res = await getMyCampaigns(params)
    const records = res.data?.records || res.data || []
    if (reset) {
      list.value = records
    } else {
      list.value.push(...records)
    }
    if (records.length < 10) finished.value = true
    page.value++
  } catch {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onTabChange() { fetchList(true) }
function loadMore() { fetchList() }
function onRefresh() { fetchList(true) }

function goDetail(item) { router.push(`/campaign/${item.id}`) }
function goManage(item) { router.push(`/campaign/manage/${item.id}`) }
function goProof(item) { router.push(`/proof/create/${item.id}`) }
function goShare(item) { router.push(`/campaign/${item.id}`) }

onMounted(() => { fetchList(true) })
</script>

<style scoped>
.my-campaigns-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}

.campaign-list {
  padding: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--qgc-spacing-sm);
}

.campaign-card {
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.card-title {
  font-size: var(--qgc-font-lg);
  font-weight: 600;
  flex: 1;
  line-height: 1.4;
  margin-right: var(--qgc-spacing-sm);
  color: var(--qgc-text-primary);
}

.card-body {
  margin-top: var(--qgc-spacing-md);
}
.card-stats {
  display: flex;
  justify-content: space-between;
  margin-top: var(--qgc-spacing-xs);
  font-size: var(--qgc-font-xs);
}
.stat-amount {
  color: var(--qgc-primary);
  font-weight: 700;
}
.stat-target {
  color: var(--qgc-text-tertiary);
}
.stat-count {
  color: var(--qgc-text-secondary);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--qgc-spacing-md);
  padding-top: var(--qgc-spacing-md);
  border-top: 1px solid var(--qgc-border-light);
}
.card-time {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
}
.card-actions {
  display: flex;
  gap: var(--qgc-spacing-xs);
}

/* 内联空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0 40px;
}
.empty-emoji {
  font-size: 48px;
  margin-bottom: var(--qgc-spacing-md);
}
.empty-text {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-tertiary);
  margin-bottom: var(--qgc-spacing-lg);
}
</style>