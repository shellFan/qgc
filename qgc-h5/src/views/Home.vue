<template>
  <div class="home-page">
    <van-nav-bar fixed placeholder>
      <template #title>
        <span class="app-logo">👻 穷鬼筹</span>
      </template>
      <template #right>
        <van-icon name="bell" :badge="unreadCount > 0 ? unreadCount : ''" size="18" @click="$router.push('/notifications')" />
      </template>
    </van-nav-bar>

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
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 顶部广告位 -->
      <div v-if="topAd" class="ad-banner" @click="clickAd(topAd)">
        <img :src="topAd.imageUrl" class="ad-img" />
        <span class="ad-tag">广告</span>
      </div>

      <!-- 热门筹款 -->
      <div v-if="currentCategory === 0 && hotList.length" class="section">
        <div class="section-header">
          <span class="section-title">🔥 正在努力</span>
          <span class="section-more" @click="scrollToHot = !scrollToHot">查看全部</span>
        </div>
        <div class="hot-scroll">
          <div v-for="item in hotList" :key="'hot-'+item.id" class="hot-card" @click="goDetail(item.id)">
            <img :src="item.cover || defaultCover" class="hot-cover" />
            <div class="hot-info">
              <div class="hot-title">{{ item.title }}</div>
              <div class="hot-progress">
                <van-progress :percentage="progressPercent(item)" :show-pivot="false" color="#ff4500" track-color="#ffe0d0" stroke-width="6" />
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
      <div v-if="currentCategory === 0 && almostList.length" class="section">
        <div class="section-header">
          <span class="section-title">🎯 马上成功</span>
        </div>
        <div class="almost-scroll">
          <div v-for="item in almostList" :key="'alm-'+item.id" class="almost-card" @click="goDetail(item.id)">
            <img :src="item.cover || defaultCover" class="almost-cover" />
            <div class="almost-badge">{{ progressPercent(item) }}%</div>
            <div class="almost-title">{{ item.title }}</div>
          </div>
        </div>
      </div>

      <!-- 列表广告位 -->
      <div v-if="listAd && campaigns.length > 3" class="ad-inline" @click="clickAd(listAd)">
        <img :src="listAd.imageUrl" class="ad-inline-img" />
        <span class="ad-tag">广告</span>
      </div>

      <!-- 最新筹款列表 -->
      <div class="section">
        <div class="section-header">
          <span class="section-title">📋 最新筹款</span>
        </div>
        <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
          <div class="campaign-list">
            <div v-for="item in campaigns" :key="item.id" class="campaign-card" @click="goDetail(item.id)">
              <div class="card-header">
                <img :src="item.cover || defaultCover" class="card-cover" />
                <div class="card-info">
                  <div class="card-title">{{ item.title }}</div>
                  <div class="card-user">
                    <img :src="item.creatorAvatar || defaultAvatar" class="user-avatar" />
                    <span>{{ item.creatorNickname || '穷鬼' }}</span>
                    <span v-if="item.categoryName" class="card-category">{{ item.categoryName }}</span>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <van-progress :percentage="progressPercent(item)" :show-pivot="false" color="#ff4500" track-color="#ffe0d0" stroke-width="8" />
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
    </van-pull-refresh>

    <!-- 发起筹款按钮 -->
    <van-button class="fab-btn" icon="plus" type="danger" round @click="goCreate" />

    <!-- 空状态 -->
    <van-empty v-if="!loading && campaigns.length === 0 && hotList.length === 0" description="暂无筹款，快去发起一个吧" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCampaignList, getCategoryList, getUnreadCount, getRandomMessages } from '@/api'
import { formatMoney } from '@/utils/money'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const categories = ref([{ id: 0, name: '全部', icon: '🔥' }])
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
    categories.value = [{ id: 0, name: '全部', icon: '🔥' }, ...(res.data || [])]
  } catch { /* ignore */ }
}

async function fetchUnread() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
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
    // 顶部广告
    const topRes = await fetch('/api/ad/position/HOME_TOP').then(r => r.json()).catch(() => null)
    if (topRes?.data?.length) topAd.value = topRes.data[0]
    // 列表广告
    const listRes = await fetch('/api/ad/position/HOME_LIST').then(r => r.json()).catch(() => null)
    if (listRes?.data?.length) listAd.value = listRes.data[0]
  } catch { /* ignore */ }
}

function clickAd(ad) {
  if (ad.linkUrl) {
    window.location.href = ad.linkUrl
  }
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

function loadMore() {
  fetchList()
}

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

function goDetail(id) {
  router.push(`/campaign/${id}`)
}

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
.home-page { padding: 0 12px 12px; }
.app-logo { font-size: 18px; font-weight: bold; }

.category-bar { padding: 8px 0; background: #fff; margin: 0 -12px; padding-left: 12px; }
.category-scroll { display: flex; gap: 16px; white-space: nowrap; overflow-x: auto; -webkit-overflow-scrolling: touch; scrollbar-width: none; }
.category-scroll::-webkit-scrollbar { display: none; }
.category-item { display: flex; flex-direction: column; align-items: center; min-width: 50px; cursor: pointer; }
.category-item.active .cat-name { color: #ff4500; font-weight: bold; }
.cat-icon { font-size: 24px; }
.cat-name { font-size: 12px; color: #666; margin-top: 2px; }

/* 广告 */
.ad-banner { position: relative; margin: 10px 0; border-radius: 12px; overflow: hidden; }
.ad-img { width: 100%; height: 120px; object-fit: cover; display: block; }
.ad-inline { position: relative; margin: 10px 0; border-radius: 8px; overflow: hidden; }
.ad-inline-img { width: 100%; height: 80px; object-fit: cover; display: block; }
.ad-tag { position: absolute; right: 8px; bottom: 8px; background: rgba(0,0,0,0.5); color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 4px; }

/* 区块 */
.section { margin-top: 14px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.section-title { font-size: 16px; font-weight: 600; }
.section-more { font-size: 12px; color: #999; }

/* 热门横向滚动 */
.hot-scroll { display: flex; gap: 10px; overflow-x: auto; -webkit-overflow-scrolling: touch; scrollbar-width: none; padding-bottom: 4px; }
.hot-scroll::-webkit-scrollbar { display: none; }
.hot-card { min-width: 200px; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.hot-cover { width: 200px; height: 120px; object-fit: cover; }
.hot-info { padding: 8px 10px; }
.hot-title { font-size: 14px; font-weight: 500; line-height: 1.3; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.hot-progress { margin-top: 6px; }
.hot-stats { display: flex; justify-content: space-between; margin-top: 4px; font-size: 12px; }
.hot-amount { color: #ff4500; font-weight: bold; }
.hot-supporters { color: #999; }

/* 快成功横向滚动 */
.almost-scroll { display: flex; gap: 10px; overflow-x: auto; -webkit-overflow-scrolling: touch; scrollbar-width: none; padding-bottom: 4px; }
.almost-scroll::-webkit-scrollbar { display: none; }
.almost-card { min-width: 110px; background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.06); position: relative; }
.almost-cover { width: 110px; height: 80px; object-fit: cover; }
.almost-badge { position: absolute; top: 4px; right: 4px; background: #ff4500; color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 4px; font-weight: bold; }
.almost-title { font-size: 12px; padding: 6px 8px; line-height: 1.3; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }

/* 列表 */
.campaign-list { display: flex; flex-direction: column; gap: 12px; }
.campaign-card { background: #fff; border-radius: 12px; padding: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.card-header { display: flex; gap: 10px; }
.card-cover { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; background: #f5f5f5; }
.card-info { flex: 1; min-width: 0; }
.card-title { font-size: 15px; font-weight: 500; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-user { display: flex; align-items: center; gap: 6px; margin-top: 8px; font-size: 12px; color: #999; }
.user-avatar { width: 20px; height: 20px; border-radius: 50%; background: #e0e0e0; }
.card-category { background: #fff0e6; color: #ff4500; font-size: 10px; padding: 1px 6px; border-radius: 4px; }
.card-body { margin-top: 10px; }
.card-stats { display: flex; justify-content: space-between; margin-top: 6px; font-size: 12px; color: #666; }
.stat-amount { color: #ff4500; font-weight: bold; }
.card-time { margin-top: 4px; font-size: 11px; color: #999; }
.fab-btn { position: fixed; right: 20px; bottom: 70px; z-index: 100; width: 50px; height: 50px; }
</style>