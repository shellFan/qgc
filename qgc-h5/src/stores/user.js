import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo, quickLogin } from '@/api'
import router from '@/router'

function getDeviceId() {
  let deviceId = localStorage.getItem('qgc_device_id')
  if (!deviceId) {
    deviceId = typeof crypto?.randomUUID === 'function'
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(36).slice(2)}`
    localStorage.setItem('qgc_device_id', deviceId)
  }
  return deviceId
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const unreadCount = ref(0)

  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname || '未登录')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // 生产环境登录（快速登录，不依赖微信OAuth）
  async function login(loginData) {
    const res = await quickLogin({ ...loginData, deviceId: getDeviceId() })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    router.push('/login')
  }

  return { token, userInfo, unreadCount, isLoggedIn, nickname, avatar, login, fetchUserInfo, logout }
})
