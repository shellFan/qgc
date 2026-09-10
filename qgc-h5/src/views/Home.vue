<template>
  <div class="home-page">
    <van-nav-bar fixed placeholder>
      <template #title>
        <span class="app-logo">👻 穷鬼筹</span>
      </template>
      <template #right>
        <van-icon name="bell" :badge="unreadCount > 0 ? (unreadCount > 99 ? '99+' : unreadCount) : ''" size="18" @click="$router.push('/notifications')" />
      </template>
    </van-nav-bar>

    <!-- Hero区 -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-title">V我50，帮帮穷鬼</div>
        <div class="hero-subtitle">穷鬼筹 · 你的一块钱，我的续命钱</div>
        <van-button type="primary" round size="large" class="hero-btn" @click="goCreate">
          👻 立即发起筹款
        </van-button>
      </div>
    </div>

    <!-- 分类入口 -->
    <div class="category-bar">
      <div class="category-scroll">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-item"
          :class="{ active: currentCategory === cat.id }"
          @click="currentCategory = cat.id"
        >
          <span class="cat-icon">{{ cat.icon }}</span>
          <span class="cat-name">{{ cat.shortName || cat.name }}</span>
        </div>
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 顶部广告位 -->
      <div v-if="topAd" class="ad-banner safe-padding" @click="clickAd(topAd)">
        <img :src="topAd.imageUrl" class="ad-img" />
        <span class="ad-tag">广告</span>
      </div>

      <!-- 热门筹款 -->
      <div v-if="currentCategory === 0 && hotList.length" class="section safe-padding">
        <div class="section-header">
          <span class="section-title">🔥 正在努力</span>
        </div>
        <div class="hot-scroll">
          <div v-for="item in hotList" :key="'hot-'+item.id" class="hot-card" @click="goDetail(item.id)">
            <img :src="item.cover || defaultCover" class="hot-cover" />
            <div class="hot-info">
              <div class="hot-title text-ellipsis-2">{{ item.title }}</div>
              <div class="hot-progress">
                <van-progress :percentage="progressPercent(item)" :show-pivot="false" color="var(--qgc-primary)" track-color="var(--qgc-primary-light)" stroke-width="6" />
              </div>
              <div class="hot-stats">
                <span class="hot-amount">{{ formatMoney(item.raisedAmount) }}</span>
                <span class="hot-supporters">{{ item.supportCount }}人投喂</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快成功列表 -->
      <div v-if="currentCategory === 0 && almostList.length" class="section safe-padding">
        <div class="section-header">
          <span class="section-title">🎯 马上成功</span>
        </div>
        <div class="almost-scroll">
          <div v-for="item in almostList" :key="'alm-'+item.id" class="almost-card" @click="goDetail(item.id)">
            <img :src="item.cover || defaultCover" class="almost-cover" />
            <div class="almost-badge">{{ progressPercent(item) }}%</div>
            <div class="almost-title text-ellipsis">{{ item.title }}</div>
          </div>
        </div>
      </div>

      <!-- 列表广告位 -->
      <div v-if="listAd && campaigns.length > 3" class="ad-inline safe-padding" @click="clickAd(listAd)">
        <img :src="listAd.imageUrl" class="ad-inline-img" />
        <span class="ad-tag">广告</span>
      </div>

      <!-- 最新筹款列表 -->
      <div class="section safe-padding">
        <div class="section-header">
          <span class="section-title">📋 最新筹款</span>
        </div>
        <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
          <div class="campaign-list">
            <div v-for="item in campaigns" :key="item.id" class="campaign-card qgc-card" @click="goDetail(item.id)">
              <div class="card-header">
                <img :src="item.cover || defaultCover" class="card-cover" />
                <div class="card-info">
                  <div class="card-title text-ellipsis-2">{{ item.title }}</div>
                  <div class="card-user">
                    <img :src="item.creatorAvatar || defaultAvatar" class="user-avatar" />
                    <span class="card-nickname">{{ item.creatorNickname || '穷鬼' }}</span>
                    <span v-if="item.categoryName" class="card-category">{{ item.categoryShortName || item.categoryName }}</span>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <van-progress :percentage="progressPercent(item)" :show-pivot="false" color="var(--qgc-primary)" track-color="var(--qgc-primary-light)" stroke-width="8" />
                <div class="card-stats">
                  <span class="stat-amount">已筹 {{ formatMoney(item.raisedAmount) }}</span>
                  <span class="stat-target">目标 {{ formatMoney(item.targetAmount) }}</span>
                  <span class="stat-count">{{ item.supportCount }}人支持</span>
                </div>
                <div v-if="item.endTime" class="card-time">
                  <van-count-down :time="getRemainingTime(item.endTime)" format="DD天HH时mm分" />
                </div>
              </div>
            </div>
          </div>
        </van-list>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && campaigns.length === 0 && hotList.length === 0" class="empty-section">
        <div class="empty-icon">👻</div>
        <div class="empty-title">还没有人发起筹款</div>
        <div class="empty-desc">成为第一个穷鬼，发起你的筹款吧！</div>
        <van-button type="primary" round size="small" @click="goCreate">发起筹款</van-button>
      </div>

      <!-- 玩法说明 -->
      <div v-if="currentCategory === 0" class="how-to-play safe-padding">
        <div class="section-header">
          <span class="section-title">💡 玩法说明</span>
        </div>
        <div class="play-cards">
          <div class="play-card">
            <div class="play-icon">📝</div>
            <div class="play-text">发起筹款</div>
            <div class="play-desc">填写目标金额和理由</div>
          </div>
          <div class="play-card">
            <div class="play-icon">🔗</div>
            <div class="play-text">分享传播</div>
            <div class="play-desc">转发给好友帮忙筹</div>
          </div>
          <div class="play-card">
            <div class="play-icon">💰</div>
            <div class="play-text">投喂支持</div>
            <div class="play-desc">一块也是爱，穷鬼不挑</div>
          </div>
        </div>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCampaignList, getCategoryList, getUnreadCount, getAdsByPosition, recordAdClick } from '@/api'
import { formatMoney } from '@/utils/money'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const categories = ref([{ id: 0, name: '全部', icon: '🔥', shortName: '全部' }])
const currentCategory = ref(0)
const campaigns = ref([])
const hotList = ref([])
const almostList = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const unreadCount = ref(0)
const topAd = ref(null)
const listAd = ref(null)

const defaultCover = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect fill="%23f5f5f5" width="200" height="200"/><text x="50%" y="50%" fill="%23ccc" font-size="14" text-anchor="middle">暂无图片</text></svg>'
const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40"><rect fill="%23e0e0e0" width="40" height="40" rx="20"/><text x="50%" y="55%" fill="%23999" font-size="12" text-anchor="middle">👻</text></svg>'

function progressPercent(item) {
  if (!item.targetAmount || item.targetAmount === 0) return 0
  return Math.min(100, Math.round((item.raisedAmount / item.targetAmount) * 100))
}

function getRemainingTime(endTime) {
  if (!endTime) return 0
  const diff = new Date(endTime).getTime() - Date.now()
  return Math.max(0, diff)
}

async function fetchCategories() {
  try {
    const res = await getCategoryList()
    categories.value = [{ id: 0, name: '全部', icon: '🔥', shortName: '全部' }, ...(res.data || [])]
  } catch { /* ignore */ }
}

async function fetchUnread() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
    userStore.unreadCount = res.data || 0
  } catch { /* ignore */ }
}

async function fetchHotList() {
  try {
    const res = await getCampaignList({ page: 1, size: 6, sort: 'POPULAR' })
    hotList.value = res.data?.records || res.data || []
  } catch { /* ignore */ }
}

async function fetchAlmostList() {
  try {
    const res = await getCampaignList({ page: 1, size: 6, sort: 'ALMOST' })
    almostList.value = res.data?.records || res.data || []
  } catch { /* ignore */ }
}

async function fetchAds() {
  try {
    const topRes = await getAdsByPosition('HOME_TOP')
    if (topRes?.data?.length) topAd.value = topRes.data[0]
    const listRes = await getAdsByPosition('HOME_LIST')
    if (listRes?.data?.length) listAd.value = listRes.data[0]
  } catch { /* ignore */ }
}

async function clickAd(ad) {
  try { await recordAdClick(ad.id) } catch { /* ignore */ }
  if (ad.linkUrl) window.location.href = ad.linkUrl
}

async function fetchList(reset = false) {
  if (reset) {
    page.value = 1
    campaigns.value = []
    finished.value = false
  }
  loading.value = true
  try {
    const params = { page: page.value, size: 10, sort: 'NEW' }
    if (currentCategory.value > 0) params.categoryId = currentCategory.value
    const res = await getCampaignList(params)
    const list = res.data?.records || res.data || []
    if (reset) {
      campaigns.value = list
    } else {
      campaigns.value.push(...list)
    }
    if (list.length < 10) finished.value = true
    page.value++
  } catch {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function loadMore() { fetchList() }

function onRefresh() {
  fetchList(true)
  if (currentCategory.value === 0) {
    fetchHotList()
    fetchAlmostList()
  }
}

watch(currentCategory, () => {
  fetchList(true)
  if (currentCategory.value === 0) {
    fetchHotList()
    fetchAlmostList()
  }
})

function goDetail(id) { router.push(`/campaign/${id}`) }

function goCreate() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push('/campaign/create')
}

onMounted(() => {
  fetchCategories()
  fetchUnread()
  fetchHotList()
  fetchAlmostList()
  fetchAds()
  if (userStore.isLoggedIn) userStore.fetchUserInfo()
})
</script>

<style scoped>
.home-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
  padding-bottom: calc(var(--qgc-tabbar-height) + var(--qgc-safe-bottom) + 10px);
}

.app-logo {
  font-size: var(--qgc-font-lg);
  font-weight: bold;
  color: var(--qgc-primary);
}

/* Hero区 */
.hero-section {
  background: linear-gradient(135deg, var(--qgc-primary) 0%, var(--qgc-primary-dark) 100%);
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-lg);
  margin: 0 0 var(--qgc-spacing-md);
  position: relative;
  overflow: hidden;
}
.hero-section::after {
  content: '';
  position: absolute;
  bottom: -20px;
  left: 0;
  right: 0;
  height: 20px;
  background: var(--qgc-bg);
  border-radius: 20px 20px 0 0;
}
.hero-content {
  text-align: center;
  padding: var(--qgc-spacing-lg) 0;
}
.hero-title {
  font-size: var(--qgc-font-hero);
  font-weight: 700;
  color: #fff;
  line-height: 1.3;
}
.hero-subtitle {
  font-size: var(--qgc-font-sm);
  color: rgba(255,255,255,0.85);
  margin-top: var(--qgc-spacing-sm);
}
.hero-btn {
  margin-top: var(--qgc-spacing-lg);
  width: 200px;
  font-size: var(--qgc-font-lg);
  font-weight: 600;
  background: var(--qgc-secondary) !important;
  border-color: var(--qgc-secondary) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(245,166,35,0.4);
}

/* 分类栏 */
.category-bar {
  padding: var(--qgc-spacing-sm) 0;
  background: var(--qgc-bg-white);
  margin-bottom: var(--qgc-spacing-sm);
}
.category-scroll {
  display: flex;
  gap: var(--qgc-spacing-md);
  white-space: nowrap;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  padding: var(--qgc-spacing-xs) var(--qgc-spacing-lg);
}
.category-scroll::-webkit-scrollbar { display: none; }
.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 0 0 auto;
  min-width: 56px;
  max-width: 72px;
  cursor: pointer;
  padding: var(--qgc-spacing-xs) 0;
  transition: transform 0.2s;
}
.category-item:active { transform: scale(0.95); }
.category-item.active .cat-name {
  color: var(--qgc-primary);
  font-weight: 600;
}
.category-item.active .cat-icon {
  transform: scale(1.1);
}
.cat-icon {
  font-size: 28px;
  line-height: 1;
  transition: transform 0.2s;
}
.cat-name {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-secondary);
  margin-top: 4px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 64px;
}

/* 广告 */
.ad-banner {
  position: relative;
  margin: var(--qgc-spacing-sm) 0;
  border-radius: var(--qgc-radius-md);
  overflow: hidden;
}
.ad-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}
.ad-inline {
  position: relative;
  margin: var(--qgc-spacing-sm) 0;
  border-radius: var(--qgc-radius-md);
  overflow: hidden;
}
.ad-inline-img {
  width: 100%;
  height: 80px;
  object-fit: cover;
  display: block;
}
.ad-tag {
  position: absolute;
  right: 8px;
  bottom: 8px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
}

/* 区块 */
.section {
  margin-top: var(--qgc-spacing-md);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--qgc-spacing-sm);
}
.section-title {
  font-size: var(--qgc-font-lg);
  font-weight: 600;
  color: var(--qgc-text-primary);
}

/* 热门横向滚动 */
.hot-scroll {
  display: flex;
  gap: var(--qgc-spacing-sm);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  padding-bottom: var(--qgc-spacing-xs);
  padding-left: var(--qgc-spacing-lg);
  padding-right: var(--qgc-spacing-lg);
  margin: 0 calc(-1 * var(--qgc-spacing-lg));
}
.hot-scroll::-webkit-scrollbar { display: none; }
.hot-card {
  flex: 0 0 200px;
  background: var(--qgc-bg-card);
  border-radius: var(--qgc-radius-md);
  overflow: hidden;
  box-shadow: var(--qgc-shadow-sm);
}
.hot-cover {
  width: 200px;
  height: 120px;
  object-fit: cover;
}
.hot-info {
  padding: var(--qgc-spacing-sm) var(--qgc-spacing-md);
}
.hot-title {
  font-size: var(--qgc-font-md);
  font-weight: 500;
  line-height: 1.3;
  height: 2.6em;
}
.hot-progress { margin-top: 6px; }
.hot-stats {
  display: flex;
  justify-content: space-between;
  margin-top: var(--qgc-spacing-xs);
  font-size: var(--qgc-font-sm);
}
.hot-amount {
  color: var(--qgc-primary);
  font-weight: bold;
}
.hot-supporters {
  color: var(--qgc-text-tertiary);
}

/* 快成功横向滚动 */
.almost-scroll {
  display: flex;
  gap: var(--qgc-spacing-sm);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  padding-bottom: var(--qgc-spacing-xs);
  padding-left: var(--qgc-spacing-lg);
  padding-right: var(--qgc-spacing-lg);
  margin: 0 calc(-1 * var(--qgc-spacing-lg));
}
.almost-scroll::-webkit-scrollbar { display: none; }
.almost-card {
  flex: 0 0 110px;
  background: var(--qgc-bg-card);
  border-radius: var(--qgc-radius-md);
  overflow: hidden;
  box-shadow: var(--qgc-shadow-sm);
  position: relative;
}
.almost-cover {
  width: 110px;
  height: 80px;
  object-fit: cover;
}
.almost-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: var(--qgc-primary);
  color: #fff;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: bold;
}
.almost-title {
  font-size: var(--qgc-font-sm);
  padding: 6px var(--qgc-spacing-sm);
  line-height: 1.3;
}

/* 列表 */
.campaign-list {
  display: flex;
  flex-direction: column;
  gap: var(--qgc-spacing-md);
}
.campaign-card {
  padding: var(--qgc-spacing-md);
}
.card-header {
  display: flex;
  gap: var(--qgc-spacing-sm);
}
.card-cover {
  width: 80px;
  height: 80px;
  border-radius: var(--qgc-radius-sm);
  object-fit: cover;
  background: var(--qgc-bg-grey);
  flex-shrink: 0;
}
.card-info {
  flex: 1;
  min-width: 0;
}
.card-title {
  font-size: var(--qgc-font-lg);
  font-weight: 500;
  line-height: 1.4;
}
.card-user {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: var(--qgc-spacing-sm);
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-tertiary);
}
.user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--qgc-border);
}
.card-category {
  background: var(--qgc-primary-light);
  color: var(--qgc-primary);
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
}
.card-body { margin-top: var(--qgc-spacing-sm); }
.card-stats {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-secondary);
}
.stat-amount {
  color: var(--qgc-primary);
  font-weight: bold;
}
.card-time {
  margin-top: var(--qgc-spacing-xs);
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
}

/* 空状态 */
.empty-section {
  text-align: center;
  padding: 48px var(--qgc-spacing-lg);
}
.empty-icon {
  font-size: 56px;
  line-height: 1;
  margin-bottom: var(--qgc-spacing-md);
}
.empty-title {
  font-size: var(--qgc-font-lg);
  font-weight: 600;
  color: var(--qgc-text-primary);
  margin-bottom: var(--qgc-spacing-sm);
}
.empty-desc {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-tertiary);
  margin-bottom: var(--qgc-spacing-xl);
}

/* 玩法说明 */
.how-to-play {
  margin-top: var(--qgc-spacing-xl);
  padding-bottom: var(--qgc-spacing-xl);
}
.play-cards {
  display: flex;
  gap: var(--qgc-spacing-sm);
}
.play-card {
  flex: 1;
  background: var(--qgc-bg-card);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-md) var(--qgc-spacing-sm);
  text-align: center;
  box-shadow: var(--qgc-shadow-sm);
}
.play-icon {
  font-size: 28px;
  line-height: 1;
  margin-bottom: var(--qgc-spacing-xs);
}
.play-text {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
  margin-bottom: 2px;
}
.play-desc {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  line-height: 1.3;
}
</style>