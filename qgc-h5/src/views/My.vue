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
    </div>
    <div class="user-card login-card" v-else @click="$router.push('/login')">
      <img :src="defaultAvatar" class="user-avatar" />
      <div class="user-info">
        <div class="user-name">点击登录</div>
      </div>
    </div>

    <!-- 钱包概览 -->
    <van-cell-group inset style="margin-top: 12px" v-if="userStore.isLoggedIn">
      <van-cell title="钱包余额" :value="formatMoney(wallet?.balance)" is-link to="/wallet" />
      <van-cell title="累计收入" :value="formatMoney(wallet?.totalIncome)" />
      <van-cell title="累计投喂" :value="formatMoney(wallet?.totalSupport)" />
    </van-cell-group>

    <!-- 功能列表 -->
    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="我发起的" icon="orders-o" is-link to="/campaign/my" />
      <van-cell title="我支持的" icon="like-o" is-link />
      <van-cell title="消息通知" icon="bell" is-link to="/notifications">
        <template #right-icon>
          <van-badge :content="unreadCount" v-if="unreadCount > 0" /><van-icon name="arrow" />
        </template>
      </van-cell>
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="我的钱包" icon="balance-o" is-link to="/wallet" />
      <van-cell title="提现" icon="gold-coin-o" is-link to="/wallet/withdraw" />
      <van-cell title="流水明细" icon="records" is-link to="/wallet/flow" />
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px" v-if="userStore.isLoggedIn">
      <van-cell title="退出登录" icon="revoke" @click="handleLogout" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWallet, getUnreadCount } from '@/api'
import { formatMoney } from '@/utils/money'
import { useUserStore } from '@/stores/user'
import { showDialog } from 'vant'

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

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchWallet()
    fetchUnread()
  }
})
</script>

<style scoped>
.my-page { padding: 0 0 20px; }
.user-card { display: flex; align-items: center; gap: 14px; background: linear-gradient(135deg, #ff4500, #ff6b35); color: #fff; padding: 20px 16px; margin: 10px 12px; border-radius: 12px; }
.user-avatar { width: 56px; height: 56px; border-radius: 50%; border: 2px solid rgba(255,255,255,0.5); }
.user-name { font-size: 18px; font-weight: 600; }
.user-id { font-size: 12px; opacity: 0.8; margin-top: 2px; }
.login-card { cursor: pointer; }
</style>