import request from '@/utils/request'

// ====== 认证 ======
export const devLogin = (data) => request.post('/dev/login', data)
export const devLoginByOpenid = (data) => request.post('/dev/login-by-openid', data)
export const getAuthorizeUrl = () => request.get('/api/auth/authorize-url')
export const authCallback = (params) => request.get('/api/auth/callback', { params })
export const getJsSdkConfig = (params) => request.get('/api/auth/jssdk-config', { params })

// ====== 用户 ======
export const getUserInfo = () => request.get('/api/user/info')
export const updateUser = (data) => request.post('/api/user/update', data)
export const getUserCampaigns = (params) => request.get('/api/user/campaigns', { params })
export const getUserSupports = (params) => request.get('/api/user/supports', { params })
export const checkUser = () => request.get('/api/user/check')

// ====== 筹款 ======
export const getCampaignList = (params) => request.get('/api/campaign/list', { params })
export const getCampaignDetail = (id) => request.get(`/api/campaign/${id}`)
export const createCampaign = (data) => request.post('/api/campaign', data)
export const closeCampaign = (id) => request.post(`/api/campaign/${id}/close`)
export const getMyCampaigns = (params) => request.get('/api/campaign/my', { params })
export const getRandomMessages = () => request.get('/api/campaign/random-messages')
export const getCategoryList = () => request.get('/api/campaign/category/list')

// ====== 返图 ======
export const createProof = (data) => request.post('/api/campaign/proof', data)
export const getProofList = (campaignId) => request.get(`/api/campaign/proof/${campaignId}`)
export const likeProof = (proofId) => request.post(`/api/campaign/proof/${proofId}/like`)
export const unlikeProof = (proofId) => request.delete(`/api/campaign/proof/${proofId}/like`)

// ====== 支付 ======
export const createPayment = (data) => request.post('/api/payment/pay', data)

// ====== 钱包 ======
export const getWallet = () => request.get('/api/wallet')
export const applyWithdraw = (data) => request.post('/api/wallet/withdraw', data)
export const getWithdrawList = (params) => request.get('/api/wallet/withdraw/list', { params })
export const getFlowList = (params) => request.get('/api/wallet/flow/list', { params })

// ====== 评论 ======
export const addComment = (data) => request.post('/api/comment', data)
export const getCommentList = (targetId, params) => request.get(`/api/comment/list/${targetId}`, { params })
export const deleteComment = (commentId) => request.delete(`/api/comment/${commentId}`)

// ====== 通知 ======
export const getNotificationList = (params) => request.get('/api/notification/list', { params })
export const markNotificationRead = (id) => request.post(`/api/notification/read/${id}`)
export const markAllRead = () => request.post('/api/notification/read/all')
export const getUnreadCount = () => request.get('/api/notification/unread/count')

// ====== 举报 ======
export const createReport = (data) => request.post('/api/report', data)

// ====== 微信 ======
export const getWechatShareConfig = (params) => request.get('/api/wechat/share/config', { params })
export const getQrcode = (params) => request.get('/api/wechat/qrcode', { params })

// ====== 排行榜 ======
export const getRanking = (type) => request.get(`/api/ranking/${type}`)
export const getRankingHistory = (type, params) => request.get(`/api/ranking/${type}/history`, { params })

// ====== 分享 ======
export const recordShare = (data) => request.post('/api/share', data)
export const trackShareVisit = (data) => request.post('/api/share/visit', data)
export const getShareStats = (campaignId) => request.get(`/api/share/stats/${campaignId}`)

// ====== 用户等级/徽章/积分 ======
export const getUserLevel = (userId) => request.get(`/api/user/level/${userId}`)
export const getMyLevel = () => request.get('/api/user/level/mine')
export const getUserBadges = (userId) => request.get(`/api/user/badges/${userId}`)
export const getMyBadges = () => request.get('/api/user/badges/mine')
export const getUserPoints = (userId) => request.get(`/api/user/points/${userId}`)
export const getMyPoints = () => request.get('/api/user/points/mine')
export const getMyPointsFlow = (params) => request.get('/api/user/points/mine/flow', { params })
export const getUserProfile = (userId) => request.get(`/api/user/profile/${userId}`)
export const getMyProfile = () => request.get('/api/user/profile/mine')

// ====== 广告 ======
export const getAdsByPosition = (code) => request.get(`/api/ad/position/${code}`)
export const recordAdImpression = (adId) => request.post(`/api/ad/${adId}/impression`)
export const recordAdClick = (adId) => request.post(`/api/ad/${adId}/click`)