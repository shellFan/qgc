<template>
  <div class="flow-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="流水明细" fixed placeholder />

    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
      <van-cell-group inset>
        <van-cell v-for="item in flows" :key="item.id" :title="typeText(item.type)" :label="item.createTime">
          <template #value>
            <span :class="item.amount >= 0 ? 'flow-in' : 'flow-out'">
              {{ item.amount >= 0 ? '+' : '' }}{{ formatMoney(item.amount) }}
            </span>
          </template>
        </van-cell>
      </van-cell-group>
      <van-empty v-if="!loading && flows.length === 0" description="暂无流水" />
    </van-list>
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
.flow-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20px; }
.flow-in { color: #07c160; font-weight: 500; }
.flow-out { color: #ff4500; font-weight: 500; }
</style>