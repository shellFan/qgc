<template>
  <div class="flow-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="流水明细" fixed placeholder />

    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
      <div class="flow-list">
        <div v-for="item in flows" :key="item.id" class="flow-card">
          <div class="flow-left">
            <div class="flow-type">{{ typeText(item.type) }}</div>
            <div class="flow-time">{{ item.createTime }}</div>
          </div>
          <div class="flow-amount" :class="item.amount >= 0 ? 'flow-in' : 'flow-out'">
            {{ item.amount >= 0 ? '+' : '' }}{{ formatMoney(item.amount) }}
          </div>
        </div>
      </div>
    </van-list>

    <!-- 内联空状态 -->
    <div v-if="!loading && flows.length === 0" class="empty-state">
      <div class="empty-emoji">💰</div>
      <div class="empty-text">暂无流水</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getFlowList } from '@/api'
import { formatMoney } from '@/utils/money'

const flows = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

function typeText(t) {
  const map = { CAMPAIGN_INCOME: '筹款收入', WITHDRAW_APPLY: '提现申请', WITHDRAW_SUCCESS: '提现成功', WITHDRAW_FAIL_RETURN: '提现退回', PAYMENT_REFUND: '退款', WECHAT_FEE: '微信手续费', ADMIN_ADJUST: '管理员调整' }
  return map[t] || t
}

async function loadMore() {
  loading.value = true
  try {
    const res = await getFlowList({ page: page.value, size: 20 })
    const list = res.data?.records || res.data || []
    flows.value.push(...list)
    if (list.length < 20) finished.value = true
    page.value++
  } catch { finished.value = true } finally { loading.value = false }
}
</script>

<style scoped>
.flow-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}

.flow-list {
  padding: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--qgc-spacing-sm);
}

.flow-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
}

.flow-left {
  flex: 1;
  min-width: 0;
}
.flow-type {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
}
.flow-time {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: 4px;
}

.flow-amount {
  font-size: var(--qgc-font-lg);
  font-weight: 700;
  flex-shrink: 0;
  margin-left: var(--qgc-spacing-md);
}
.flow-in {
  color: var(--qgc-primary);
}
.flow-out {
  color: var(--qgc-danger);
}

/* 内联空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0 40px;
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