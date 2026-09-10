<template>
  <div class="notif-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="消息通知" fixed placeholder>
      <template #right>
        <van-button size="mini" @click="handleReadAll">全部已读</van-button>
      </template>
    </van-nav-bar>

    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
      <div class="notif-list">
        <div v-for="item in list" :key="item.id" class="notif-card" :class="{ unread: item.isRead === 0 }" @click="goDetail(item)">
          <div class="notif-dot" v-if="item.isRead === 0"></div>
          <div class="notif-body">
            <div class="notif-title">{{ item.title }}</div>
            <div class="notif-content">{{ item.content }}</div>
            <div class="notif-time">{{ formatTime(item.createTime) }}</div>
          </div>
        </div>
      </div>
    </van-list>

    <!-- 内联空状态 -->
    <div v-if="!loading && list.length === 0" class="empty-state">
      <div class="empty-emoji">🔔</div>
      <div class="empty-text">暂无消息</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showToast } from 'vant'
import { getNotificationList, markAllRead } from '@/api'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleDateString()
}

function goDetail(item) {
  // Mark as read locally
  if (item.isRead === 0) item.isRead = 1
}

async function loadMore() {
  loading.value = true
  try {
    const res = await getNotificationList({ page: page.value, size: 20 })
    const records = res.data?.records || res.data || []
    list.value.push(...records)
    if (records.length < 20) finished.value = true
    page.value++
  } catch { finished.value = true } finally { loading.value = false }
}

async function handleReadAll() {
  try {
    await markAllRead()
    list.value.forEach(i => i.isRead = 1)
    showToast('已全部标记为已读')
  } catch { /* ignore */ }
}
</script>

<style scoped>
.notif-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}

.notif-list {
  padding: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--qgc-spacing-sm);
}

.notif-card {
  display: flex;
  align-items: flex-start;
  gap: var(--qgc-spacing-sm);
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-lg);
  box-shadow: var(--qgc-shadow-sm);
  position: relative;
  transition: background 0.15s;
}
.notif-card.unread {
  background: var(--qgc-primary-light);
}
.notif-card:active {
  background: var(--qgc-bg-grey);
}

.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--qgc-danger);
  flex-shrink: 0;
  margin-top: 6px;
}

.notif-body {
  flex: 1;
  min-width: 0;
}
.notif-title {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
  line-height: 1.4;
}
.notif-content {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-secondary);
  margin-top: 4px;
  line-height: 1.4;
}
.notif-time {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-top: 6px;
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