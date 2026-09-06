<template>
  <div class="notif-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="消息通知" fixed placeholder>
      <template #right>
        <van-button size="mini" @click="handleReadAll">全部已读</van-button>
      </template>
    </van-nav-bar>

    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
      <van-cell-group inset>
        <van-cell v-for="item in list" :key="item.id" :class="{ unread: item.isRead === 0 }" :title="item.title" :label="item.content">
          <template #value>
            <span class="notif-time">{{ formatTime(item.createTime) }}</span>
          </template>
        </van-cell>
      </van-cell-group>
      <van-empty v-if="!loading && list.length === 0" description="暂无消息" />
    </van-list>
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
.notif-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20px; }
.unread { background: #fff8f0; }
.notif-time { font-size: 11px; color: #999; }
</style>