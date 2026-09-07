<template>
  <div class="detail-page">
    <van-nav-bar left-arrow @click-left="$router.back()" :title="campaign.title || '筹款详情'" fixed placeholder />

    <div v-if="campaign.id" class="detail-content">
      <!-- 封面图 -->
      <div class="cover-section">
        <img v-if="campaign.cover" :src="campaign.cover" class="detail-cover" />
        <div class="cover-status">
          <van-tag :type="statusType(campaign.status)" size="medium">{{ statusText(campaign.status) }}</van-tag>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="detail-card">
        <h2 class="detail-title">{{ campaign.title }}</h2>
        <div class="detail-creator">
          <img :src="campaign.creatorAvatar || defaultAvatar" class="creator-avatar" />
          <span>{{ campaign.creatorNickname || '穷鬼' }}</span>
          <span v-if="campaign.categoryName" class="creator-category">{{ campaign.categoryName }}</span>
        </div>

        <!-- 进度 -->
        <div class="progress-section">
          <div class="progress-amount">
            <span class="raised">{{ formatMoney(campaign.raisedAmount) }}</span>
            <span class="target">目标 {{ formatMoney(campaign.targetAmount) }}</span>
          </div>
          <van-progress :percentage="progressPercent" :show-pivot="false" color="#ff4500" track-color="#ffe0d0" stroke-width="10" />
          <div class="progress-info">
            <span>{{ campaign.supportCount }}人支持</span>
            <span>{{ campaign.viewCount }}次浏览</span>
            <span v-if="campaign.endTime">
              <van-count-down v-if="getRemainingTime(campaign.endTime) > 0" :time="getRemainingTime(campaign.endTime)" format="剩余DD天HH时mm分" />
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
        <h3>项目详情</h3>
        <p class="detail-desc">{{ campaign.description }}</p>
        <div v-if="campaign.images && campaign.images.length" class="detail-images">
          <img v-for="(img, idx) in campaign.images" :key="idx" :src="img" class="detail-img" />
        </div>
      </div>

      <!-- 支持者 -->
      <div class="detail-card">
        <h3>支持者 <span class="count-badge">{{ supporters.length }}</span></h3>
        <van-empty v-if="supporters.length === 0" description="暂无支持者，快来做第一个义父" :image-size="60" />
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
        <h3>返图</h3>
        <div v-for="p in proofs" :key="p.id" class="proof-item">
          <h4>{{ p.title }}</h4>
          <p>{{ p.content }}</p>
          <div class="proof-images" v-if="p.images && p.images.length">
            <img v-for="(img, idx) in p.images" :key="idx" :src="img.imageUrl" class="proof-img" />
          </div>
          <div class="proof-actions">
            <span @click="toggleLike(p)">{{ p.liked ? '❤️' : '🤍' }} {{ p.likeCount }}</span>
          </div>
        </div>
      </div>

      <!-- 评论 -->
      <div class="detail-card">
        <h3>评论 <span class="count-badge">{{ comments.length }}</span></h3>
        <van-empty v-if="comments.length === 0" description="暂无评论" :image-size="60" />
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

    <!-- 底部操作栏 -->
    <div class="bottom-bar">
      <div class="bar-actions">
        <div class="bar-action" @click="handleShare">
          <van-icon name="share-o" size="20" />
          <span>分享</span>
        </div>
      </div>
      <van-button type="danger" round class="support-btn" @click="goPay" :disabled="campaign.status !== 'ACTIVE'">
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
.detail-page { padding-bottom: 70px; }
.cover-section { position: relative; }
.detail-cover { width: 100%; max-height: 240px; object-fit: cover; }
.cover-status { position: absolute; top: 10px; right: 10px; }
.detail-card { background: #fff; margin: 10px 12px; border-radius: 12px; padding: 14px; }
.detail-title { font-size: 18px; font-weight: 600; line-height: 1.4; }
.detail-creator { display: flex; align-items: center; gap: 8px; margin-top: 8px; font-size: 13px; color: #666; }
.creator-avatar { width: 24px; height: 24px; border-radius: 50%; }
.creator-category { background: #fff0e6; color: #ff4500; font-size: 10px; padding: 1px 6px; border-radius: 4px; }
.progress-section { margin-top: 14px; }
.progress-amount { display: flex; justify-content: space-between; margin-bottom: 6px; }
.raised { font-size: 20px; font-weight: bold; color: #ff4500; }
.target { font-size: 13px; color: #999; align-self: flex-end; }
.progress-info { display: flex; gap: 12px; margin-top: 6px; font-size: 12px; color: #999; }
.expired-text { color: #ff4500; font-weight: 500; }

/* 搞笑文案 */
.funny-card { display: flex; align-items: center; gap: 8px; background: #fff9e6; border: 1px solid #ffe0b2; }
.funny-icon { font-size: 20px; }
.funny-text { font-size: 13px; color: #e65100; line-height: 1.4; }

.detail-desc { font-size: 14px; line-height: 1.6; color: #333; white-space: pre-wrap; }
.detail-images { margin-top: 10px; }
.detail-img { width: 100%; border-radius: 8px; margin-bottom: 8px; }
.supporter-list { display: flex; flex-direction: column; gap: 10px; }
.supporter-item { display: flex; align-items: center; gap: 8px; }
.supporter-avatar { width: 32px; height: 32px; border-radius: 50%; }
.supporter-info { flex: 1; }
.supporter-name { font-size: 13px; }
.supporter-msg { font-size: 12px; color: #999; display: block; }
.supporter-amount { font-size: 14px; color: #ff4500; font-weight: 500; }
.count-badge { font-size: 12px; color: #999; font-weight: normal; }
.proof-item { padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.proof-images { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 8px; }
.proof-img { width: 100px; height: 100px; border-radius: 6px; object-fit: cover; }
.proof-actions { margin-top: 6px; font-size: 13px; color: #666; }
.comment-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.comment-content { font-size: 14px; }
.comment-time { font-size: 11px; color: #999; display: block; margin-top: 2px; }

/* 广告 */
.ad-inline { position: relative; margin: 10px 12px; border-radius: 8px; overflow: hidden; }
.ad-inline-img { width: 100%; height: 80px; object-fit: cover; display: block; }
.ad-tag { position: absolute; right: 8px; bottom: 8px; background: rgba(0,0,0,0.5); color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 4px; }

/* 底部栏 */
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; padding: 10px 16px; background: #fff; box-shadow: 0 -1px 4px rgba(0,0,0,0.06); z-index: 99; display: flex; align-items: center; gap: 12px; }
.bar-actions { display: flex; gap: 16px; }
.bar-action { display: flex; flex-direction: column; align-items: center; font-size: 11px; color: #666; cursor: pointer; }
.support-btn { flex: 1; }
</style>