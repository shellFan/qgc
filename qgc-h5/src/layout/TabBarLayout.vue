<template>
  <div class="tab-bar-layout">
    <div class="main-content">
      <router-view />
    </div>
    <van-tabbar v-model="active" route active-color="var(--qgc-primary)" inactive-color="#999" :placeholder="true" :safe-area-inset-bottom="true">
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/square" icon="chat-o">广场</van-tabbar-item>
      <van-tabbar-item to="/campaign/create" icon="add-o" class="create-tab">发起</van-tabbar-item>
      <van-tabbar-item to="/notifications" icon="chat-o" class="msg-tab">
        消息
        <template #icon="props">
          <van-icon name="chat-o" :class="props.active ? 'msg-icon-active' : ''" />
          <span v-if="unreadCount > 0" class="msg-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </template>
      </van-tabbar-item>
      <van-tabbar-item to="/my" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'

const active = ref(0)
const userStore = useUserStore()
const unreadCount = computed(() => userStore.unreadCount || 0)
</script>

<style scoped>
.tab-bar-layout {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  background: var(--qgc-bg);
}
.main-content {
  flex: 1;
  padding-bottom: calc(var(--qgc-tabbar-height) + var(--qgc-safe-bottom));
}

/* 发起按钮突出样式 */
.create-tab :deep(.van-tabbar-item__icon) {
  font-size: 24px;
  color: var(--qgc-primary);
}
.create-tab :deep(.van-tabbar-item__text) {
  font-weight: 600;
  color: var(--qgc-primary);
}

/* 消息Tab角标 */
.msg-tab {
  position: relative;
}
.msg-badge {
  position: absolute;
  top: -2px;
  right: 50%;
  transform: translateX(16px);
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  padding: 0 4px;
  font-size: 10px;
  color: #fff;
  text-align: center;
  background: var(--qgc-danger);
  border-radius: 999px;
  box-sizing: border-box;
}
</style>