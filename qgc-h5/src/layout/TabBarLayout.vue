<template>
  <div class="tab-bar-layout">
    <div class="main-content">
      <router-view />
    </div>
    <van-tabbar v-model="active" route active-color="#ff4500" inactive-color="#999">
      <van-tabbar-item to="/" icon="home-o">广场</van-tabbar-item>
      <van-tabbar-item to="/my" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const active = ref(0)
</script>

<style scoped>
.tab-bar-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.main-content {
  flex: 1;
  padding-bottom: 50px;
}
</style>