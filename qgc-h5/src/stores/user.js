import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo, devLogin as devLoginApi } from '@/api'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname || '未登录')
  const avatar = computed(() => userInfo.value?.avatar || '')

  async function login(loginData) {
    const res = await devLoginApi(loginData)
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

  return { token, userInfo, isLoggedIn, nickname, avatar, login, fetchUserInfo, logout }
})