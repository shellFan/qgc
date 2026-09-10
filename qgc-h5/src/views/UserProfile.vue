<template>
  <div class="profile-page">
    <van-nav-bar left-arrow @click-left="$router.back()" :title="isMine ? '我的主页' : '用户主页'" fixed placeholder />

    <!-- 用户头部 -->
    <div class="profile-header">
      <img :src="profile.avatar || defaultAvatar" class="profile-avatar" />
      <div class="profile-info">
        <h2 class="profile-name">{{ profile.nickname || '穷鬼' }}</h2>
        <div class="profile-level" v-if="levelInfo">
          <van-tag type="warning" size="medium" round>Lv{{ levelInfo.level }} {{ levelInfo.title }}</van-tag>
        </div>
        <div class="profile-joined" v-if="profile.createTime">加入于 {{ formatDate(profile.createTime) }}</div>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-row" v-if="isMine">
      <div class="stat-item" @click="showPoints = true">
        <span class="stat-value">{{ pointsInfo.points || 0 }}</span>
        <span class="stat-label">积分</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="stat-value">{{ levelInfo?.level || 1 }}</span>
        <span class="stat-label">等级</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item" @click="showBadges = true">
        <span class="stat-value">{{ badges.length }}</span>
        <span class="stat-label">徽章</span>
      </div>
    </div>

    <!-- 徽章墙 -->
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
        <van-progress :percentage="levelPercent" :show-pivot="false" color="var(--qgc-primary)" track-color="var(--qgc-primary-light)" stroke-width="12" />
        <div class="level-text">{{ levelInfo.exp || 0 }} / {{ nextLevelExp }} 经验</div>
      </div>
    </div>

    <!-- 积分明细 -->
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
        <div v-if="pointsFlow.length === 0" class="empty-state">
          <span class="empty-emoji">💰</span>
          <p>暂无积分记录</p>
        </div>
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
        <div v-if="badges.length === 0" class="empty-state">
          <span class="empty-emoji">🏅</span>
          <p>暂无徽章</p>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import { getMyProfile, getUserProfile, getMyLevel, getMyBadges, getMyPoints, getMyPointsFlow } from '@/api'
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
    const res = isMine.value ? await getMyProfile() : await getUserProfile(userId.value)
    profile.value = res?.data || {}
  } catch { showToast('获取用户信息失败') }
}

async function fetchLevel() {
  try {
    const res = await getMyLevel()
    levelInfo.value = res?.data || null
  } catch { /* level is optional */ }
}

async function fetchBadges() {
  try {
    const res = await getMyBadges()
    badges.value = res?.data || []
  } catch { /* badges is optional */ }
}

async function fetchPoints() {
  if (!isMine.value) return
  try {
    const res = await getMyPoints()
    pointsInfo.value = res?.data || {}
  } catch { /* points is optional */ }
}

async function fetchPointsFlow() {
  if (!isMine.value) return
  try {
    const res = await getMyPointsFlow()
    pointsFlow.value = res?.data?.records || res?.data || []
  } catch { /* points flow is optional */ }
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
.profile-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}

/* 头部 */
.profile-header {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-md);
  padding: var(--qgc-spacing-lg) var(--qgc-spacing-md);
  background: linear-gradient(135deg, var(--qgc-primary), var(--qgc-primary-dark));
  color: #fff;
}
.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}
.profile-info {
  flex: 1;
  min-width: 0;
}
.profile-name {
  font-size: var(--qgc-font-xl);
  font-weight: 700;
  margin: 0;
  color: #fff;
}
.profile-level {
  margin-top: 6px;
}
.profile-joined {
  font-size: var(--qgc-font-xs);
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

/* 统计栏 */
.stats-row {
  display: flex;
  align-items: center;
  background: var(--qgc-bg-white);
  padding: var(--qgc-spacing-md) 0;
  box-shadow: var(--qgc-shadow-sm);
}
.stat-item {
  flex: 1;
  text-align: center;
  cursor: pointer;
}
.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--qgc-primary);
}
.stat-label {
  display: block;
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: 2px;
}
.stat-divider {
  width: 1px;
  height: 24px;
  background: var(--qgc-border-light);
}

/* 通用 section */
.section {
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--qgc-spacing-md);
}
.section-title {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
}
.section-close {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  cursor: pointer;
}

/* 徽章 */
.badge-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--qgc-spacing-md);
}
.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
}
.badge-icon {
  font-size: 28px;
}
.badge-name {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-secondary);
  margin-top: 2px;
}

/* 等级进度 */
.level-progress {
  margin-top: 4px;
}
.level-text {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: 6px;
  text-align: center;
}

/* 积分列表 */
.points-list {
  max-height: 300px;
  overflow-y: auto;
}
.points-item {
  display: flex;
  justify-content: space-between;
  padding: var(--qgc-spacing-sm) 0;
  border-bottom: 1px solid var(--qgc-border-light);
}
.points-item:last-child {
  border-bottom: none;
}
.points-remark {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-primary);
}
.points-amount {
  font-size: var(--qgc-font-md);
  font-weight: 500;
  color: var(--qgc-text-tertiary);
}
.points-amount.positive {
  color: var(--qgc-primary);
}

/* 徽章弹窗 */
.badge-dialog {
  padding: var(--qgc-spacing-lg);
  max-height: 400px;
  overflow-y: auto;
}
.badge-dialog-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: var(--qgc-spacing-sm) 0;
  border-bottom: 1px solid var(--qgc-border-light);
}
.badge-dialog-item:last-child {
  border-bottom: none;
}
.badge-dialog-info {
  flex: 1;
}
.badge-time {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  display: block;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 0;
  color: var(--qgc-text-tertiary);
}
.empty-emoji {
  font-size: 48px;
  margin-bottom: var(--qgc-spacing-sm);
}
.empty-state p {
  font-size: var(--qgc-font-md);
  margin: 0;
}
</style>