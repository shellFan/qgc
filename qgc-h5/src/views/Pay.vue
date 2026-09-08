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

    <!-- 支付结果弹窗 -->
    <van-dialog v-model:show="showResult" :title="resultTitle" :message="resultMsg" @confirm="onResultConfirm" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { getCampaignDetail, createPayment, getPaymentStatus } from '@/api'
import { formatMoney, yuanToFen } from '@/utils/money'

const route = useRoute()
const router = useRouter()

const campaign = ref(null)
const amountYuan = ref('')
const message = ref('')
const anonymous = ref(false)
const hideAmount = ref(false)
const paying = ref(false)
const showResult = ref(false)
const resultTitle = ref('')
const resultMsg = ref('')

const quickAmounts = [1, 5, 10, 20, 50, 100]

/** 轮询相关 */
let pollTimer = null
let currentOrderNo = null

/** 检测是否在微信浏览器内 */
function isWechatBrowser() {
  const ua = navigator.userAgent.toLowerCase()
  return ua.indexOf('micromessenger') !== -1
}

/** 调用微信JSAPI支付 */
function callWxPay(payParams) {
  return new Promise((resolve, reject) => {
    if (typeof WeixinJSBridge === 'undefined') {
      // 等待WeixinJSBridge就绪
      if (document.addEventListener) {
        document.addEventListener('WeixinJSBridgeReady', () => {
          onBridgeReady(payParams, resolve, reject)
        }, false)
      }
    } else {
      onBridgeReady(payParams, resolve, reject)
    }
  })
}

function onBridgeReady(payParams, resolve, reject) {
  WeixinJSBridge.invoke(
    'getBrandWCPayRequest',
    {
      appId: payParams.appId,
      timeStamp: payParams.timeStamp,
      nonceStr: payParams.nonceStr,
      package: payParams.packageValue,
      signType: payParams.signType || 'RSA',
      paySign: payParams.paySign
    },
    (res) => {
      // 注意：res.err_msg仅代表前端调用结果，不等于支付成功
      // 必须通过后端轮询确认真实支付状态
      if (res.err_msg === 'get_brand_wcpay_request:ok') {
        // 用户点击了完成，开始轮询
        resolve('ok')
      } else if (res.err_msg === 'get_brand_wcpay_request:cancel') {
        // 用户取消支付，不标FAIL，订单保持CREATED/PAYING
        resolve('cancel')
      } else {
        // 调用失败
        reject(new Error(res.err_msg || '支付调用失败'))
      }
    }
  )
}

/** 轮询支付状态 */
function startPolling(orderNo) {
  currentOrderNo = orderNo
  let pollCount = 0
  const maxPolls = 30 // 最多轮询30次，约30秒

  const doPoll = async () => {
    try {
      const res = await getPaymentStatus(orderNo)
      const status = res.data?.status
      pollCount++

      if (status === 'SUCCESS') {
        stopPolling()
        closeToast()
        resultTitle.value = '投喂成功'
        resultMsg.value = `成功投喂 ${formatMoney(yuanToFen(amountYuan.value))}，感谢义父！`
        showResult.value = true
        return
      }

      if (status === 'FAIL' || status === 'CLOSED') {
        stopPolling()
        closeToast()
        resultTitle.value = '支付失败'
        resultMsg.value = status === 'CLOSED' ? '订单已关闭' : '支付失败，请重试'
        showResult.value = true
        return
      }

      // CREATED/PAYING 继续轮询
      if (pollCount >= maxPolls) {
        stopPolling()
        closeToast()
        // 超时不显示"支付失败"，提示"支付结果确认中"
        resultTitle.value = '支付结果确认中'
        resultMsg.value = '支付结果正在确认中，请稍后在订单中查看'
        showResult.value = true
      }
    } catch (e) {
      console.error('轮询支付状态失败:', e)
      // 网络错误继续轮询
      if (pollCount >= maxPolls) {
        stopPolling()
        closeToast()
        resultTitle.value = '网络异常'
        resultMsg.value = '网络异常，支付结果正在确认中，请稍后查看订单'
        showResult.value = true
      }
    }
  }

  // 立即查一次
  doPoll()
  // 每1秒轮询
  pollTimer = setInterval(doPoll, 1000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  currentOrderNo = null
}

function onResultConfirm() {
  router.replace(`/campaign/${campaign.value.id}`)
}

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
      hideAmount: hideAmount.value ? 1 : 0,
      requestId: `pay_${campaign.value.id}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    }
    const res = await createPayment(data)
    const payResult = res.data

    if (payResult.mock) {
      // Mock模式：直接成功
      resultTitle.value = '投喂成功'
      resultMsg.value = `成功投喂 ${formatMoney(yuanToFen(amountYuan.value))}，感谢义父！(Mock模式)`
      showResult.value = true
      return
    }

    // 真实JSAPI支付
    if (!isWechatBrowser()) {
      // 非微信浏览器：提示用户使用微信打开，不报JS error
      showToast('请使用微信打开后支付')
      return
    }

    if (payResult.wxPayParams) {
      // 调用微信JSAPI支付
      try {
        const wxResult = await callWxPay(payResult.wxPayParams)
        if (wxResult === 'cancel') {
          // 用户取消，不标FAIL，提示用户
          showToast('已取消支付')
          return
        }
        // wxResult === 'ok'，开始轮询确认支付状态
        showLoadingToast({ message: '确认支付结果...', forbidClick: true, duration: 0 })
        startPolling(payResult.paymentOrderNo)
      } catch (e) {
        showToast('支付调用失败: ' + (e.message || '未知错误'))
      }
    } else if (payResult.paymentOrderNo) {
      // 有订单号但无支付参数，轮询等待(可能是已支付状态)
      showLoadingToast({ message: '确认支付结果...', forbidClick: true, duration: 0 })
      startPolling(payResult.paymentOrderNo)
    } else {
      showToast('支付参数异常')
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  fetchCampaign()
})

onUnmounted(() => {
  stopPolling()
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