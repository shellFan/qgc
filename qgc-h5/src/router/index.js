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
        path: 'ranking',
        name: 'RankingTab',
        component: () => import('@/views/Ranking.vue'),
        meta: { title: '排行榜' }
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
    meta: { title: '发起筹款' }
  },
  {
    path: '/campaign/:id',
    name: 'CampaignDetail',
    component: () => import('@/views/CampaignDetail.vue'),
    meta: { title: '筹款详情' }
  },
  {
    path: '/pay',
    name: 'Pay',
    component: () => import('@/views/Pay.vue'),
    meta: { title: '投喂' }
  },
  {
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/Wallet.vue'),
    meta: { title: '我的钱包' }
  },
  {
    path: '/wallet/withdraw',
    name: 'Withdraw',
    component: () => import('@/views/Withdraw.vue'),
    meta: { title: '提现' }
  },
  {
    path: '/wallet/flow',
    name: 'WalletFlow',
    component: () => import('@/views/WalletFlow.vue'),
    meta: { title: '流水明细' }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/Notifications.vue'),
    meta: { title: '消息通知' }
  },
  {
    path: '/proof/create/:campaignId',
    name: 'ProofCreate',
    component: () => import('@/views/ProofCreate.vue'),
    meta: { title: '发布返图' }
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
    meta: { title: '个人主页' }
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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router