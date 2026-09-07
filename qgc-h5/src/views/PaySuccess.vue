<template>
  <div class="pay-success-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="投喂成功" fixed placeholder />

    <div class="success-content">
      <div class="success-icon">🎉</div>
      <h2 class="success-title">投喂成功！</h2>
      <p class="success-desc">你已成功投喂 {{ formatMoney(amount) }}，穷鬼感谢你的慷慨！</p>

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
        <van-button round block type="danger" @click="goShare">分享给朋友</van-button>
        <van-button round block plain @click="goDetail" style="margin-top: 12px">查看筹款详情</van-button>
        <van-button round block plain @click="goHome" style="margin-top: 12px">返回首页</van-button>
      </div>
    </div>

    <!-- 分享弹窗 -->
    <van-share-sheet v-model:show="showShare" title="分享给朋友" :options="shareOptions" @select="onShareSelect" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRandomMessages } from '@/api'
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
    const res = await fetch('/api/ad/position/PAY_SUCCESS').then(r => r.json()).catch(() => null)
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
  if (option.name === '复制链接') {
    navigator.clipboard?.writeText(window.location.origin + '/campaign/' + campaignId.value)
    showToast('链接已复制')
  }
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
.pay-success-page { min-height: 100vh; background: #f5f5f5; }
.success-content { padding: 40px 20px; text-align: center; }
.success-icon { font-size: 64px; margin-bottom: 16px; }
.success-title { font-size: 22px; font-weight: bold; color: #333; }
.success-desc { font-size: 14px; color: #666; margin-top: 8px; }
.funny-box { background: #fff9e6; border: 1px solid #ffe0b2; border-radius: 8px; padding: 12px; margin-top: 16px; font-size: 13px; color: #e65100; }
.points-box { background: #e8f5e9; border: 1px solid #c8e6c9; border-radius: 8px; padding: 10px; margin-top: 12px; font-size: 14px; color: #2e7d32; font-weight: 500; }
.ad-box { position: relative; margin-top: 16px; border-radius: 8px; overflow: hidden; }
.ad-img { width: 100%; height: 80px; object-fit: cover; display: block; }
.ad-tag { position: absolute; right: 8px; bottom: 8px; background: rgba(0,0,0,0.5); color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 4px; }
.action-buttons { margin-top: 24px; }
</style>