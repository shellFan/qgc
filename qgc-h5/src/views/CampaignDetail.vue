<template>
  <div class="detail-page">
    <van-nav-bar left-arrow @click-left="$router.back()" :title="campaign.title || '筹款详情'" fixed placeholder />

    <div v-if="campaign.id" class="detail-content">
      <!-- 封面图 -->
      <div class="cover-section">
        <img v-if="campaign.cover" :src="campaign.cover" class="detail-cover" />
        <div v-else class="cover-placeholder">
          <span class="cover-emoji">{{ campaign.categoryIcon || '🎯' }}</span>
        </div>
        <div class="cover-status">
          <van-tag :type="statusType(campaign.status)" size="medium" round>{{ statusText(campaign.status) }}</van-tag>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="detail-card">
        <h2 class="detail-title">{{ campaign.title }}</h2>
        <div class="detail-creator">
          <img :src="campaign.creatorAvatar || defaultAvatar" class="creator-avatar" />
          <span class="creator-name">{{ campaign.creatorNickname || '穷鬼' }}</span>
          <van-tag v-if="campaign.categoryName" type="primary" size="small" round class="creator-category">{{ campaign.categoryName }}</van-tag>
        </div>

        <!-- 进度 -->
        <div class="progress-section">
          <div class="progress-amount">
            <span class="raised">{{ formatMoney(campaign.raisedAmount) }}</span>
            <span class="target">目标 {{ formatMoney(campaign.targetAmount) }}</span>
          </div>
          <van-progress :percentage="progressPercent" :show-pivot="false" color="var(--qgc-primary)" track-color="var(--qgc-primary-light)" stroke-width="8" />
          <div class="progress-info">
            <span>{{ campaign.supportCount }}人支持</span>
            <span>{{ campaign.viewCount }}次浏览</span>
            <span v-if="campaign.endTime">
              <van-count-down v-if="getRemainingTime(campaign.endTime) > 0" :time="getRemainingTime(campaign.endTime)" format="剩余DD天HH时mm分" class="countdown" />
              <span v-else class="expired-text">已截止</span>
            </span>
          </div>
        </div>
      </div>

      <!-- 搞笑文案 -->
      <div v-if="funnyMessage" class="detail-card funny-card">
        <span class="funny-icon">😂</span>
        <span class="funny-text">{{ funnyMessage }}</span>
      </div>

      <!-- 详情 -->
      <div class="detail-card">
        <h3 class="card-heading">项目详情</h3>
        <p class="detail-desc">{{ campaign.description }}</p>
        <div v-if="campaign.images && campaign.images.length" class="detail-images">
          <img v-for="(img, idx) in campaign.images" :key="idx" :src="img" class="detail-img" />
        </div>
      </div>

      <!-- 支持者 -->
      <div class="detail-card">
        <h3 class="card-heading">支持者 <span class="count-badge">{{ supporters.length }}</span></h3>
        <div v-if="supporters.length === 0" class="empty-inline">
          <span class="empty-emoji">🤝</span>
          <span class="empty-text">暂无支持者，快来做第一个义父</span>
        </div>
        <div v-else class="supporter-list">
          <div v-for="s in supporters" :key="s.id" class="supporter-item">
            <img :src="s.supporterAvatar || defaultAvatar" class="supporter-avatar" />
            <div class="supporter-info">
              <span class="supporter-name">{{ s.anonymous ? '匿名义父' : (s.supporterNickname || '义父') }}</span>
              <span class="supporter-msg" v-if="s.message">{{ s.message }}</span>
            </div>
            <span class="supporter-amount">{{ s.hideAmount ? '***' : formatMoney(s.amount) }}</span>
          </div>
        </div>
      </div>

      <!-- 返图 -->
      <div class="detail-card" v-if="campaign.proofStatus === 1">
        <h3 class="card-heading">返图</h3>
        <div v-for="p in proofs" :key="p.id" class="proof-item">
          <h4 class="proof-title">{{ p.title }}</h4>
          <p class="proof-content">{{ p.content }}</p>
          <div class="proof-images" v-if="p.images && p.images.length">
            <img v-for="(img, idx) in p.images" :key="idx" :src="img.imageUrl" class="proof-img" />
          </div>
          <div class="proof-actions">
            <span @click="toggleLike(p)" class="like-btn">{{ p.liked ? '❤️' : '🤍' }} {{ p.likeCount }}</span>
          </div>
        </div>
      </div>

      <!-- 评论 -->
      <div class="detail-card">
        <h3 class="card-heading">评论 <span class="count-badge">{{ comments.length }}</span></h3>
        <div v-if="comments.length === 0" class="empty-inline">
          <span class="empty-emoji">💬</span>
          <span class="empty-text">暂无评论</span>
        </div>
        <div v-else class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <span class="comment-content">{{ c.content }}</span>
            <span class="comment-time">{{ formatTime(c.createTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 详情底部广告位 -->
      <div v-if="detailAd" class="ad-inline" @click="clickAd(detailAd)">
        <img :src="detailAd.imageUrl" class="ad-inline-img" />
        <span class="ad-tag">广告</span>
      </div>
    </div>

    <!-- 加载骨架 -->
    <div v-else class="loading-skeleton">
      <van-skeleton title :row="8" />
    </div>

    <!-- 底部操作栏 -->
    <div class="bottom-bar">
      <div class="bar-actions">
        <div class="bar-action" @click="handleShare">
          <van-icon name="share-o" size="20" />
          <span>分享</span>
        </div>
        <div class="bar-action" @click="goHome">
          <van-icon name="home-o" size="20" />
          <span>首页</span>
        </div>
      </div>
      <van-button type="primary" round class="support-btn" @click="goPay" :disabled="campaign.status !== 'ACTIVE'">
        💰 投喂穷鬼
      </van-button>
    </div>

    <!-- 分享弹窗 -->
    <van-share-sheet v-model:show="showShare" title="分享给朋友" :options="shareOptions" @select="onShareSelect" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCampaignDetail, getProofList, likeProof, unlikeProof, getCommentList, getRandomMessages } from '@/api'
import { formatMoney } from '@/utils/money'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const campaign = ref({})
const proofs = ref([])
const comments = ref([])
const supporters = ref([])
const funnyMessage = ref('')
const detailAd = ref(null)
const showShare = ref(false)

const shareOptions = [
  { name: '微信好友', icon: 'wechat' },
  { name: '朋友圈', icon: 'wechat-moments' },
  { name: '复制链接', icon: 'link' }
]

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40"><rect fill="%23e0e0e0" width="40" height="40" rx="20"/><text x="50%" y="55%" fill="%23999" font-size="12" text-anchor="middle">👻</text></svg>'

const progressPercent = computed(() => {
  if (!campaign.value.targetAmount) return 0
  return Math.min(100, Math.round((campaign.value.raisedAmount / campaign.value.targetAmount) * 100))
})

function statusText(s) {
  const map = { DRAFT: '草稿', PENDING: '审核中', ACTIVE: '进行中', SUCCESS: '已成功', CLOSED: '已关闭', EXPIRED: '已过期', REJECTED: '已拒绝', PENDING_REVIEW: '审核中' }
  return map[s] || s
}
function statusType(s) {
  const map = { ACTIVE: 'success', SUCCESS: 'success', CLOSED: 'danger', EXPIRED: 'warning', REJECTED: 'danger' }
  return map[s] || 'primary'
}
function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleDateString()
}
function getRemainingTime(endTime) {
  if (!endTime) return 0
  return Math.max(0, new Date(endTime).getTime() - Date.now())
}

async function fetchDetail() {
  const id = route.params.id
  const res = await getCampaignDetail(id)
  campaign.value = res.data
  supporters.value = res.data?.supporters || []
}

async function fetchFunnyMessage() {
  try {
    const res = await getRandomMessages()
    const msgs = res.data || []
    if (msgs.length) funnyMessage.value = msgs[Math.floor(Math.random() * msgs.length)]
  } catch { /* ignore */ }
}

async function fetchProofs() {
  try {
    const res = await getProofList(route.params.id)
    proofs.value = res.data || []
  } catch { /* ignore */ }
}

async function fetchComments() {
  try {
    const res = await getCommentList(route.params.id, { page: 1, size: 20 })
    comments.value = res.data?.records || res.data || []
  } catch { /* ignore */ }
}

async function fetchDetailAd() {
  try {
    const res = await fetch('/api/ad/position/DETAIL_BOTTOM').then(r => r.json()).catch(() => null)
    if (res?.data?.length) detailAd.value = res.data[0]
  } catch { /* ignore */ }
}

function clickAd(ad) {
  if (ad.linkUrl) window.location.href = ad.linkUrl
}

async function toggleLike(proof) {
  try {
    if (proof.liked) {
      await unlikeProof(proof.id)
      proof.liked = false
      proof.likeCount--
    } else {
      await likeProof(proof.id)
      proof.liked = true
      proof.likeCount++
    }
  } catch { /* ignore */ }
}

function goPay() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push({ path: '/pay', query: { campaignId: route.params.id } })
}

function goHome() {
  router.push('/')
}

function handleShare() {
  showShare.value = true
}

function onShareSelect(option) {
  if (option.name === '复制链接') {
    navigator.clipboard?.writeText(window.location.href)
    showToast('链接已复制')
  }
  showShare.value = false
}

onMounted(() => {
  fetchDetail()
  fetchFunnyMessage()
  fetchProofs()
  fetchComments()
  fetchDetailAd()
})
</script>

<style scoped>
.detail-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
  padding-bottom: calc(60px + var(--qgc-safe-bottom));
}

/* 封面 */
.cover-section { position: relative; }
.detail-cover {
  width: 100%;
  max-height: 220px;
  object-fit: cover;
  display: block;
}
.cover-placeholder {
  width: 100%;
  height: 160px;
  background: linear-gradient(135deg, var(--qgc-primary), var(--qgc-primary-dark));
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-emoji { font-size: 48px; }
.cover-status {
  position: absolute;
  top: var(--qgc-spacing-md);
  right: var(--qgc-spacing-md);
}

/* 卡片 */
.detail-card {
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}
.card-heading {
  font-size: var(--qgc-font-lg);
  font-weight: 600;
  color: var(--qgc-text-primary);
  margin-bottom: var(--qgc-spacing-md);
}
.detail-title {
  font-size: var(--qgc-font-xl);
  font-weight: 700;
  line-height: 1.4;
  color: var(--qgc-text-primary);
}

/* 创建者 */
.detail-creator {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-sm);
  margin-top: var(--qgc-spacing-sm);
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-secondary);
}
.creator-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}
.creator-name { font-weight: 500; }
.creator-category { margin-left: auto; }

/* 进度 */
.progress-section { margin-top: var(--qgc-spacing-md); }
.progress-amount {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: var(--qgc-spacing-xs);
}
.raised {
  font-size: 22px;
  font-weight: 700;
  color: var(--qgc-primary);
}
.target {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
}
.progress-info {
  display: flex;
  gap: var(--qgc-spacing-md);
  margin-top: var(--qgc-spacing-xs);
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
}
.countdown { font-size: var(--qgc-font-xs); color: var(--qgc-secondary); }
.expired-text { color: var(--qgc-danger); font-weight: 500; }

/* 搞笑文案 */
.funny-card {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-sm);
  background: var(--qgc-secondary-light);
  border: 1px solid var(--qgc-secondary-border);
}
.funny-icon { font-size: 20px; }
.funny-text {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-secondary-dark);
  line-height: 1.4;
}

/* 详情 */
.detail-desc {
  font-size: var(--qgc-font-md);
  line-height: 1.7;
  color: var(--qgc-text-primary);
  white-space: pre-wrap;
}
.detail-images { margin-top: var(--qgc-spacing-md); }
.detail-img {
  width: 100%;
  border-radius: var(--qgc-radius-sm);
  margin-bottom: var(--qgc-spacing-sm);
}

/* 支持者 */
.supporter-list {
  display: flex;
  flex-direction: column;
  gap: var(--qgc-spacing-sm);
}
.supporter-item {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-sm);
}
.supporter-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
}
.supporter-info { flex: 1; min-width: 0; }
.supporter-name {
  font-size: var(--qgc-font-sm);
  font-weight: 500;
  color: var(--qgc-text-primary);
}
.supporter-msg {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.supporter-amount {
  font-size: var(--qgc-font-md);
  color: var(--qgc-primary);
  font-weight: 600;
  flex-shrink: 0;
}
.count-badge {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  font-weight: normal;
}

/* 返图 */
.proof-item {
  padding: var(--qgc-spacing-md) 0;
  border-bottom: 1px solid var(--qgc-border-light);
}
.proof-item:last-child { border-bottom: none; }
.proof-title {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  margin-bottom: var(--qgc-spacing-xs);
}
.proof-content {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-secondary);
  line-height: 1.5;
}
.proof-images {
  display: flex;
  gap: var(--qgc-spacing-xs);
  flex-wrap: wrap;
  margin-top: var(--qgc-spacing-sm);
}
.proof-img {
  width: 100px;
  height: 100px;
  border-radius: var(--qgc-radius-sm);
  object-fit: cover;
}
.proof-actions {
  margin-top: var(--qgc-spacing-xs);
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-tertiary);
}
.like-btn { cursor: pointer; }

/* 评论 */
.comment-item {
  padding: var(--qgc-spacing-sm) 0;
  border-bottom: 1px solid var(--qgc-border-light);
}
.comment-item:last-child { border-bottom: none; }
.comment-content {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-primary);
}
.comment-time {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  display: block;
  margin-top: 2px;
}

/* 空状态内联 */
.empty-inline {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-sm);
  padding: var(--qgc-spacing-md) 0;
  color: var(--qgc-text-tertiary);
}
.empty-emoji { font-size: 24px; }
.empty-text { font-size: var(--qgc-font-sm); }

/* 广告 */
.ad-inline {
  position: relative;
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-sm);
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

/* 加载骨架 */
.loading-skeleton {
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-md);
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  padding-bottom: calc(var(--qgc-spacing-sm) + var(--qgc-safe-bottom));
  background: var(--qgc-bg-white);
  box-shadow: 0 -1px 4px rgba(0,0,0,0.06);
  z-index: 99;
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-md);
}
.bar-actions {
  display: flex;
  gap: var(--qgc-spacing-md);
}
.bar-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  cursor: pointer;
  min-width: 40px;
}
.support-btn {
  flex: 1;
  height: 44px;
  font-size: var(--qgc-font-md);
  font-weight: 600;
}
</style>