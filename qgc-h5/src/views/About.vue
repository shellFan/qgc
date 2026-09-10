<template>
  <div class="about-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="关于" fixed placeholder />

    <div class="about-content">
      <div class="app-logo">
        <span class="logo-emoji">👻</span>
        <h2 class="app-name">穷鬼筹</h2>
        <p class="app-version">版本号：v{{ version }}</p>
      </div>

      <p class="app-desc">一个有趣的筹款平台，让每一份善意都被看见。</p>

      <div class="menu-card">
        <div class="menu-item" @click="$router.push('/agreement?tab=agreement')">
          <van-icon name="description" size="20" color="var(--qgc-primary)" />
          <span class="menu-text">用户协议</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
        <div class="menu-item" @click="$router.push('/agreement?tab=privacy')">
          <van-icon name="shield-o" size="20" color="var(--qgc-primary)" />
          <span class="menu-text">隐私政策</span>
          <van-icon name="arrow" size="14" color="var(--qgc-text-tertiary)" />
        </div>
      </div>

      <div class="menu-card">
        <div class="menu-item">
          <van-icon name="chat-o" size="20" color="var(--qgc-secondary)" />
          <span class="menu-text">官方微信</span>
          <span class="menu-value">穷鬼筹</span>
        </div>
        <div class="menu-item">
          <van-icon name="envelop-o" size="20" color="var(--qgc-secondary)" />
          <span class="menu-text">客服邮箱</span>
          <span class="menu-value">support@qgc.example.com</span>
        </div>
      </div>

      <p class="copyright">© 2026 穷鬼筹 All Rights Reserved</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const version = ref('1.0.0')
</script>

<style scoped>
.about-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}
.about-content {
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-md);
  text-align: center;
}
.app-logo {
  margin-bottom: var(--qgc-spacing-lg);
}
.logo-emoji {
  font-size: 64px;
  display: block;
  margin-bottom: var(--qgc-spacing-sm);
}
.app-name {
  font-size: 22px;
  font-weight: 700;
  color: var(--qgc-primary);
  margin: 0;
}
.app-version {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-tertiary);
  margin-top: 4px;
}
.app-desc {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-secondary);
  margin: 0 0 var(--qgc-spacing-lg);
}

.menu-card {
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  margin-top: var(--qgc-spacing-sm);
  box-shadow: var(--qgc-shadow-sm);
  overflow: hidden;
  text-align: left;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: var(--qgc-spacing-md);
  padding: var(--qgc-spacing-md) var(--qgc-spacing-lg);
  cursor: pointer;
  transition: background 0.15s;
}
.menu-item:active {
  background: var(--qgc-bg-grey);
}
.menu-item + .menu-item {
  border-top: 1px solid var(--qgc-border-light);
}
.menu-text {
  flex: 1;
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-primary);
}
.menu-value {
  font-size: var(--qgc-font-sm);
  color: var(--qgc-text-tertiary);
}

.copyright {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-quaternary);
  margin-top: var(--qgc-spacing-xl);
}
</style>