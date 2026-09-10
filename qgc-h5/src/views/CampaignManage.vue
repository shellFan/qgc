<template>
  <div class="campaign-manage-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="管理筹款" fixed placeholder />

    <div v-if="campaign" class="manage-content">
      <!-- 筹款信息 -->
      <div class="info-card">
        <div class="info-header">
          <h3>{{ campaign.title }}</h3>
          <van-tag :type="statusType(campaign.status)" size="medium" round>{{ statusText(campaign.status) }}</van-tag>
        </div>
        <div class="info-row">
          <span>目标金额</span>
          <span class="info-value">{{ formatMoney(campaign.targetAmount) }}</span>
        </div>
        <div class="info-row">
          <span>已筹金额</span>
          <span class="info-value highlight">{{ formatMoney(campaign.raisedAmount) }}</span>
        </div>
        <div class="info-row">
          <span>投喂人数</span>
          <span class="info-value">{{ campaign.supportCount || 0 }}人</span>
        </div>
        <div class="info-row">
          <span>创建时间</span>
          <span class="info-value">{{ formatDate(campaign.createTime) }}</span>
        </div>
        <div class="info-row" v-if="campaign.endTime">
          <span>截止时间</span>
          <span class="info-value">{{ formatDate(campaign.endTime) }}</span>
        </div>
        <van-progress v-if="campaign.targetAmount" :percentage="progressPercent" :show-pivot="false" color="var(--qgc-primary)" track-color="var(--qgc-primary-light)" stroke-width="10" style="margin-top: 10px" />
      </div>

      <!-- 操作区 -->
      <div class="menu-card">
        <div class="menu-item" @click="goProof">
          <van-icon name="photo-o" size="20" color="var(--qgc-primary)" />
          <span class="menu-text">发布返图</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
        <div class="menu-item" @click="goProofList">
          <van-icon name="records" size="20" color="var(--qgc-secondary)" />
          <span class="menu-text">查看返图</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
        <div class="menu-item" @click="showShare = true">
          <van-icon name="share-o" size="20" color="var(--qgc-primary)" />
          <span class="menu-text">分享筹款</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
        <div class="menu-item" @click="goDetail">
          <van-icon name="description" size="20" color="var(--qgc-text-secondary)" />
          <span class="menu-text">查看详情</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
      </div>

      <!-- 返图列表 -->
      <div class="section" v-if="proofs.length > 0">
        <div class="section-header">
          <span class="section-title">返图记录 ({{ proofs.length }})</span>
        </div>
        <div class="proof-list">
          <div v-for="p in proofs" :key="p.id" class="proof-item">
            <div class="proof-title">{{ p.title }}</div>
            <div class="proof-meta">
              <span>{{ formatDate(p.createTime) }}</span>
              <span>❤️ {{ p.likeCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 危险操作 -->
      <div class="danger-card" v-if="campaign.status === 'ACTIVE'">
        <div class="menu-item danger-item" @click="confirmClose">
          <van-icon name="close" size="20" color="var(--qgc-danger)" />
          <span class="menu-text" style="color: var(--qgc-danger)">关闭筹款</span>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-else class="loading-box">
      <van-loading size="24px">加载中...</van-loading>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showSuccessToast, showDialog } from 'vant'
import { getCampaignDetail, getProofList, closeCampaign, recordShare } from '@/api'
import { formatMoney } from '@/utils/money'

const route = useRoute()
const router = useRouter()

const campaign = ref(null)
const proofs = ref([])
const showShare = ref(false)

const statusMap = {
  PENDING_REVIEW: '待审核', ACTIVE: '进行中', REJECTED: '已拒绝',
  SUCCESS: '已成功', CLOSED: '已关闭', EXPIRED: '已过期', FROZEN: '已冻结'
}
const statusTypeMap = {
  PENDING_REVIEW: 'warning', ACTIVE: 'primary', REJECTED: 'danger',
  SUCCESS: 'success', CLOSED: 'default', EXPIRED: 'default', FROZEN: 'danger'
}

function statusText(s) { return statusMap[s] || s }
function statusType(s) { return statusTypeMap[s] || 'default' }

const progressPercent = computed(() => {
  if (!campaign.value || !campaign.value.targetAmount) return 0
  return Math.min(100, Math.round((campaign.value.raisedAmount / campaign.value.targetAmount) * 100))
})

function formatDate(t) {
  if (!t) return ''
  return new Date(t).toLocaleString()
}

async function fetchCampaign() {
  const id = route.params.id
  if (!id) return
  try {
    const res = await getCampaignDetail(id)
    campaign.value = res?.data || null
  } catch { showToast('获取筹款信息失败') }
}

async function fetchProofs() {
  const id = route.params.id
  if (!id) return
  try {
    const res = await getProofList(id)
    proofs.value = res?.data || []
  } catch { /* ignore */ }
}

function goProof() { router.push(`/proof/create/${route.params.id}`) }
function goProofList() { router.push(`/campaign/${route.params.id}`) }
function goDetail() { router.push(`/campaign/${route.params.id}`) }

async function confirmClose() {
  try {
    await showDialog({
      title: '关闭筹款',
      message: '确定要关闭此筹款吗？关闭后将无法继续接受投喂。'
    })
    await closeCampaign(route.params.id)
    showSuccessToast('已关闭')
    fetchCampaign()
  } catch { /* cancel or error */ }
}

async function copyLink() {
  const url = window.location.origin + `/h5/campaign/${campaign.value?.id || ''}`
  try {
    await navigator.clipboard.writeText(url)
    showSuccessToast('链接已复制')
    if (campaign.value?.id) {
      recordShare({ campaignId: campaign.value.id, channel: 'LINK' }).catch(() => {})
    }
  } catch { showToast('复制失败') }
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
  fetchProofs()
})
</script>

<style scoped>
.campaign-manage-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}
.manage-content {
  padding: 0 0 var(--qgc-spacing-xl);
}

/* 信息卡片 */
.info-card {
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}
.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--qgc-spacing-md);
}
.info-header h3 {
  font-size: var(--qgc-font-lg);
  font-weight: 600;
  margin: 0;
  flex: 1;
  margin-right: var(--qgc-spacing-sm);
  line-height: 1.4;
  color: var(--qgc-text-primary);
}
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-secondary);
}
.info-value {
  color: var(--qgc-text-primary);
  font-weight: 500;
}
.info-value.highlight {
  color: var(--qgc-primary);
  font-weight: 700;
}

/* 菜单卡片 */
.menu-card {
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  box-shadow: var(--qgc-shadow-sm);
  overflow: hidden;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-md);
  padding: var(--qgc-spacing-md) var(--qgc-spacing-lg);
  cursor: pointer;
  transition: background 0.15s;
}
.menu-item:active {
  background: var(--qgc-bg-grey);
}
.menu-item + .menu-item {
  border-top: 1px solid var(--qgc-border-light);
}
.menu-text {
  flex: 1;
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-primary);
}

/* 返图区域 */
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
.proof-list {
  display: flex;
  flex-direction: column;
  gap: var(--qgc-spacing-sm);
}
.proof-item {
  padding: var(--qgc-spacing-sm) 0;
  border-bottom: 1px solid var(--qgc-border-light);
}
.proof-item:last-child {
  border-bottom: none;
}
.proof-title {
  font-size: var(--qgc-font-md);
  font-weight: 500;
  color: var(--qgc-text-primary);
}
.proof-meta {
  display: flex;
  justify-content: space-between;
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: 4px;
}

/* 危险操作 */
.danger-card {
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  box-shadow: var(--qgc-shadow-sm);
  overflow: hidden;
}
.danger-item {
  justify-content: center;
}

.loading-box {
  display: flex;
  justify-content: center;
  padding: 40px;
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
