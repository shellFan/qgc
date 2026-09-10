import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'campaigns', name: 'Campaigns', component: () => import('@/views/Campaigns.vue'), meta: { title: '筹款管理' } },
      { path: 'review', name: 'Review', component: () => import('@/views/Review.vue'), meta: { title: '审核中心' } },
      { path: 'users', name: 'Users', component: () => import('@/views/Users.vue'), meta: { title: '用户管理' } },
      { path: 'withdraws', name: 'Withdraws', component: () => import('@/views/Withdraws.vue'), meta: { title: '提现审核' } },
      { path: 'categories', name: 'Categories', component: () => import('@/views/Categories.vue'), meta: { title: '分类管理' } },
      { path: 'payments', name: 'Payments', component: () => import('@/views/Payments.vue'), meta: { title: '支付订单' } },
      { path: 'wallet-flows', name: 'WalletFlows', component: () => import('@/views/WalletFlows.vue'), meta: { title: '钱包流水' } },
      { path: 'proofs', name: 'Proofs', component: () => import('@/views/Proofs.vue'), meta: { title: '返图管理' } },
      { path: 'comments', name: 'Comments', component: () => import('@/views/Comments.vue'), meta: { title: '评论管理' } },
      { path: 'sensitive-words', name: 'SensitiveWords', component: () => import('@/views/SensitiveWords.vue'), meta: { title: '敏感词管理' } },
      { path: 'share-templates', name: 'ShareTemplates', component: () => import('@/views/ShareTemplates.vue'), meta: { title: '分享模板' } },
      { path: 'random-messages', name: 'RandomMessages', component: () => import('@/views/RandomMessages.vue'), meta: { title: '随机留言' } },
      { path: 'system-config', name: 'SystemConfig', component: () => import('@/views/SystemConfig.vue'), meta: { title: '系统配置' } },
      { path: 'managers', name: 'Managers', component: () => import('@/views/Managers.vue'), meta: { title: '管理员管理' } },
      { path: 'roles', name: 'Roles', component: () => import('@/views/Roles.vue'), meta: { title: '角色管理' } },
      { path: 'audit-logs', name: 'AuditLogs', component: () => import('@/views/AuditLogs.vue'), meta: { title: '审计日志' } },
      { path: 'reports', name: 'Reports', component: () => import('@/views/Reports.vue'), meta: { title: '举报管理' } },
      { path: 'ads', name: 'Ads', component: () => import('@/views/Ads.vue'), meta: { title: '广告管理' } },
      { path: 'risk', name: 'Risk', component: () => import('@/views/Risk.vue'), meta: { title: '风控配置' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory('/admin/web/'),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) document.title = to.meta.title + ' - 穷鬼筹管理后台'
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router