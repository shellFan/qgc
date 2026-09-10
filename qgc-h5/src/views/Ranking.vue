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
          <div v-if="!loading && sadList.length === 0" class="empty-state">
            <span class="empty-emoji">📊</span>
            <p>今日暂无数据</p>
          </div>
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
          <div v-if="!loading && supporterList.length === 0" class="empty-state">
            <span class="empty-emoji">📊</span>
            <p>暂无数据</p>
          </div>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRanking } from '@/api'
import { showToast } from 'vant'

const router = useRouter()
const activeTab = ref('SADDEST_DAY')
const sadList = ref([])
const supporterList = ref([])
const loading = ref(false)

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40"><rect fill="%23e0e0e0" width="40" height="40" rx="20"/><text x="50%" y="55%" fill="%23999" font-size="12" text-anchor="middle">👻</text></svg>'

async function fetchRanking(type) {
  loading.value = true
  try {
    const res = await getRanking({ type })
    const list = res?.data || []
    if (type === 'SADDEST_DAY') sadList.value = list
    else supporterList.value = list
  } catch (e) {
    showToast('获取排行榜失败')
  } finally {
    loading.value = false
  }
}

function goDetail(item) {
  if (item.userId) {
    router.push(`/profile/${item.userId}`)
  }
}

onMounted(() => {
  fetchRanking('SADDEST_DAY')
  fetchRanking('MOST_SUPPORTERS')
})
</script>

<style scoped>
.ranking-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}
.ranking-list {
  padding: var(--qgc-spacing-sm);
}
.rank-item {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-md);
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-md);
  margin-bottom: var(--qgc-spacing-xs);
  box-shadow: var(--qgc-shadow-sm);
  transition: background 0.15s;
}
.rank-item:active {
  background: var(--qgc-bg-grey);
}
.rank-no {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--qgc-bg-grey);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--qgc-font-md);
  font-weight: 700;
  color: var(--qgc-text-tertiary);
  flex-shrink: 0;
}
.rank-no.top3 {
  background: var(--qgc-secondary-light);
  color: var(--qgc-secondary);
}
.rank-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}
.rank-info {
  flex: 1;
  min-width: 0;
}
.rank-name {
  font-size: var(--qgc-font-md);
  font-weight: 500;
  display: block;
  color: var(--qgc-text-primary);
}
.rank-score {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  display: block;
  margin-top: 2px;
}
.rank-badge {
  font-size: 20px;
  flex-shrink: 0;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  color: var(--qgc-text-tertiary);
}
.empty-emoji {
  font-size: 48px;
  margin-bottom: var(--qgc-spacing-sm);
}
.empty-state p {
  font-size: var(--qgc-font-md);
  margin: 0;
}
</style>