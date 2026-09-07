<template>
  <div class="ranking-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="排行榜" fixed placeholder />

    <van-tabs v-model:active="activeTab" sticky>
      <van-tab title="今日最惨" name="SADDEST_DAY">
        <div class="ranking-list">
          <div v-for="(item, idx) in sadList" :key="item.id || idx" class="rank-item" @click="goDetail(item)">
            <div class="rank-no" :class="{ top3: idx < 3 }">{{ idx + 1 }}</div>
            <img :src="item.avatar || defaultAvatar" class="rank-avatar" />
            <div class="rank-info">
              <span class="rank-name">{{ item.anonymous ? '匿名义父' : (item.nickname || '穷鬼') }}</span>
              <span class="rank-score">{{ item.scoreLabel }}</span>
            </div>
            <div class="rank-badge" v-if="idx < 3">{{ ['🥇','🥈','🥉'][idx] }}</div>
          </div>
          <van-empty v-if="!loading && sadList.length === 0" description="今日暂无数据" />
        </div>
      </van-tab>
      <van-tab title="最多义父" name="MOST_SUPPORTERS">
        <div class="ranking-list">
          <div v-for="(item, idx) in supporterList" :key="item.id || idx" class="rank-item" @click="goDetail(item)">
            <div class="rank-no" :class="{ top3: idx < 3 }">{{ idx + 1 }}</div>
            <img :src="item.avatar || defaultAvatar" class="rank-avatar" />
            <div class="rank-info">
              <span class="rank-name">{{ item.anonymous ? '匿名义父' : (item.nickname || '穷鬼') }}</span>
              <span class="rank-score">{{ item.scoreLabel }}</span>
            </div>
            <div class="rank-badge" v-if="idx < 3">{{ ['🥇','🥈','🥉'][idx] }}</div>
          </div>
          <van-empty v-if="!loading && supporterList.length === 0" description="暂无数据" />
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeTab = ref('SADDEST_DAY')
const sadList = ref([])
const supporterList = ref([])
const loading = ref(false)

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40"><rect fill="%23e0e0e0" width="40" height="40" rx="20"/><text x="50%" y="55%" fill="%23999" font-size="12" text-anchor="middle">👻</text></svg>'

async function fetchRanking(type) {
  loading.value = true
  try {
    const res = await fetch(`/api/ranking/${type}`).then(r => r.json())
    const list = res?.data || []
    if (type === 'SADDEST_DAY') sadList.value = list
    else supporterList.value = list
  } catch { /* ignore */ }
  loading.value = false
}

function goDetail(item) {
  // 排行榜项可能没有campaignId，暂不跳转
}

onMounted(() => {
  fetchRanking('SADDEST_DAY')
  fetchRanking('MOST_SUPPORTERS')
})
</script>

<style scoped>
.ranking-page { min-height: 100vh; background: #f5f5f5; }
.ranking-list { padding: 12px; }
.rank-item { display: flex; align-items: center; gap: 12px; background: #fff; border-radius: 10px; padding: 12px; margin-bottom: 8px; }
.rank-no { width: 28px; height: 28px; border-radius: 50%; background: #f0f0f0; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: bold; color: #999; flex-shrink: 0; }
.rank-no.top3 { background: #fff0e6; color: #ff4500; }
.rank-avatar { width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0; }
.rank-info { flex: 1; min-width: 0; }
.rank-name { font-size: 14px; font-weight: 500; display: block; }
.rank-score { font-size: 12px; color: #999; display: block; margin-top: 2px; }
.rank-badge { font-size: 20px; flex-shrink: 0; }
</style>