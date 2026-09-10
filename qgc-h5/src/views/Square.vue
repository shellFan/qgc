<template>
  <div class="square-page">
    <van-nav-bar title="穷鬼广场" fixed placeholder />

    <!-- 分类标签 -->
    <div class="category-bar">
      <div class="category-scroll">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="category-item"
          :class="{ active: currentTab === tab.key }"
          @click="currentTab = tab.key"
        >
          <span>{{ tab.label }}</span>
        </div>
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <div class="feed-list safe-padding">
          <!-- 返图动态 -->
          <template v-if="currentTab === 'proof'">
            <div v-for="item in list" :key="'p-'+item.id" class="feed-card qgc-card" @click="goCampaign(item.campaignId)">
              <div class="feed-header">
                <img :src="item.creatorAvatar || defaultAvatar" class="feed-avatar" />
                <div class="feed-user">
                  <span class="feed-name">{{ item.creatorNickname || '穷鬼' }}</span>
                  <span class="feed-time">{{ formatTime(item.createTime) }}</span>
                </div>
              </div>
              <div class="feed-title text-ellipsis-2">{{ item.title }}</div>
              <div class="feed-content text-ellipsis-2">{{ item.content }}</div>
              <div v-if="item.images && item.images.length" class="feed-images">
                <img v-for="(img, idx) in item.images.slice(0, 3)" :key="idx" :src="img" class="feed-img" />
              </div>
              <div class="feed-footer">
                <span class="feed-stat">❤️ {{ item.likeCount || 0 }}</span>
                <span class="feed-stat">💬 {{ item.commentCount || 0 }}</span>
              </div>
            </div>
          </template>

          <!-- 筹款动态 -->
          <template v-else>
            <div v-for="item in list" :key="'c-'+item.id" class="feed-card qgc-card" @click="goCampaign(item.id)">
              <div class="feed-header">
                <img :src="item.creatorAvatar || defaultAvatar" class="feed-avatar" />
                <div class="feed-user">
                  <span class="feed-name">{{ item.creatorNickname || '穷鬼' }}</span>
                  <span class="feed-time">{{ formatTime(item.createTime) }}</span>
                </div>
                <van-tag v-if="item.categoryName" type="primary" size="small" class="feed-tag">{{ item.categoryShortName || item.categoryName }}</van-tag>
              </div>
              <div class="feed-title text-ellipsis-2">{{ item.title }}</div>
              <div class="feed-content text-ellipsis-2">{{ item.description }}</div>
              <div class="feed-progress">
                <van-progress :percentage="progressPercent(item)" :show-pivot="false" color="var(--qgc-primary)" track-color="var(--qgc-primary-light)" stroke-width="6" />
                <div class="feed-stats-row">
                  <span class="stat-amount">已筹 {{ formatMoney(item.raisedAmount) }}</span>
                  <span class="stat-target">目标 {{ formatMoney(item.targetAmount) }}</span>
                  <span class="stat-count">{{ item.supportCount }}人投喂</span>
                </div>
              </div>
            </div>
          </template>
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- 空状态 -->
    <div v-if="!loading && list.length === 0" class="empty-section">
      <div class="empty-icon">📢</div>
      <div class="empty-title">广场还很安静</div>
      <div class="empty-desc">快去发起筹款，成为第一个穷鬼吧！</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCampaignList, getProofList } from '@/api'
import { formatMoney } from '@/utils/money'

const router = useRouter()

const tabs = [
  { key: 'latest', label: '最新' },
  { key: 'popular', label: '热门' },
  { key: 'almost', label: '快成功' },
  { key: 'proof', label: '返图' }
]

const currentTab = ref('latest')
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40"><rect fill="%23e0e0e0" width="40" height="40" rx="20"/><text x="50%" y="55%" fill="%23999" font-size="12" text-anchor="middle">👻</text></svg>'

function progressPercent(item) {
  if (!item.targetAmount || item.targetAmount === 0) return 0
  return Math.min(100, Math.round((item.raisedAmount / item.targetAmount) * 100))
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = Date.now()
  const diff = now - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString()
}

async function fetchList(reset = false) {
  if (reset) {
    page.value = 1
    list.value = []
    finished.value = false
  }
  loading.value = true
  try {
    if (currentTab.value === 'proof') {
      const campaignRes = await getCampaignList({ page: page.value, size: 10, sort: 'NEW' })
      const campaigns = campaignRes.data?.records || campaignRes.data || []
      if (campaigns.length === 0) {
        finished.value = true
      } else {
        for (const c of campaigns) {
          try {
            const proofRes = await getProofList(c.id)
            const proofs = proofRes?.data || []
            list.value.push(...proofs)
          } catch { /* ignore */ }
        }
        if (campaigns.length < 10) finished.value = true
        page.value++
      }
    } else {
      const sortMap = { latest: 'NEW', popular: 'POPULAR', almost: 'ALMOST' }
      const params = { page: page.value, size: 10, sort: sortMap[currentTab.value] || 'NEW' }
      const res = await getCampaignList(params)
      const records = res.data?.records || res.data || []
      if (reset) {
        list.value = records
      } else {
        list.value.push(...records)
      }
      if (records.length < 10) finished.value = true
      page.value++
    }
  } catch {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function loadMore() { fetchList() }
function onRefresh() { fetchList(true) }
watch(currentTab, () => { fetchList(true) })
function goCampaign(id) { if (id) router.push(`/campaign/${id}`) }

onMounted(() => { fetchList(true) })
</script>

<style scoped>
.square-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
  padding-bottom: calc(var(--qgc-tabbar-height) + var(--qgc-safe-bottom) + 10px);
}

/* 分类标签 */
.category-bar {
  padding: var(--qgc-spacing-sm) 0;
  background: var(--qgc-bg-white);
  margin-bottom: var(--qgc-spacing-sm);
}
.category-scroll {
  display: flex;
  gap: var(--qgc-spacing-sm);
  white-space: nowrap;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  padding: var(--qgc-spacing-xs) var(--qgc-spacing-lg);
}
.category-scroll::-webkit-scrollbar { display: none; }
.category-item {
  padding: 6px 16px;
  border-radius: var(--qgc-radius-full);
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-secondary);
  background: var(--qgc-bg-grey);
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}
.category-item.active {
  background: var(--qgc-primary-light);
  color: var(--qgc-primary);
  font-weight: 600;
}

/* Feed列表 */
.feed-list {
  display: flex;
  flex-direction: column;
  gap: var(--qgc-spacing-md);
  margin-top: var(--qgc-spacing-sm);
}
.feed-card {
  padding: var(--qgc-spacing-md);
}
.feed-header {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-sm);
}
.feed-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--qgc-border);
  flex-shrink: 0;
}
.feed-user { flex: 1; min-width: 0; }
.feed-name {
  font-size: var(--qgc-font-md);
  font-weight: 500;
  display: block;
  color: var(--qgc-text-primary);
}
.feed-time {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
}
.feed-tag {
  flex-shrink: 0;
}
.feed-title {
  font-size: var(--qgc-font-lg);
  font-weight: 600;
  margin-top: var(--qgc-spacing-sm);
  line-height: 1.4;
  color: var(--qgc-text-primary);
}
.feed-content {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-secondary);
  margin-top: var(--qgc-spacing-xs);
  line-height: 1.5;
}
.feed-images {
  display: flex;
  gap: 6px;
  margin-top: var(--qgc-spacing-sm);
}
.feed-img {
  width: 100px;
  height: 100px;
  border-radius: var(--qgc-radius-sm);
  object-fit: cover;
}
.feed-footer {
  display: flex;
  gap: var(--qgc-spacing-lg);
  margin-top: var(--qgc-spacing-sm);
}
.feed-stat {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-tertiary);
}
.feed-progress { margin-top: var(--qgc-spacing-sm); }
.feed-stats-row {
  display: flex;
  justify-content: space-between;
  margin-top: var(--qgc-spacing-xs);
  font-size: var(--qgc-font-sm);
}
.stat-amount {
  color: var(--qgc-primary);
  font-weight: bold;
}
.stat-target { color: var(--qgc-text-tertiary); }
.stat-count { color: var(--qgc-text-secondary); }

/* 空状态 */
.empty-section {
  text-align: center;
  padding: 48px var(--qgc-spacing-lg);
}
.empty-icon { font-size: 56px; line-height: 1; margin-bottom: var(--qgc-spacing-md); }
.empty-title { font-size: var(--qgc-font-lg); font-weight: 600; color: var(--qgc-text-primary); margin-bottom: var(--qgc-spacing-sm); }
.empty-desc { font-size: var(--qgc-font-sm); color: var(--qgc-text-tertiary); }
</style>