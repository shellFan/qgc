<template>
  <div class="wallet-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="我的钱包" fixed placeholder />

    <!-- 钱包头部 -->
    <div class="wallet-header">
      <div class="wallet-balance">
        <div class="balance-label">可提现余额</div>
        <div class="balance-amount">{{ formatMoney(wallet?.balance) }}</div>
      </div>
      <van-button type="primary" size="small" round to="/wallet/withdraw">提现</van-button>
    </div>

    <!-- 资金概览 -->
    <div class="overview-card">
      <div class="overview-item">
        <span class="overview-value">{{ formatMoney(wallet?.totalIncome) }}</span>
        <span class="overview-label">累计收入</span>
      </div>
      <div class="overview-divider"></div>
      <div class="overview-item">
        <span class="overview-value">{{ formatMoney(wallet?.totalWithdraw) }}</span>
        <span class="overview-label">累计提现</span>
      </div>
      <div class="overview-divider"></div>
      <div class="overview-item">
        <span class="overview-value">{{ formatMoney(wallet?.frozenAmount) }}</span>
        <span class="overview-label">冻结金额</span>
      </div>
    </div>

    <!-- 投喂统计 -->
    <div class="stat-card">
      <div class="stat-icon">💰</div>
      <div class="stat-info">
        <div class="stat-label">累计投喂</div>
        <div class="stat-desc">你为别人投喂的总金额</div>
      </div>
      <div class="stat-value">{{ formatMoney(wallet?.totalSupport) }}</div>
    </div>

    <!-- 功能入口 -->
    <div class="menu-section">
      <div class="menu-card">
        <div class="menu-item" @click="$router.push('/wallet/flow')">
          <van-icon name="records" size="20" color="var(--qgc-primary)" />
          <span class="menu-text">流水明细</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
        <div class="menu-item" @click="$router.push('/wallet/withdraw')">
          <van-icon name="cash-back-record" size="20" color="var(--qgc-secondary)" />
          <span class="menu-text">提现记录</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
      </div>
    </div>
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
.wallet-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}

/* 钱包头部 */
.wallet-header {
  background: linear-gradient(135deg, var(--qgc-primary), var(--qgc-primary-dark));
  color: #fff;
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-lg);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
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

/* 资金概览 */
.overview-card {
  display: flex;
  align-items: center;
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}
.overview-item {
  flex: 1;
  text-align: center;
}
.overview-value {
  display: block;
  font-size: var(--qgc-font-lg);
  font-weight: 700;
  color: var(--qgc-text-primary);
}
.overview-label {
  display: block;
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: 2px;
}
.overview-divider {
  width: 1px;
  height: 30px;
  background: var(--qgc-border-light);
}

/* 投喂统计 */
.stat-card {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-md);
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}
.stat-icon {
  font-size: 28px;
  flex-shrink: 0;
}
.stat-info {
  flex: 1;
}
.stat-label {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
}
.stat-desc {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: 2px;
}
.stat-value {
  font-size: var(--qgc-font-xl);
  font-weight: 700;
  color: var(--qgc-secondary);
}

/* 功能菜单 */
.menu-section {
  padding: 0 var(--qgc-spacing-md);
}
.menu-card {
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  margin-top: var(--qgc-spacing-sm);
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
</style>