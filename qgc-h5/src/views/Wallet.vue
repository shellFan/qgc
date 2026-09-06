<template>
  <div class="wallet-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="我的钱包" fixed placeholder />

    <div class="wallet-header">
      <div class="wallet-balance">
        <div class="balance-label">可提现余额</div>
        <div class="balance-amount">{{ formatMoney(wallet?.balance) }}</div>
      </div>
      <van-button type="danger" size="small" round to="/wallet/withdraw">提现</van-button>
    </div>

    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="累计收入" :value="formatMoney(wallet?.totalIncome)" />
      <van-cell title="累计提现" :value="formatMoney(wallet?.totalWithdraw)" />
      <van-cell title="冻结金额" :value="formatMoney(wallet?.frozenAmount)" />
      <van-cell title="累计投喂" :value="formatMoney(wallet?.totalSupport)" />
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="流水明细" is-link to="/wallet/flow" icon="records" />
      <van-cell title="提现记录" is-link to="/wallet/withdraw" icon="cash-back-record" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWallet } from '@/api'
import { formatMoney } from '@/utils/money'

const wallet = ref(null)

async function fetchWallet() {
  try {
    const res = await getWallet()
    wallet.value = res.data
  } catch { /* ignore */ }
}

onMounted(() => { fetchWallet() })
</script>

<style scoped>
.wallet-page { min-height: 100vh; background: #f5f5f5; }
.wallet-header { background: linear-gradient(135deg, #ff4500, #ff6b35); color: #fff; padding: 20px 16px; display: flex; justify-content: space-between; align-items: center; }
.balance-label { font-size: 13px; opacity: 0.8; }
.balance-amount { font-size: 28px; font-weight: bold; margin-top: 4px; }
</style>