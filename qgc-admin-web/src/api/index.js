import request from '@/utils/request'

// ====== 管理员认证 ======
export const adminLogin = (data) => request.post('/admin/api/auth/login', data)
export const getAdminInfo = () => request.get('/admin/api/auth/info')

// ====== 仪表盘 ======
export const getDashboardOverview = () => request.get('/admin/api/dashboard/overview')
export const getDashboardTrend = (params) => request.get('/admin/api/dashboard/trend', { params })
export const getDashboardCategoryStats = () => request.get('/admin/api/dashboard/category-stats')
export const getDashboardPendingCounts = () => request.get('/admin/api/dashboard/pending-counts')

// ====== 筹款管理 ======
export const getCampaignList = (params) => request.get('/admin/api/campaigns', { params })
export const getCampaignDetail = (id) => request.get(`/admin/api/campaigns/${id}`)
export const approveCampaign = (id) => request.post(`/admin/api/campaigns/${id}/approve`)
export const rejectCampaign = (id, data) => request.post(`/admin/api/campaigns/${id}/reject`, data)
export const freezeCampaign = (id) => request.post(`/admin/api/campaigns/${id}/freeze`)
export const unfreezeCampaign = (id) => request.post(`/admin/api/campaigns/${id}/unfreeze`)
export const closeCampaign = (id, data) => request.post(`/admin/api/campaigns/${id}/close`, data)

// ====== 用户管理 ======
export const getUserList = (params) => request.get('/admin/api/users', { params })
export const getUserDetail = (id) => request.get(`/admin/api/users/${id}`)
export const disableUser = (id) => request.post(`/admin/api/users/${id}/disable`)
export const enableUser = (id) => request.post(`/admin/api/users/${id}/enable`)

// ====== 提现管理 ======
export const getWithdrawList = (params) => request.get('/admin/api/withdraws', { params })
export const approveWithdraw = (id) => request.post(`/admin/api/withdraws/${id}/approve`)
export const rejectWithdraw = (id, data) => request.post(`/admin/api/withdraws/${id}/reject`, data)
export const markWithdrawPaid = (id) => request.post(`/admin/api/withdraws/${id}/paid`)
export const markWithdrawPayFail = (id) => request.post(`/admin/api/withdraws/${id}/pay-fail`)