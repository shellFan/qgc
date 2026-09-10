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

// ====== 审核中心 ======
export const getReviewCampaigns = (params) => request.get('/admin/api/review/campaigns', { params })
export const reviewCampaign = (id, data) => request.post(`/admin/api/review/campaign/${id}`, data)
export const getReviewComments = (params) => request.get('/admin/api/review/comments', { params })
export const reviewComment = (id, data) => request.post(`/admin/api/review/comment/${id}`, data)
export const getReports = (params) => request.get('/admin/api/review/reports', { params })
export const handleReport = (id) => request.post(`/admin/api/review/report/${id}`)

// ====== 广告管理 ======
export const getAdList = () => request.get('/admin/api/ad/list')
export const createAd = (data) => request.post('/admin/api/ad', data)
export const updateAd = (id, data) => request.put(`/admin/api/ad/${id}`, data)
export const deleteAd = (id) => request.delete(`/admin/api/ad/${id}`)
export const toggleAd = (id) => request.put(`/admin/api/ad/${id}/toggle`)

// ====== 风控配置 ======
export const getRiskConfig = () => request.get('/admin/api/risk/config')
export const updateRiskConfig = (data) => request.post('/admin/api/risk/config', data)
export const getRiskRecords = (params) => request.get('/admin/api/risk/records', { params })
export const handleRiskRecord = (id) => request.post(`/admin/api/risk/record/${id}`)

// ====== 对账 ======
export const reconcileWallet = () => request.get('/admin/api/reconcile/wallet')
export const reconcileCampaign = () => request.get('/admin/api/reconcile/campaign')
export const reconcilePayment = () => request.get('/admin/api/reconcile/payment')

// ====== 分类管理 ======
export const getCategoryList = (params) => request.get('/admin/api/categories', { params })
export const getCategoryDetail = (id) => request.get(`/admin/api/categories/${id}`)
export const createCategory = (data) => request.post('/admin/api/categories', data)
export const updateCategory = (id, data) => request.put(`/admin/api/categories/${id}`, data)
export const toggleCategory = (id) => request.put(`/admin/api/categories/${id}/toggle`)

// ====== 支付/支持订单 ======
export const getPaymentList = (params) => request.get('/admin/api/payments', { params })
export const getPaymentDetail = (id) => request.get(`/admin/api/payments/${id}`)
export const refundPayment = (id, data) => request.post(`/admin/api/payments/${id}/refund`, data)

// ====== 钱包流水 ======
export const getWalletFlowList = (params) => request.get('/admin/api/wallet/flows', { params })

// ====== 返图管理 ======
export const getProofList = (params) => request.get('/admin/api/proofs', { params })
export const deleteProof = (id) => request.delete(`/admin/api/proofs/${id}`)

// ====== 评论管理 ======
export const getCommentList = (params) => request.get('/admin/api/comments', { params })
export const updateCommentStatus = (id, data) => request.put(`/admin/api/comments/${id}/status`, data)

// ====== 敏感词管理 ======
export const getSensitiveWordList = (params) => request.get('/admin/api/sensitive-words', { params })
export const createSensitiveWord = (data) => request.post('/admin/api/sensitive-words', data)
export const updateSensitiveWord = (id, data) => request.put(`/admin/api/sensitive-words/${id}`, data)
export const deleteSensitiveWord = (id) => request.delete(`/admin/api/sensitive-words/${id}`)
export const importSensitiveWords = (data) => request.post('/admin/api/sensitive-words/import', data)

// ====== 分享模板 ======
export const getShareTemplateList = (params) => request.get('/admin/api/share-templates', { params })
export const updateShareTemplate = (id, data) => request.put(`/admin/api/share-templates/${id}`, data)

// ====== 随机留言 ======
export const getRandomMessageList = (params) => request.get('/admin/api/random-messages', { params })
export const createRandomMessage = (data) => request.post('/admin/api/random-messages', data)
export const updateRandomMessage = (id, data) => request.put(`/admin/api/random-messages/${id}`, data)
export const deleteRandomMessage = (id) => request.delete(`/admin/api/random-messages/${id}`)
export const importRandomMessages = (data) => request.post('/admin/api/random-messages/import', data)

// ====== 系统配置 ======
export const getSystemConfigByGroup = (group) => request.get('/admin/api/system-config', { params: { group } })
export const updateSystemConfig = (data) => request.post('/admin/api/system-config', data)

// ====== 管理员管理 ======
export const getManagerList = (params) => request.get('/admin/api/managers', { params })
export const getManagerDetail = (id) => request.get(`/admin/api/managers/${id}`)
export const createManager = (data) => request.post('/admin/api/managers', data)
export const updateManager = (id, data) => request.put(`/admin/api/managers/${id}`, data)
export const resetManagerPassword = (id, data) => request.put(`/admin/api/managers/${id}/password`, data)
export const toggleManager = (id) => request.put(`/admin/api/managers/${id}/toggle`)

// ====== 角色管理 ======
export const getRoleList = () => request.get('/admin/api/roles')
export const getRoleDetail = (id) => request.get(`/admin/api/roles/${id}`)
export const updateRole = (id, data) => request.put(`/admin/api/roles/${id}`, data)

// ====== 审计日志 ======
export const getAuditLogList = (params) => request.get('/admin/api/audit-logs', { params })
export const getAuditLogDetail = (id) => request.get(`/admin/api/audit-logs/${id}`)

// ====== 举报管理 ======
export const getReportList = (params) => request.get('/admin/api/reports', { params })
export const handleReportAdmin = (id, data) => request.post(`/admin/api/reports/${id}`, data)