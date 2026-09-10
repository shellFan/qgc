<template>
  <div class="withdraw-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="提现" fixed placeholder />

    <!-- 余额信息 -->
    <div class="balance-card">
      <div class="balance-label">可提现余额</div>
      <div class="balance-amount">{{ formatMoney(wallet?.balance) }}</div>
    </div>

    <!-- 提现表单 -->
    <div class="form-card">
      <div class="form-label">提现金额</div>
      <div class="amount-input-row">
        <span class="amount-unit">¥</span>
        <input v-model="amountYuan" type="number" class="amount-input" placeholder="输入提现金额" inputmode="decimal" />
        <span class="amount-suffix">元</span>
      </div>
      <div class="form-hint">最低提现1元</div>
    </div>

    <div class="submit-area">
      <van-button type="primary" block round :loading="submitting" @click="handleSubmit">
        申请提现
      </van-button>
    </div>

    <!-- 提现记录 -->
    <div class="records-section" v-if="records.length > 0">
      <div class="section-title">提现记录</div>
      <div class="record-list">
        <div v-for="item in records" :key="item.id" class="record-card">
          <div class="record-left">
            <div class="record-amount">{{ formatMoney(item.amount) }}</div>
            <div class="record-time">{{ item.createTime }}</div>
          </div>
          <van-tag :type="statusType(item.status)" round>{{ statusText(item.status) }}</van-tag>
        </div>
      </div>
    </div>

    <!-- 内联空状态 -->
    <div v-if="records.length === 0 && !submitting" class="empty-state">
      <div class="empty-emoji">💸</div>
      <div class="empty-text">暂无提现记录</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getWallet, applyWithdraw, getWithdrawList } from '@/api'
import { formatMoney, yuanToFen } from '@/utils/money'

const wallet = ref(null)
const amountYuan = ref('')
const submitting = ref(false)
const records = ref([])

function statusText(s) {
  const map = { PENDING: '待审核', PROCESSING: '处理中', SUCCESS: '已到账', FAIL: '失败', REJECTED: '已拒绝' }
  return map[s] || s
}
function statusType(s) {
  const map = { SUCCESS: 'success', FAIL: 'danger', REJECTED: 'danger', PROCESSING: 'primary' }
  return map[s] || 'warning'
}

async function fetchWallet() {
  try { const res = await getWallet(); wallet.value = res.data } catch { /* ignore */ }
}

async function fetchRecords() {
  try {
    const res = await getWithdrawList({ page: 1, size: 20 })
    records.value = res.data?.records || res.data || []
  } catch { /* ignore */ }
}

async function handleSubmit() {
  const amount = parseFloat(amountYuan.value)
  if (!amount || amount < 1) { showToast('最低提现1元'); return }
  if (wallet.value && yuanToFen(amount) > wallet.value.balance) { showToast('余额不足'); return }

  submitting.value = true
  try {
    await applyWithdraw({ amount: yuanToFen(amountYuan.value) })
    showToast('提现申请已提交')
    amountYuan.value = ''
    fetchWallet()
    fetchRecords()
  } catch { /* error handled */ } finally {
    submitting.value = false
  }
}

onMounted(() => { fetchWallet(); fetchRecords() })
</script>

<style scoped>
.withdraw-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}

/* 余额卡片 */
.balance-card {
  background: linear-gradient(135deg, var(--qgc-primary), var(--qgc-primary-dark));
  color: #fff;
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-lg);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-lg);
  box-shadow: var(--qgc-shadow-md);
}
.balance-label {
  font-size: var(--qgc-font-sm);
  opacity: 0.85;
}
.balance-amount {
  font-size: 28px;
  font-weight: 700;
  margin-top: 4px;
}

/* 表单卡片 */
.form-card {
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}
.form-label {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
  margin-bottom: var(--qgc-spacing-md);
}
.amount-input-row {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-sm);
}
.amount-unit {
  font-size: 24px;
  font-weight: 700;
  color: var(--qgc-primary);
}
.amount-input {
  flex: 1;
  font-size: 24px;
  font-weight: 700;
  color: var(--qgc-primary);
  border: none;
  outline: none;
  background: transparent;
  min-width: 0;
}
.amount-input::placeholder {
  color: var(--qgc-text-quaternary);
  font-weight: 400;
  font-size: var(--qgc-font-lg);
}
.amount-suffix {
  font-size: var(--qgc-font-lg);
  color: var(--qgc-text-tertiary);
}
.form-hint {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: var(--qgc-spacing-sm);
}

.submit-area {
  padding: var(--qgc-spacing-lg) var(--qgc-spacing-md);
}

/* 提现记录 */
.records-section {
  padding: 0 var(--qgc-spacing-md);
}
.section-title {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
  margin-bottom: var(--qgc-spacing-sm);
}
.record-list {
  display: flex;
  flex-direction: column;
  gap: var(--qgc-spacing-sm);
}
.record-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}
.record-left {
  flex: 1;
}
.record-amount {
  font-size: var(--qgc-font-lg);
  font-weight: 700;
  color: var(--qgc-text-primary);
}
.record-time {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: 4px;
}

/* 内联空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}
.empty-emoji {
  font-size: 48px;
  margin-bottom: var(--qgc-spacing-md);
}
.empty-text {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-tertiary);
}
</style>