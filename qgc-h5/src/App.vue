<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  /* 品牌色体系 */
  --qgc-primary: #2db84b;
  --qgc-primary-light: #e8f5e9;
  --qgc-primary-dark: #1e8e3a;
  --qgc-secondary: #f5a623;
  --qgc-secondary-light: #fff8e1;
  --qgc-danger: #e53935;
  --qgc-danger-light: #ffebee;

  /* 文字色 */
  --qgc-text-primary: #1a1a1a;
  --qgc-text-secondary: #666666;
  --qgc-text-tertiary: #999999;
  --qgc-text-placeholder: #cccccc;

  /* 背景色 */
  --qgc-bg: #f5f5f5;
  --qgc-bg-white: #ffffff;
  --qgc-bg-card: #ffffff;
  --qgc-bg-grey: #f8f8f8;

  /* 边框色 */
  --qgc-border: #eeeeee;
  --qgc-border-light: #f0f0f0;

  /* 间距体系 */
  --qgc-spacing-xs: 4px;
  --qgc-spacing-sm: 8px;
  --qgc-spacing-md: 12px;
  --qgc-spacing-lg: 16px;
  --qgc-spacing-xl: 20px;
  --qgc-spacing-xxl: 24px;

  /* 圆角 */
  --qgc-radius-sm: 6px;
  --qgc-radius-md: 10px;
  --qgc-radius-lg: 14px;
  --qgc-radius-xl: 20px;
  --qgc-radius-full: 999px;

  /* 字号 */
  --qgc-font-xs: 11px;
  --qgc-font-sm: 12px;
  --qgc-font-md: 14px;
  --qgc-font-lg: 16px;
  --qgc-font-xl: 18px;
  --qgc-font-xxl: 22px;
  --qgc-font-hero: 28px;

  /* 阴影 */
  --qgc-shadow-sm: 0 1px 3px rgba(0,0,0,0.06);
  --qgc-shadow-md: 0 2px 8px rgba(0,0,0,0.08);
  --qgc-shadow-lg: 0 4px 16px rgba(0,0,0,0.1);

  /* Safe Area */
  --qgc-safe-top: env(safe-area-inset-top, 0px);
  --qgc-safe-bottom: env(safe-area-inset-bottom, 0px);
  --qgc-tabbar-height: 50px;
  --qgc-navbar-height: 46px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  -webkit-tap-highlight-color: transparent;
}

html, body {
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background-color: var(--qgc-bg);
  color: var(--qgc-text-primary);
  font-size: var(--qgc-font-md);
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 防止iOS Safari 100vh问题 */
html {
  height: 100%;
}
body {
  min-height: 100%;
  min-height: 100dvh;
}

/* iOS输入框防放大 */
input, textarea, select {
  font-size: 16px !important;
}

/* 全局滚动优化 */
::-webkit-scrollbar {
  display: none;
}

/* Vant主题覆盖 */
:root:root {
  --van-primary-color: var(--qgc-primary);
  --van-success-color: var(--qgc-primary);
  --van-danger-color: var(--qgc-danger);
  --van-warning-color: var(--qgc-secondary);
  --van-tabbar-height: var(--qgc-tabbar-height);
  --van-nav-bar-height: var(--qgc-navbar-height);
  --van-nav-bar-background: var(--qgc-bg-white);
  --van-nav-bar-icon-color: var(--qgc-text-primary);
  --van-nav-bar-text-color: var(--qgc-text-primary);
  --van-tabbar-item-active-color: var(--qgc-primary);
  --van-button-primary-background: var(--qgc-primary);
  --van-button-primary-border-color: var(--qgc-primary);
  --van-switch-on-background: var(--qgc-primary);
  --van-slider-active-background: var(--qgc-primary);
  --van-cascader-active-color: var(--qgc-primary);
  --van-checkbox-checked-icon-color: var(--qgc-primary);
  --van-radio-checked-icon-color: var(--qgc-primary);
  --van-stepper-input-font-size: var(--qgc-font-md);
}

/* 通用页面容器 */
.page-container {
  min-height: 100dvh;
  background: var(--qgc-bg);
  padding-bottom: calc(var(--qgc-tabbar-height) + var(--qgc-safe-bottom) + 10px);
}

/* 通用卡片 */
.qgc-card {
  background: var(--qgc-bg-card);
  border-radius: var(--qgc-radius-md);
  box-shadow: var(--qgc-shadow-sm);
  overflow: hidden;
}

/* 通用安全区域内边距 */
.safe-padding {
  padding-left: var(--qgc-spacing-lg);
  padding-right: var(--qgc-spacing-lg);
}

/* 文字省略 */
.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>