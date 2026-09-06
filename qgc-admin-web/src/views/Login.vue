<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">👻</div>
      <h2>穷鬼筹 管理后台</h2>
      <el-form :model="form" @submit.prevent="handleLogin" class="login-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" :loading="loading" native-type="submit">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api'

const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await adminLogin(form.value)
    localStorage.setItem('admin_token', res.data.token)
    ElMessage.success('登录成功')
    router.replace('/dashboard')
  } catch { /* error handled */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea, #764ba2); }
.login-card { background: #fff; border-radius: 12px; padding: 40px; width: 400px; text-align: center; box-shadow: 0 8px 32px rgba(0,0,0,0.15); }
.login-logo { font-size: 48px; margin-bottom: 8px; }
.login-card h2 { color: #333; margin-bottom: 24px; }
.login-form { text-align: left; }
</style>