<template>
  <div class="withdraw-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="提现" fixed placeholder />

    <van-cell-group inset>
      <van-field v-model="amountYuan" label="提现金额" type="number" placeholder="输入提现金额(元)" required>
        <template #button>元</template>
      </van-field>
    </van-cell-group>

    <div style="padding: 8px 16px; font-size: 12px; color: #999;">
      可提现余额: {{ formatMoney(wallet?.balance) }}，最低提现1元
    </div>

    <div style="padding: 20px 16px">
      <van-button type="danger" block round :loading="submitting" @click="handleSubmit">
        申请提现
      </van-button>
    </div>

    <!-- 提现记录 -->
    <h3 style="padding: 12px 16px; font-size: 15px;">提现记录</h3>
    <van-cell-group inset>
      <van-cell v-for="item in records" :key="item.id" :title="formatMoney(item.amount)" :label="item.createTime">
        <template #value>
          <van-tag :type="statusType(item.status)">{{ statusText(item.status) }}</van-tag>
        </template>
      </van-cell>
      <van-empty v-if="records.length === 0" description="暂无提现记录" :image-size="60" />
    </van-cell-group>
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
.withdraw-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20px; }
</style>