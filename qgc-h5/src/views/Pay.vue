<template>
  <div class="pay-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="投喂穷鬼" fixed placeholder />

    <div class="pay-content" v-if="campaign">
      <div class="pay-header">
        <h3>{{ campaign.title }}</h3>
        <p>目标 {{ formatMoney(campaign.targetAmount) }} | 已筹 {{ formatMoney(campaign.raisedAmount) }}</p>
      </div>

      <!-- 快捷金额 -->
      <div class="amount-grid">
        <div v-for="a in quickAmounts" :key="a" class="amount-item" :class="{ active: amountYuan === a }" @click="amountYuan = a">
          {{ a }}元
        </div>
      </div>

      <!-- 自定义金额 -->
      <van-cell-group inset style="margin-top: 16px">
        <van-field v-model="amountYuan" label="自定义金额" type="number" placeholder="输入投喂金额(元)">
          <template #button>元</template>
        </van-field>
      </van-cell-group>

      <!-- 留言 -->
      <van-cell-group inset style="margin-top: 12px">
        <van-field v-model="message" label="留言" placeholder="给穷鬼说句话..." maxlength="50" show-word-limit />
      </van-cell-group>

      <!-- 选项 -->
      <van-cell-group inset style="margin-top: 12px">
        <van-cell title="匿名投喂" center>
          <template #right-icon><van-switch v-model="anonymous" size="20px" /></template>
        </van-cell>
        <van-cell title="隐藏金额" center>
          <template #right-icon><van-switch v-model="hideAmount" size="20px" /></template>
        </van-cell>
      </van-cell-group>

      <!-- 提交 -->
      <div style="padding: 24px 16px">
        <van-button type="danger" block round :loading="paying" @click="handlePay">
          💰 确认投喂 {{ amountYuan ? formatMoney(yuanToFen(amountYuan)) : '' }}
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { getCampaignDetail, createPayment } from '@/api'
import { formatMoney, yuanToFen } from '@/utils/money'

const route = useRoute()
const router = useRouter()

const campaign = ref(null)
const amountYuan = ref('')
const message = ref('')
const anonymous = ref(false)
const hideAmount = ref(false)
const paying = ref(false)

const quickAmounts = [1, 5, 10, 20, 50, 100]

async function fetchCampaign() {
  const id = route.query.campaignId
  if (!id) { router.back(); return }
  const res = await getCampaignDetail(id)
  campaign.value = res.data
}

async function handlePay() {
  const amount = parseFloat(amountYuan.value)
  if (!amount || amount <= 0) { showToast('请输入金额'); return }
  if (amount < 0.01) { showToast('最低0.01元'); return }

  paying.value = true
  try {
    const data = {
      campaignId: campaign.value.id,
      amount: yuanToFen(amountYuan.value),
      message: message.value,
      anonymous: anonymous.value ? 1 : 0,
      hideAmount: hideAmount.value ? 1 : 0
    }
    const res = await createPayment(data)
    // Mock模式直接返回成功
    showDialog({ title: '投喂成功', message: `成功投喂 ${formatMoney(yuanToFen(amountYuan.value))}，感谢义父！` }).then(() => {
      router.replace(`/campaign/${campaign.value.id}`)
    })
  } catch { /* error handled by interceptor */ } finally {
    paying.value = false
  }
}

onMounted(() => {
  fetchCampaign()
})
</script>

<style scoped>
.pay-page { min-height: 100vh; background: #f5f5f5; }
.pay-header { background: #fff; padding: 16px; margin: 10px 12px; border-radius: 12px; }
.pay-header h3 { font-size: 16px; margin-bottom: 4px; }
.pay-header p { font-size: 13px; color: #999; }
.amount-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; padding: 0 12px; margin-top: 12px; }
.amount-item { background: #fff; border-radius: 10px; padding: 12px; text-align: center; font-size: 15px; font-weight: 500; border: 2px solid transparent; cursor: pointer; }
.amount-item.active { border-color: #ff4500; color: #ff4500; background: #fff5f0; }
</style>