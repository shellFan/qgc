<template>
  <div class="my-page">
    <van-nav-bar title="我的" fixed placeholder />

    <!-- 用户信息 -->
    <div class="user-card" v-if="userStore.isLoggedIn">
      <img :src="userStore.avatar || defaultAvatar" class="user-avatar" />
      <div class="user-info">
        <div class="user-name">{{ userStore.nickname }}</div>
        <div class="user-id">ID: {{ userStore.userInfo?.id }}</div>
      </div>
      <van-icon name="setting-o" size="20" color="rgba(255,255,255,0.8)" @click="$router.push('/profile')" />
    </div>
    <div class="user-card login-card" v-else @click="$router.push('/login')">
      <img :src="defaultAvatar" class="user-avatar" />
      <div class="user-info">
        <div class="user-name">点击登录</div>
        <div class="user-id">登录后查看更多功能</div>
      </div>
      <van-icon name="arrow" size="16" color="rgba(255,255,255,0.6)" />
    </div>

    <!-- 钱包概览 -->
    <div class="wallet-overview" v-if="userStore.isLoggedIn">
      <div class="wallet-item" @click="$router.push('/wallet')">
        <span class="wallet-value">{{ formatMoney(wallet?.balance) }}</span>
        <span class="wallet-label">钱包余额</span>
      </div>
      <div class="wallet-divider"></div>
      <div class="wallet-item">
        <span class="wallet-value">{{ formatMoney(wallet?.totalIncome) }}</span>
        <span class="wallet-label">累计收入</span>
      </div>
      <div class="wallet-divider"></div>
      <div class="wallet-item">
        <span class="wallet-value">{{ formatMoney(wallet?.totalSupport) }}</span>
        <span class="wallet-label">累计投喂</span>
      </div>
    </div>

    <!-- 功能列表 -->
    <div class="menu-section">
      <div class="menu-card">
        <div class="menu-item" @click="$router.push('/campaign/my')">
          <van-icon name="orders-o" size="20" color="var(--qgc-primary)" />
          <span class="menu-text">我发起的</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
        <div class="menu-item" @click="goMySupports">
          <van-icon name="like-o" size="20" color="var(--qgc-secondary)" />
          <span class="menu-text">我支持的</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
        <div class="menu-item" @click="$router.push('/notifications')">
          <van-icon name="bell" size="20" color="var(--qgc-primary)" />
          <span class="menu-text">消息通知</span>
          <van-badge :content="unreadCount" v-if="unreadCount > 0" />
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
      </div>

      <div class="menu-card">
        <div class="menu-item" @click="$router.push('/wallet')">
          <van-icon name="balance-o" size="20" color="var(--qgc-secondary)" />
          <span class="menu-text">我的钱包</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
        <div class="menu-item" @click="$router.push('/wallet/withdraw')">
          <van-icon name="gold-coin-o" size="20" color="var(--qgc-primary)" />
          <span class="menu-text">提现</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
        <div class="menu-item" @click="$router.push('/wallet/flow')">
          <van-icon name="records" size="20" color="var(--qgc-text-secondary)" />
          <span class="menu-text">流水明细</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
      </div>

      <div class="menu-card">
        <div class="menu-item" @click="$router.push('/about')">
          <van-icon name="info-o" size="20" color="var(--qgc-text-tertiary)" />
          <span class="menu-text">关于</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
        <div class="menu-item" @click="$router.push('/agreement')">
          <van-icon name="description" size="20" color="var(--qgc-text-tertiary)" />
          <span class="menu-text">用户协议</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
      </div>

      <div class="menu-card" v-if="userStore.isLoggedIn">
        <div class="menu-item logout-item" @click="handleLogout">
          <van-icon name="revoke" size="20" color="var(--qgc-danger)" />
          <span class="menu-text" style="color: var(--qgc-danger)">退出登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWallet, getUnreadCount } from '@/api'
import { formatMoney } from '@/utils/money'
import { useUserStore } from '@/stores/user'
import { showDialog } from 'vant'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const wallet = ref(null)
const unreadCount = ref(0)

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64"><rect fill="%23e0e0e0" width="64" height="64" rx="32"/><text x="50%" y="55%" fill="%23999" font-size="20" text-anchor="middle">👻</text></svg>'

async function fetchWallet() {
  try {
    const res = await getWallet()
    wallet.value = res.data
  } catch { /* ignore */ }
}

async function fetchUnread() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch { /* ignore */ }
}

function handleLogout() {
  showDialog({ title: '确认退出', message: '确定要退出登录吗？' }).then(() => {
    userStore.logout()
  }).catch(() => {})
}

function goMySupports() {
  router.push('/campaign/my')
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchWallet()
    fetchUnread()
  }
})
</script>

<style scoped>
.my-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}

/* 用户卡片 */
.user-card {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-md);
  background: linear-gradient(135deg, var(--qgc-primary), var(--qgc-primary-dark));
  color: #fff;
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-lg);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-lg);
  box-shadow: var(--qgc-shadow-md);
}
.user-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.4);
  flex-shrink: 0;
}
.user-info { flex: 1; }
.user-name {
  font-size: var(--qgc-font-xl);
  font-weight: 700;
}
.user-id {
  font-size: var(--qgc-font-xs);
  opacity: 0.8;
  margin-top: 2px;
}
.login-card { cursor: pointer; }

/* 钱包概览 */
.wallet-overview {
  display: flex;
  align-items: center;
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}
.wallet-item {
  flex: 1;
  text-align: center;
  cursor: pointer;
}
.wallet-value {
  display: block;
  font-size: var(--qgc-font-lg);
  font-weight: 700;
  color: var(--qgc-primary);
}
.wallet-label {
  display: block;
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: 2px;
}
.wallet-divider {
  width: 1px;
  height: 30px;
  background: var(--qgc-border-light);
}

/* 菜单区 */
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
.logout-item {
  justify-content: center;
}
</style>