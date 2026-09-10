<template>
  <div class="login-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="登录" fixed placeholder />

    <div class="login-content">
      <div class="login-logo">👻</div>
      <h2 class="login-title">穷鬼筹</h2>
      <p class="login-desc">穷鬼互助，人间有爱</p>

      <div class="form-card">
        <van-field v-model="nickname" placeholder="输入你的穷鬼昵称" maxlength="20" clearable>
          <template #left-icon>
            <span style="font-size: 18px">😎</span>
          </template>
        </van-field>
      </div>

      <div class="submit-area">
        <van-button type="primary" block round :loading="logging" @click="handleQuickLogin">
          🚀 快速登录
        </van-button>
      </div>

      <div class="login-tip">
        输入昵称即可登录，新用户自动注册
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const nickname = ref('')
const logging = ref(false)

async function handleQuickLogin() {
  if (!nickname.value.trim()) {
    showToast('请输入昵称')
    return
  }
  logging.value = true
  try {
    await userStore.login({ nickname: nickname.value })
    showToast('登录成功')
    const redirect = router.currentRoute.value.query.redirect || '/'
    router.replace(redirect)
  } catch { /* error handled by interceptor */ } finally {
    logging.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}
.login-content {
  padding: 60px var(--qgc-spacing-lg) 20px;
  text-align: center;
}
.login-logo {
  font-size: 64px;
  margin-bottom: var(--qgc-spacing-md);
}
.login-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--qgc-primary);
  margin: 0 0 4px;
}
.login-desc {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-tertiary);
  margin: 0 0 var(--qgc-spacing-xl);
}
.form-card {
  background: var(--qgc-bg-white);
  border-radius: var(--qgc-radius-md);
  padding: var(--qgc-spacing-md);
  box-shadow: var(--qgc-shadow-sm);
  margin: 0 var(--qgc-spacing-md);
}
.submit-area {
  padding: var(--qgc-spacing-xl) var(--qgc-spacing-lg) 0;
}
.login-tip {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-quaternary);
  padding: 0 var(--qgc-spacing-xl);
  margin-top: var(--qgc-spacing-lg);
}
</style>