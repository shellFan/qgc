<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">👻 穷鬼筹管理</div>
      <el-menu :default-active="activeMenu" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/campaigns">
          <el-icon><Tickets /></el-icon>
          <span>筹款管理</span>
        </el-menu-item>
        <el-menu-item index="/review">
          <el-icon><CircleCheck /></el-icon>
          <span>审核中心</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/withdraws">
          <el-icon><Money /></el-icon>
          <span>提现审核</span>
        </el-menu-item>
        <el-menu-item index="/ads">
          <el-icon><Picture /></el-icon>
          <span>广告管理</span>
        </el-menu-item>
        <el-menu-item index="/risk">
          <el-icon><Warning /></el-icon>
          <span>风控配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="page-title">{{ $route.meta.title }}</span>
        </div>
        <div class="header-right">
          <span class="admin-name">{{ adminInfo?.nickname || '管理员' }}</span>
          <el-button text @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminInfo } from '@/api'

const route = useRoute()
const router = useRouter()
const adminInfo = ref(null)

const activeMenu = computed(() => '/' + route.path.split('/')[1])

async function fetchAdminInfo() {
  try {
    const res = await getAdminInfo()
    adminInfo.value = res.data
  } catch { /* ignore */ }
}

function handleLogout() {
  localStorage.removeItem('admin_token')
  router.push('/login')
}

onMounted(() => { fetchAdminInfo() })
</script>

<style scoped>
.admin-layout { height: 100vh; }
.sidebar { background: #304156; overflow-y: auto; }
.logo { color: #fff; font-size: 18px; font-weight: bold; padding: 20px; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.1); }
.header { background: #fff; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.page-title { font-size: 16px; font-weight: 600; }
.header-right { display: flex; align-items: center; gap: 12px; }
.admin-name { font-size: 14px; color: #666; }
.main-content { background: #f0f2f5; }
</style>