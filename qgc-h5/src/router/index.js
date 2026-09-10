import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layout/TabBarLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '穷鬼筹' }
      },
      {
        path: 'square',
        name: 'Square',
        component: () => import('@/views/Square.vue'),
        meta: { title: '穷鬼广场' }
      },
      {
        path: 'notifications',
        name: 'NotificationsTab',
        component: () => import('@/views/Notifications.vue'),
        meta: { title: '消息通知', requiresAuth: true }
      },
      {
        path: 'my',
        name: 'My',
        component: () => import('@/views/My.vue'),
        meta: { title: '我的' }
      }
    ]
  },
  {
    path: '/campaign/create',
    name: 'CampaignCreate',
    component: () => import('@/views/CampaignCreate.vue'),
    meta: { title: '发起筹款', requiresAuth: true }
  },
  {
    path: '/campaign/create-success',
    name: 'CampaignCreateSuccess',
    component: () => import('@/views/CampaignCreateSuccess.vue'),
    meta: { title: '发起成功', requiresAuth: true }
  },
  {
    path: '/campaign/:id',
    name: 'CampaignDetail',
    component: () => import('@/views/CampaignDetail.vue'),
    meta: { title: '筹款详情' }
  },
  {
    path: '/campaign/my',
    name: 'MyCampaigns',
    component: () => import('@/views/MyCampaigns.vue'),
    meta: { title: '我发起的', requiresAuth: true }
  },
  {
    path: '/campaign/manage/:id',
    name: 'CampaignManage',
    component: () => import('@/views/CampaignManage.vue'),
    meta: { title: '管理筹款', requiresAuth: true }
  },
  {
    path: '/pay',
    name: 'Pay',
    component: () => import('@/views/Pay.vue'),
    meta: { title: '投喂', requiresAuth: true }
  },
  {
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/Wallet.vue'),
    meta: { title: '我的钱包', requiresAuth: true }
  },
  {
    path: '/wallet/withdraw',
    name: 'Withdraw',
    component: () => import('@/views/Withdraw.vue'),
    meta: { title: '提现', requiresAuth: true }
  },
  {
    path: '/wallet/flow',
    name: 'WalletFlow',
    component: () => import('@/views/WalletFlow.vue'),
    meta: { title: '流水明细', requiresAuth: true }
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: () => import('@/views/Ranking.vue'),
    meta: { title: '排行榜' }
  },
  {
    path: '/proof/create/:campaignId',
    name: 'ProofCreate',
    component: () => import('@/views/ProofCreate.vue'),
    meta: { title: '发布返图', requiresAuth: true }
  },
  {
    path: '/pay-success',
    name: 'PaySuccess',
    component: () => import('@/views/PaySuccess.vue'),
    meta: { title: '投喂成功' }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('@/views/UserProfile.vue'),
    meta: { title: '个人主页', requiresAuth: true }
  },
  {
    path: '/profile/:userId',
    name: 'UserProfileOther',
    component: () => import('@/views/UserProfile.vue'),
    meta: { title: '用户主页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/agreement',
    name: 'Agreement',
    component: () => import('@/views/Agreement.vue'),
    meta: { title: '协议与政策' }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/About.vue'),
    meta: { title: '关于' }
  }
]

const router = createRouter({
  history: createWebHistory('/h5/'),
  routes
})

// 路由守卫：鉴权 + 标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }

  // 鉴权检查
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  next()
})

export default router