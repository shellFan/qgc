<template>
  <div class="profile-page">
    <van-nav-bar left-arrow @click-left="$router.back()" :title="isMine ? '我的主页' : '用户主页'" fixed placeholder />

    <div class="profile-header">
      <img :src="profile.avatar || defaultAvatar" class="profile-avatar" />
      <div class="profile-info">
        <h2 class="profile-name">{{ profile.nickname || '穷鬼' }}</h2>
        <div class="profile-level" v-if="levelInfo">
          <van-tag type="warning" size="medium">Lv{{ levelInfo.level }} {{ levelInfo.title }}</van-tag>
        </div>
        <div class="profile-joined" v-if="profile.createTime">加入于 {{ formatDate(profile.createTime) }}</div>
      </div>
    </div>

    <!-- 积分/等级/徽章 统计 -->
    <div class="stats-row" v-if="isMine">
      <div class="stat-item" @click="showPoints = true">
        <span class="stat-value">{{ pointsInfo.points || 0 }}</span>
        <span class="stat-label">积分</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ levelInfo?.level || 1 }}</span>
        <span class="stat-label">等级</span>
      </div>
      <div class="stat-item" @click="showBadges = true">
        <span class="stat-value">{{ badges.length }}</span>
        <span class="stat-label">徽章</span>
      </div>
    </div>

    <!-- 徽章展示 -->
    <div class="section" v-if="badges.length > 0">
      <div class="section-header">
        <span class="section-title">🏅 徽章墙</span>
      </div>
      <div class="badge-grid">
        <div v-for="b in badges" :key="b.id" class="badge-item">
          <span class="badge-icon">{{ b.badgeIcon }}</span>
          <span class="badge-name">{{ b.badgeName }}</span>
        </div>
      </div>
    </div>

    <!-- 等级进度 -->
    <div class="section" v-if="isMine && levelInfo">
      <div class="section-header">
        <span class="section-title">📊 等级进度</span>
      </div>
      <div class="level-progress">
        <div class="level-bar">
          <van-progress :percentage="levelPercent" :show-pivot="false" color="#ff4500" track-color="#ffe0d0" stroke-width="12" />
        </div>
        <div class="level-text">{{ levelInfo.exp || 0 }} / {{ nextLevelExp }} 经验</div>
      </div>
    </div>

    <!-- 积分流水 -->
    <div class="section" v-if="isMine && showPoints">
      <div class="section-header">
        <span class="section-title">💰 积分明细</span>
        <span class="section-close" @click="showPoints = false">收起</span>
      </div>
      <div class="points-list">
        <div v-for="f in pointsFlow" :key="f.id" class="points-item">
          <span class="points-remark">{{ f.remark }}</span>
          <span class="points-amount" :class="{ positive: f.amount > 0 }">{{ f.amount > 0 ? '+' : '' }}{{ f.amount }}</span>
        </div>
        <van-empty v-if="pointsFlow.length === 0" description="暂无积分记录" :image-size="60" />
      </div>
    </div>

    <!-- 徽章详情弹窗 -->
    <van-dialog v-model:show="showBadges" title="我的徽章" :show-confirm-button="false">
      <div class="badge-dialog">
        <div v-for="b in badges" :key="b.id" class="badge-dialog-item">
          <span class="badge-icon">{{ b.badgeIcon }}</span>
          <div class="badge-dialog-info">
            <span class="badge-name">{{ b.badgeName }}</span>
            <span class="badge-time">{{ formatDate(b.earnedTime) }}</span>
          </div>
        </div>
        <van-empty v-if="badges.length === 0" description="暂无徽章" :image-size="60" />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const userId = ref(route.params.userId || route.query.userId)
const isMine = computed(() => !userId.value || userId.value === 'mine')
const profile = ref({})
const levelInfo = ref(null)
const badges = ref([])
const pointsInfo = ref({})
const pointsFlow = ref([])
const showPoints = ref(false)
const showBadges = ref(false)

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40"><rect fill="%23e0e0e0" width="40" height="40" rx="20"/><text x="50%" y="55%" fill="%23999" font-size="12" text-anchor="middle">👻</text></svg>'

const LEVEL_EXP = [0, 100, 500, 2000]
const levelPercent = computed(() => {
  if (!levelInfo.value) return 0
  const lv = levelInfo.value.level || 1
  const exp = levelInfo.value.exp || 0
  const currentMin = LEVEL_EXP[lv - 1] || 0
  const nextMin = LEVEL_EXP[lv] || 2000
  if (nextMin === currentMin) return 100
  return Math.min(100, Math.round(((exp - currentMin) / (nextMin - currentMin)) * 100))
})
const nextLevelExp = computed(() => {
  const lv = levelInfo.value?.level || 1
  return LEVEL_EXP[lv] || 2000
})

function formatDate(t) {
  if (!t) return ''
  return new Date(t).toLocaleDateString()
}

async function fetchProfile() {
  try {
    const url = isMine.value ? '/api/user/profile/mine' : `/api/user/profile/${userId.value}`
    const res = await fetch(url).then(r => r.json())
    profile.value = res?.data || {}
  } catch { /* ignore */ }
}

async function fetchLevel() {
  try {
    const url = isMine.value ? '/api/user/level/mine' : `/api/user/level/${userId.value}`
    const res = await fetch(url).then(r => r.json())
    levelInfo.value = res?.data || null
  } catch { /* ignore */ }
}

async function fetchBadges() {
  try {
    const url = isMine.value ? '/api/user/badges/mine' : `/api/user/badges/${userId.value}`
    const res = await fetch(url).then(r => r.json())
    badges.value = res?.data || []
  } catch { /* ignore */ }
}

async function fetchPoints() {
  if (!isMine.value) return
  try {
    const res = await fetch('/api/user/points/mine').then(r => r.json())
    pointsInfo.value = res?.data || {}
  } catch { /* ignore */ }
}

async function fetchPointsFlow() {
  if (!isMine.value) return
  try {
    const res = await fetch('/api/user/points/mine/flow?page=1&size=20').then(r => r.json())
    pointsFlow.value = res?.data?.records || res?.data || []
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchProfile()
  fetchLevel()
  fetchBadges()
  fetchPoints()
  fetchPointsFlow()
})
</script>

<style scoped>
.profile-page { min-height: 100vh; background: #f5f5f5; }
.profile-header { display: flex; align-items: center; gap: 14px; padding: 20px; background: #fff; }
.profile-avatar { width: 64px; height: 64px; border-radius: 50%; }
.profile-info { flex: 1; }
.profile-name { font-size: 18px; font-weight: 600; margin: 0; }
.profile-level { margin-top: 6px; }
.profile-joined { font-size: 12px; color: #999; margin-top: 4px; }
.stats-row { display: flex; background: #fff; padding: 14px 0; margin-top: 1px; border-top: 1px solid #f0f0f0; }
.stat-item { flex: 1; text-align: center; cursor: pointer; }
.stat-value { display: block; font-size: 18px; font-weight: bold; color: #ff4500; }
.stat-label { display: block; font-size: 12px; color: #999; margin-top: 2px; }
.section { margin: 10px 12px; background: #fff; border-radius: 12px; padding: 14px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.section-title { font-size: 15px; font-weight: 600; }
.section-close { font-size: 12px; color: #999; }
.badge-grid { display: flex; flex-wrap: wrap; gap: 12px; }
.badge-item { display: flex; flex-direction: column; align-items: center; min-width: 60px; }
.badge-icon { font-size: 28px; }
.badge-name { font-size: 11px; color: #666; margin-top: 2px; }
.level-progress { margin-top: 4px; }
.level-text { font-size: 12px; color: #999; margin-top: 6px; text-align: center; }
.points-list { max-height: 300px; overflow-y: auto; }
.points-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.points-remark { font-size: 13px; color: #333; }
.points-amount { font-size: 13px; font-weight: 500; color: #999; }
.points-amount.positive { color: #ff4500; }
.badge-dialog { padding: 16px; max-height: 400px; overflow-y: auto; }
.badge-dialog-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.badge-dialog-info { flex: 1; }
.badge-time { font-size: 11px; color: #999; display: block; }
</style>