<template>
  <div class="login-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="登录" fixed placeholder />

    <div class="login-content">
      <div class="login-logo">👻</div>
      <h2 class="login-title">穷鬼筹</h2>
      <p class="login-desc">穷鬼互助，人间有爱</p>

      <van-cell-group inset>
        <van-field v-model="nickname" label="昵称" placeholder="输入你的穷鬼昵称" maxlength="20" />
      </van-cell-group>

      <div style="padding: 20px 16px">
        <van-button type="danger" block round :loading="logging" @click="handleDevLogin">
          🚀 快速登录(DEV)
        </van-button>
      </div>

      <div class="login-tip">
        开发环境使用Mock登录，生产环境将跳转微信授权
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

async function handleDevLogin() {
  if (!nickname.value.trim()) {
    showToast('请输入昵称')
    return
  }
  logging.value = true
  try {
    await userStore.login({ nickname: nickname.value })
    showToast('登录成功')
    router.replace('/')
  } catch { /* error handled by interceptor */ } finally {
    logging.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; background: #fff; }
.login-content { padding: 60px 0 20px; text-align: center; }
.login-logo { font-size: 64px; }
.login-title { font-size: 24px; font-weight: bold; color: #ff4500; margin: 10px 0 4px; }
.login-desc { font-size: 14px; color: #999; margin-bottom: 30px; }
.login-tip { font-size: 12px; color: #ccc; padding: 0 30px; margin-top: 20px; }
</style>