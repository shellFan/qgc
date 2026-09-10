<template>
  <div class="pay-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="投喂穷鬼" fixed placeholder />

    <div class="pay-content" v-if="campaign">
      <!-- 筹款信息卡片 -->
      <div class="pay-card campaign-card">
        <h3 class="campaign-title">{{ campaign.title }}</h3>
        <div class="campaign-stats">
          <span class="stat-item">目标 {{ formatMoney(campaign.targetAmount) }}</span>
          <span class="stat-divider">|</span>
          <span class="stat-item">已筹 {{ formatMoney(campaign.raisedAmount) }}</span>
        </div>
        <van-progress :percentage="progressPercent" :show-pivot="false" color="var(--qgc-primary)" track-color="var(--qgc-primary-light)" stroke-width="6" class="campaign-progress" />
      </div>

      <!-- 快捷金额 -->
      <div class="pay-card">
        <div class="section-label">选择金额</div>
        <div class="amount-grid">
          <div v-for="a in quickAmounts" :key="a" class="amount-chip" :class="{ active: amountYuan === a }" @click="amountYuan = a">
            <span class="amount-value">{{ a }}</span>
            <span class="amount-unit">元</span>
          </div>
        </div>
      </div>

      <!-- 自定义金额 -->
      <div class="pay-card">
        <div class="section-label">自定义金额</div>
        <div class="custom-amount-wrapper">
          <van-field v-model="amountYuan" type="number" placeholder="输入投喂金额" class="custom-amount-field" />
          <span class="custom-amount-unit">元</span>
        </div>
      </div>

      <!-- 留言 -->
      <div class="pay-card">
        <div class="section-label">给穷鬼说句话</div>
        <van-field v-model="message" type="textarea" placeholder="写句鼓励的话..." rows="2" maxlength="50" show-word-limit class="message-field" />
      </div>

      <!-- 选项 -->
      <div class="pay-card">
        <div class="option-row">
          <div class="option-info">
            <span class="option-label">匿名投喂</span>
            <span class="option-desc">不显示你的昵称</span>
          </div>
          <van-switch v-model="anonymous" size="20px" active-color="var(--qgc-primary)" />
        </div>
        <div class="option-row">
          <div class="option-info">
            <span class="option-label">隐藏金额</span>
            <span class="option-desc">其他人看不到金额</span>
          </div>
          <van-switch v-model="hideAmount" size="20px" active-color="var(--qgc-primary)" />
        </div>
      </div>

      <!-- 提交 -->
      <div class="submit-area">
        <van-button type="primary" block round :loading="paying" @click="handlePay" class="pay-btn">
          💰 确认投喂 {{ amountYuan ? formatMoney(yuanToFen(amountYuan)) : '' }}
        </van-button>
        <p class="submit-tip">投喂即表示你信任筹款人，请谨慎决策</p>
      </div>
    </div>

    <!-- 加载骨架 -->
    <div v-else class="loading-skeleton">
      <van-skeleton title :row="5" />
    </div>

    <!-- Native支付二维码弹窗 -->
    <van-dialog v-model:show="showQrCode" title="扫码支付" :show-confirm-button="false" close-on-click-overlay>
      <div class="qrcode-dialog">
        <div class="qrcode-container" ref="qrcodeContainer"></div>
        <p class="qrcode-amount">支付金额: <strong>{{ formatMoney(currentAmount) }}</strong></p>
        <p class="qrcode-tip">请使用微信扫描二维码完成支付</p>
        <p class="qrcode-countdown" v-if="countdown > 0">二维码有效期: {{ countdown }}秒</p>
        <p class="qrcode-expired" v-if="countdown <= 0 && qrCodeExpired">二维码已过期</p>
        <div class="qrcode-actions">
          <van-button size="small" type="primary" @click="refreshQrCode" :disabled="countdown > 0" v-if="qrCodeExpired">刷新二维码</van-button>
          <van-button size="small" @click="showQrCode = false; stopPolling()">取消支付</van-button>
        </div>
      </div>
    </van-dialog>

    <!-- 支付结果弹窗 -->
    <van-dialog v-model:show="showResult" :title="resultTitle" :message="resultMsg" @confirm="onResultConfirm" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { getCampaignDetail, createPayment, getPaymentStatus } from '@/api'
import { formatMoney, yuanToFen } from '@/utils/money'
import QRCode from 'qrcodejs2-fix'

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

const showQrCode = ref(false)
const qrcodeContainer = ref(null)
const countdown = ref(0)
const qrCodeExpired = ref(false)
const currentCodeUrl = ref('')
const currentAmount = ref(0)
const currentPaymentOrderNo = ref('')
let countdownTimer = null
let qrCodeInstance = null

const quickAmounts = [1, 5, 10, 20, 50, 100]

const progressPercent = computed(() => {
  if (!campaign.value?.targetAmount) return 0
  return Math.min(100, Math.round((campaign.value.raisedAmount / campaign.value.targetAmount) * 100))
})

let pollTimer = null
let currentOrderNo = null

function isWechatBrowser() {
  const ua = navigator.userAgent.toLowerCase()
  return ua.indexOf('micromessenger') !== -1
}

function callWxPay(payParams) {
  return new Promise((resolve, reject) => {
    if (typeof WeixinJSBridge === 'undefined') {
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
      if (res.err_msg === 'get_brand_wcpay_request:ok') {
        resolve('ok')
      } else if (res.err_msg === 'get_brand_wcpay_request:cancel') {
        resolve('cancel')
      } else {
        reject(new Error(res.err_msg || '支付调用失败'))
      }
    }
  )
}

function generateQrCode(codeUrl) {
  if (qrCodeInstance) {
    qrCodeInstance.clear()
    qrCodeInstance.makeCode(codeUrl)
  } else if (qrcodeContainer.value) {
    qrCodeInstance = new QRCode(qrcodeContainer.value, {
      text: codeUrl,
      width: 200,
      height: 200,
      colorDark: '#000000',
      colorLight: '#ffffff',
      correctLevel: QRCode.CorrectLevel.M
    })
  }
}

function startCountdown(seconds) {
  countdown.value = seconds
  qrCodeExpired.value = false
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
      qrCodeExpired.value = true
    }
  }, 1000)
}

async function refreshQrCode() {
  if (!campaign.value) return
  try {
    const data = {
      campaignId: campaign.value.id,
      amount: currentAmount.value,
      message: message.value,
      anonymous: anonymous.value ? 1 : 0,
      hideAmount: hideAmount.value ? 1 : 0,
      payType: 'NATIVE',
      requestId: `pay_${campaign.value.id}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    }
    const res = await createPayment(data)
    const payResult = res.data
    if (payResult.codeUrl) {
      currentCodeUrl.value = payResult.codeUrl
      currentPaymentOrderNo.value = payResult.paymentOrderNo
      generateQrCode(payResult.codeUrl)
      startCountdown(300)
      startPolling(payResult.paymentOrderNo)
    }
  } catch (e) {
    showToast('刷新二维码失败')
  }
}

function startPolling(orderNo) {
  currentOrderNo = orderNo
  let pollCount = 0
  const maxPolls = 60

  const doPoll = async () => {
    try {
      const res = await getPaymentStatus(orderNo)
      const status = res.data?.status
      pollCount++

      if (status === 'SUCCESS') {
        stopPolling(); closeToast(); showQrCode.value = false
        if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
        resultTitle.value = '投喂成功'; resultMsg.value = `成功投喂 ${formatMoney(currentAmount.value)}，感谢义父！`; showResult.value = true; return
      }
      if (status === 'FAIL' || status === 'CLOSED') {
        stopPolling(); closeToast(); showQrCode.value = false
        if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
        resultTitle.value = '支付失败'; resultMsg.value = status === 'CLOSED' ? '订单已关闭' : '支付失败，请重试'; showResult.value = true; return
      }
      if (pollCount >= maxPolls) {
        stopPolling(); closeToast(); showQrCode.value = false
        if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
        resultTitle.value = '支付结果确认中'; resultMsg.value = '支付结果正在确认中，请稍后在订单中查看'; showResult.value = true
      }
    } catch (e) {
      if (pollCount >= maxPolls) {
        stopPolling(); closeToast(); showQrCode.value = false
        if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
        resultTitle.value = '网络异常'; resultMsg.value = '网络异常，支付结果正在确认中，请稍后查看订单'; showResult.value = true
      }
    }
  }

  doPoll()
  pollTimer = setInterval(doPoll, 1000)
}

function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
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

function handleNativePay(payResult, amount) {
  currentCodeUrl.value = payResult.codeUrl
  currentAmount.value = amount
  currentPaymentOrderNo.value = payResult.paymentOrderNo
  showQrCode.value = true
  nextTick(() => {
    generateQrCode(payResult.codeUrl)
    startCountdown(300)
  })
  startPolling(payResult.paymentOrderNo)
}

async function handleJsapiPay(payResult) {
  if (!isWechatBrowser()) { showToast('请使用微信打开后支付'); return }
  if (payResult.wxPayParams) {
    try {
      const wxResult = await callWxPay(payResult.wxPayParams)
      if (wxResult === 'cancel') { showToast('已取消支付'); return }
      showLoadingToast({ message: '确认支付结果...', forbidClick: true, duration: 0 })
      startPolling(payResult.paymentOrderNo)
    } catch (e) {
      showToast('支付调用失败: ' + (e.message || '未知错误'))
    }
  } else if (payResult.paymentOrderNo) {
    showLoadingToast({ message: '确认支付结果...', forbidClick: true, duration: 0 })
    startPolling(payResult.paymentOrderNo)
  } else {
    showToast('支付参数异常')
  }
}

async function handlePay() {
  const amount = parseFloat(amountYuan.value)
  if (!amount || amount <= 0) { showToast('请输入金额'); return }
  if (amount < 0.01) { showToast('最低0.01元'); return }

  paying.value = true
  try {
    const fenAmount = yuanToFen(amountYuan.value)
    const data = {
      campaignId: campaign.value.id,
      amount: fenAmount,
      message: message.value,
      anonymous: anonymous.value ? 1 : 0,
      hideAmount: hideAmount.value ? 1 : 0,
      requestId: `pay_${campaign.value.id}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    }
    const res = await createPayment(data)
    const payResult = res.data
    const payType = payResult.payType || 'MOCK'
    currentAmount.value = fenAmount

    if (payType === 'MOCK' || payResult.mock) {
      resultTitle.value = '投喂成功'
      resultMsg.value = `成功投喂 ${formatMoney(fenAmount)}，感谢义父！(Mock模式)`
      showResult.value = true
    } else if (payType === 'NATIVE') {
      handleNativePay(payResult, fenAmount)
    } else if (payType === 'JSAPI') {
      handleJsapiPay(payResult)
    } else {
      showToast('不支持的支付类型: ' + payType)
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    paying.value = false
  }
}

onMounted(() => { fetchCampaign() })
onUnmounted(() => {
  stopPolling()
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
})
</script>

<style scoped>
.pay-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}

/* 卡片 */
.pay-card {
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}
.section-label {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
  margin-bottom: var(--qgc-spacing-md);
}

/* 筹款信息 */
.campaign-title {
  font-size: var(--qgc-font-lg);
  font-weight: 600;
  color: var(--qgc-text-primary);
  line-height: 1.4;
}
.campaign-stats {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-sm);
  margin-top: var(--qgc-spacing-xs);
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-tertiary);
}
.stat-divider { color: var(--qgc-border); }
.campaign-progress { margin-top: var(--qgc-spacing-sm); }

/* 快捷金额 */
.amount-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--qgc-spacing-sm);
}
.amount-chip {
  background: var(--qgc-bg-grey);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-md) 0;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}
.amount-chip.active {
  border-color: var(--qgc-primary);
  background: var(--qgc-primary-light);
}
.amount-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--qgc-text-primary);
  display: block;
}
.amount-chip.active .amount-value {
  color: var(--qgc-primary);
}
.amount-unit {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
}

/* 自定义金额 */
.custom-amount-wrapper {
  display: flex;
  align-items: center;
  background: var(--qgc-bg-grey);
  border-radius: var(--qgc-radius-sm);
  overflow: hidden;
}
.custom-amount-field {
  flex: 1;
  background: transparent;
}
.custom-amount-field :deep(.van-field__control) {
  font-size: 24px;
  font-weight: 700;
  color: var(--qgc-primary);
}
.custom-amount-unit {
  padding-right: var(--qgc-spacing-md);
  font-size: var(--qgc-font-lg);
  color: var(--qgc-text-secondary);
  font-weight: 500;
}

/* 留言 */
.message-field {
  border-radius: var(--qgc-radius-sm);
  background: var(--qgc-bg-grey);
}

/* 选项行 */
.option-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--qgc-spacing-md) 0;
}
.option-row + .option-row {
  border-top: 1px solid var(--qgc-border-light);
  padding-top: var(--qgc-spacing-md);
}
.option-info { flex: 1; }
.option-label {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-primary);
  font-weight: 500;
}
.option-desc {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  display: block;
  margin-top: 2px;
}

/* 提交 */
.submit-area {
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-lg);
  padding-bottom: calc(var(--qgc-spacing-xl) + var(--qgc-safe-bottom));
}
.pay-btn {
  height: 48px;
  font-size: var(--qgc-font-lg);
  font-weight: 600;
}
.submit-tip {
  text-align: center;
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: var(--qgc-spacing-sm);
}

/* 二维码弹窗 */
.qrcode-dialog {
  padding: var(--qgc-spacing-xl);
  text-align: center;
}
.qrcode-container {
  display: flex;
  justify-content: center;
  margin-bottom: var(--qgc-spacing-md);
}
.qrcode-amount {
  font-size: var(--qgc-font-lg);
  margin-bottom: var(--qgc-spacing-sm);
}
.qrcode-amount strong { color: var(--qgc-primary); }
.qrcode-tip {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-secondary);
  margin-bottom: var(--qgc-spacing-sm);
}
.qrcode-countdown {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
}
.qrcode-expired {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-danger);
  margin-bottom: var(--qgc-spacing-sm);
}
.qrcode-actions {
  display: flex;
  gap: var(--qgc-spacing-sm);
  justify-content: center;
  margin-top: var(--qgc-spacing-md);
}

/* 加载骨架 */
.loading-skeleton {
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-md);
}
</style>