import axios from 'axios'
import { showToast, showLoadingToast, closeToast } from 'vant'
import router from '@/router'

const request = axios.create({
  baseURL: '',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

let loadingCount = 0

function showLoading() {
  if (loadingCount === 0) {
    showLoadingToast({ message: '加载中...', forbidClick: true, duration: 0 })
  }
  loadingCount++
}

function hideLoading() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    closeToast()
  }
}

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if (config.showLoading !== false) {
    showLoading()
  }
  return config
})

request.interceptors.response.use(
  response => {
    hideLoading()
    const res = response.data
    if (res.code !== 0 && res.code !== 200) {
      showToast(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    hideLoading()
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    showToast(error.response?.data?.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request