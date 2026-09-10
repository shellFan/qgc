<template>
  <div class="pay-success-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="投喂成功" fixed placeholder />

    <div class="success-content">
      <div class="success-icon">🎉</div>
      <h2 class="success-title">投喂成功！</h2>
      <p class="success-desc">你已成功投喂 <strong class="amount-highlight">{{ formatMoney(amount) }}</strong>，穷鬼感谢你的慷慨！</p>

      <!-- 搞笑文案 -->
      <div v-if="funnyMessage" class="funny-box">
        <span>😂 {{ funnyMessage }}</span>
      </div>

      <!-- 积分/等级提示 -->
      <div v-if="pointsEarned > 0" class="points-box">
        <span>💰 获得 {{ pointsEarned }} 积分</span>
      </div>

      <!-- 广告位 -->
      <div v-if="ad" class="ad-box" @click="clickAd">
        <img :src="ad.imageUrl" class="ad-img" />
        <span class="ad-tag">广告</span>
      </div>

      <div class="action-buttons">
        <van-button round block type="primary" @click="goShare" class="action-btn">分享给朋友</van-button>
        <van-button round block plain type="primary" @click="goDetail" class="action-btn">查看筹款详情</van-button>
        <van-button round block plain @click="goHome" class="action-btn">返回首页</van-button>
      </div>
    </div>

    <!-- 分享弹窗 -->
    <van-share-sheet v-model:show="showShare" title="分享给朋友" :options="shareOptions" @select="onShareSelect" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRandomMessages, recordShare, getAdsByPosition } from '@/api'
import { formatMoney } from '@/utils/money'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()

const amount = ref(Number(route.query.amount) || 0)
const campaignId = ref(route.query.campaignId)
const funnyMessage = ref('')
const pointsEarned = ref(Number(route.query.points) || 0)
const ad = ref(null)
const showShare = ref(false)

const shareOptions = [
  { name: '微信好友', icon: 'wechat' },
  { name: '朋友圈', icon: 'wechat-moments' },
  { name: '复制链接', icon: 'link' }
]

async function fetchFunnyMessage() {
  try {
    const res = await getRandomMessages()
    const msgs = res.data || []
    if (msgs.length) funnyMessage.value = msgs[Math.floor(Math.random() * msgs.length)]
  } catch { /* ignore */ }
}

async function fetchAd() {
  try {
    const res = await getAdsByPosition('PAY_SUCCESS')
    if (res?.data?.length) ad.value = res.data[0]
  } catch { /* ignore */ }
}

function clickAd() {
  if (ad.value?.linkUrl) window.location.href = ad.value.linkUrl
}

function goShare() {
  showShare.value = true
}

function onShareSelect(option) {
  const channel = option.name
  if (channel === '复制链接') {
    navigator.clipboard?.writeText(window.location.origin + '/campaign/' + campaignId.value)
    showToast('链接已复制')
  }
  recordShare({
    campaignId: campaignId.value,
    channel: channel,
    amount: amount.value
  }).catch(() => {})
  showShare.value = false
}

function goDetail() {
  router.push('/campaign/' + campaignId.value)
}

function goHome() {
  router.push('/')
}

onMounted(() => {
  fetchFunnyMessage()
  fetchAd()
})
</script>

<style scoped>
.pay-success-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}
.success-content {
  padding: var(--qgc-spacing-xxl) var(--qgc-spacing-lg);
  text-align: center;
}
.success-icon {
  font-size: 64px;
  margin-bottom: var(--qgc-spacing-lg);
}
.success-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--qgc-primary);
}
.success-desc {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-secondary);
  margin-top: var(--qgc-spacing-sm);
  line-height: 1.6;
}
.amount-highlight {
  color: var(--qgc-primary);
  font-size: var(--qgc-font-lg);
}
.funny-box {
  background: var(--qgc-secondary-light);
  border: 1px solid var(--qgc-secondary-border);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-md);
  margin-top: var(--qgc-spacing-lg);
  font-size: var(--qgc-font-sm);
  color: var(--qgc-secondary-dark);
  text-align: left;
}
.points-box {
  background: var(--qgc-primary-light);
  border: 1px solid var(--qgc-primary-border, rgba(45, 184, 75, 0.2));
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-md);
  margin-top: var(--qgc-spacing-md);
  font-size: var(--qgc-font-md);
  color: var(--qgc-primary);
  font-weight: 600;
}
.ad-box {
  position: relative;
  margin-top: var(--qgc-spacing-lg);
  border-radius: var(--qgc-radius-sm);
  overflow: hidden;
}
.ad-img {
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
.action-buttons {
  margin-top: var(--qgc-spacing-xxl);
}
.action-btn {
  margin-top: var(--qgc-spacing-md);
}
</style>