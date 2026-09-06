<template>
  <div class="home-page">
    <van-nav-bar title="穷鬼筹" fixed placeholder>
      <template #right>
        <van-icon name="bell" :badge="unreadCount > 0 ? unreadCount : ''" size="18" @click="$router.push('/notifications')" />
      </template>
    </van-nav-bar>

    <!-- 分类栏 -->
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

    <!-- 筹款列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
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
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- 发起筹款按钮 -->
    <van-button class="fab-btn" icon="plus" type="danger" round @click="goCreate" />

    <!-- 空状态 -->
    <van-empty v-if="!loading && campaigns.length === 0" description="暂无筹款，快去发起一个吧" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCampaignList, getCategoryList, getUnreadCount } from '@/api'
import { formatMoney } from '@/utils/money'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const categories = ref([{ id: 0, name: '全部', icon: '🔥' }])
const currentCategory = ref(0)
const campaigns = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const unreadCount = ref(0)

const defaultCover = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect fill="%23f5f5f5" width="200" height="200"/><text x="50%" y="50%" fill="%23ccc" font-size="14" text-anchor="middle">暂无图片</text></svg>'
const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40"><rect fill="%23e0e0e0" width="40" height="40" rx="20"/><text x="50%" y="55%" fill="%23999" font-size="12" text-anchor="middle">👻</text></svg>'

function progressPercent(item) {
  if (!item.targetAmount || item.targetAmount === 0) return 0
  return Math.min(100, Math.round((item.raisedAmount / item.targetAmount) * 100))
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

async function fetchList(reset = false) {
  if (reset) {
    page.value = 1
    campaigns.value = []
    finished.value = false
  }
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
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
}

watch(currentCategory, () => fetchList(true))

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
  if (userStore.isLoggedIn) userStore.fetchUserInfo()
})
</script>

<style scoped>
.home-page { padding: 0 12px; }
.category-bar { padding: 8px 0; background: #fff; margin: 0 -12px; padding-left: 12px; }
.category-scroll { display: flex; gap: 16px; white-space: nowrap; overflow-x: auto; -webkit-overflow-scrolling: touch; scrollbar-width: none; }
.category-scroll::-webkit-scrollbar { display: none; }
.category-item { display: flex; flex-direction: column; align-items: center; min-width: 50px; cursor: pointer; }
.category-item.active .cat-name { color: #ff4500; font-weight: bold; }
.cat-icon { font-size: 24px; }
.cat-name { font-size: 12px; color: #666; margin-top: 2px; }
.campaign-list { display: flex; flex-direction: column; gap: 12px; padding-top: 12px; }
.campaign-card { background: #fff; border-radius: 12px; padding: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.card-header { display: flex; gap: 10px; }
.card-cover { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; background: #f5f5f5; }
.card-info { flex: 1; min-width: 0; }
.card-title { font-size: 15px; font-weight: 500; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-user { display: flex; align-items: center; gap: 6px; margin-top: 8px; font-size: 12px; color: #999; }
.user-avatar { width: 20px; height: 20px; border-radius: 50%; background: #e0e0e0; }
.card-body { margin-top: 10px; }
.card-stats { display: flex; justify-content: space-between; margin-top: 6px; font-size: 12px; color: #666; }
.stat-amount { color: #ff4500; font-weight: bold; }
.fab-btn { position: fixed; right: 20px; bottom: 70px; z-index: 100; width: 50px; height: 50px; }
</style>