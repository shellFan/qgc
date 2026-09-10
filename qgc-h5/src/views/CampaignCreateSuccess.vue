<template>
  <div class="create-success-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="发起成功" fixed placeholder />

    <div class="success-content">
      <div class="success-icon">🎉</div>
      <h2 class="success-title">筹款发起成功！</h2>
      <p class="success-desc">快去分享给你的穷鬼朋友们吧～</p>

      <!-- 筹款信息卡片 -->
      <div class="campaign-card" v-if="campaign">
        <div class="card-title">{{ campaign.title }}</div>
        <div class="card-info">
          <span>目标 {{ formatMoney(campaign.targetAmount) }}</span>
          <van-tag type="primary" round size="small">{{ campaign.categoryName || '自定义' }}</van-tag>
        </div>
      </div>

      <!-- 随机骚话 -->
      <div class="random-msg" v-if="randomMsg">
        <span class="msg-icon">💬</span>
        <span>{{ randomMsg }}</span>
      </div>

      <!-- 操作按钮 -->
      <div class="action-group">
        <van-button type="primary" block round icon="share-o" @click="showShare = true">
          分享给朋友
        </van-button>
        <van-button plain type="primary" block round icon="orders-o" @click="goManage" style="margin-top: 12px">
          管理我的筹款
        </van-button>
        <van-button plain block round @click="goHome" style="margin-top: 12px">
          返回首页
        </van-button>
      </div>

      <!-- 温馨提示 -->
      <div class="tips-card">
        <div class="tips-title">💡 温馨提示</div>
        <ul class="tips-list">
          <li>分享到朋友圈/微信群可以获得更多投喂</li>
          <li>定期发布返图可以让投喂人看到成果</li>
          <li>筹款到期后未达标金额会退还给投喂人</li>
        </ul>
      </div>
    </div>

    <!-- 分享弹窗 -->
    <van-popup v-model:show="showShare" position="bottom" round :style="{ padding: '20px' }">
      <div class="share-popup">
        <h3>分享到</h3>
        <div class="share-options">
          <div class="share-item" @click="copyLink">
            <span class="share-icon">🔗</span>
            <span>复制链接</span>
          </div>
          <div class="share-item" @click="shareWechat">
            <span class="share-icon">💬</span>
            <span>微信好友</span>
          </div>
          <div class="share-item" @click="shareMoments">
            <span class="share-icon">📸</span>
            <span>朋友圈</span>
          </div>
        </div>
        <van-button block round @click="showShare = false" style="margin-top: 16px">取消</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { getCampaignDetail, getRandomMessages, recordShare } from '@/api'
import { formatMoney } from '@/utils/money'

const route = useRoute()
const router = useRouter()

const campaign = ref(null)
const randomMsg = ref('')
const showShare = ref(false)

async function fetchCampaign() {
  const id = route.query.id || route.params.id
  if (!id) return
  try {
    const res = await getCampaignDetail(id)
    campaign.value = res?.data || null
  } catch { /* ignore */ }
}

async function fetchRandomMsg() {
  try {
    const res = await getRandomMessages()
    const msgs = res?.data || []
    if (msgs.length > 0) {
      randomMsg.value = msgs[Math.floor(Math.random() * msgs.length)].content || msgs[Math.floor(Math.random() * msgs.length)]
    }
  } catch { /* ignore */ }
}

function goManage() {
  if (campaign.value?.id) {
    router.push(`/campaign/manage/${campaign.value.id}`)
  } else {
    router.push('/campaign/my')
  }
}

function goHome() {
  router.push('/')
}

async function copyLink() {
  const url = window.location.origin + `/campaign/${campaign.value?.id || ''}`
  try {
    await navigator.clipboard.writeText(url)
    showSuccessToast('链接已复制')
    if (campaign.value?.id) {
      recordShare({ campaignId: campaign.value.id, channel: 'LINK' }).catch(() => {})
    }
  } catch {
    showToast('复制失败，请手动复制')
  }
  showShare.value = false
}

function shareWechat() {
  showToast('请在微信中打开本页面后使用微信分享')
  if (campaign.value?.id) {
    recordShare({ campaignId: campaign.value.id, channel: 'WECHAT' }).catch(() => {})
  }
  showShare.value = false
}

function shareMoments() {
  showToast('请在微信中打开本页面后分享到朋友圈')
  if (campaign.value?.id) {
    recordShare({ campaignId: campaign.value.id, channel: 'MOMENTS' }).catch(() => {})
  }
  showShare.value = false
}

onMounted(() => {
  fetchCampaign()
  fetchRandomMsg()
})
</script>

<style scoped>
.create-success-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}
.success-content {
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-lg);
  text-align: center;
}
.success-icon {
  font-size: 64px;
  margin-bottom: var(--qgc-spacing-md);
}
.success-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--qgc-primary);
  margin: 0 0 var(--qgc-spacing-sm);
}
.success-desc {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-tertiary);
  margin: 0 0 var(--qgc-spacing-xl);
}

/* 筹款卡片 */
.campaign-card {
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  text-align: left;
  margin-bottom: var(--qgc-spacing-md);
  box-shadow: var(--qgc-shadow-sm);
}
.card-title {
  font-size: var(--qgc-font-lg);
  font-weight: 600;
  margin-bottom: var(--qgc-spacing-sm);
  color: var(--qgc-text-primary);
}
.card-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-tertiary);
}

/* 随机骚话 */
.random-msg {
  background: var(--qgc-secondary-light);
  border: 1px solid var(--qgc-secondary-border);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-md) var(--qgc-spacing-lg);
  text-align: left;
  font-size: var(--qgc-font-sm);
  color: var(--qgc-secondary-dark);
  margin-bottom: var(--qgc-spacing-xl);
  display: flex;
  align-items: flex-start;
  gap: var(--qgc-spacing-sm);
}
.msg-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.action-group {
  margin-bottom: var(--qgc-spacing-xl);
}

/* 温馨提示 */
.tips-card {
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  text-align: left;
  box-shadow: var(--qgc-shadow-sm);
}
.tips-title {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  margin-bottom: var(--qgc-spacing-sm);
  color: var(--qgc-text-primary);
}
.tips-list {
  margin: 0;
  padding-left: 18px;
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-secondary);
  line-height: 1.8;
}

/* 分享弹窗 */
.share-popup h3 {
  text-align: center;
  margin: 0 0 var(--qgc-spacing-lg);
  font-size: var(--qgc-font-lg);
}
.share-options {
  display: flex;
  justify-content: space-around;
}
.share-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.share-icon {
  font-size: 36px;
}
.share-item span:last-child {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-secondary);
}
</style>