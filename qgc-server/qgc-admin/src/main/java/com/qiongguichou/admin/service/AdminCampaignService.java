package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.Admin;
import com.qiongguichou.common.enums.CampaignStatus;
import com.qiongguichou.campaign.entity.Campaign;

/**
 * 后台-筹款管理服务
 */
public interface AdminCampaignService {

    /**
     * 分页查询筹款列表（支持状态/关键词筛选）
     */
    IPage<Campaign> listCampaigns(Page<Campaign> page, CampaignStatus status, String keyword);

    /**
     * 审核通过
     */
    void approve(Long campaignId, Admin admin);

    /**
     * 审核拒绝
     */
    void reject(Long campaignId, String reason, Admin admin);

    /**
     * 风控冻结
     */
    void riskFreeze(Long campaignId, String reason, Admin admin);

    /**
     * 解除风控冻结
     */
    void unfreeze(Long campaignId, Admin admin);

    /**
     * 强制关闭筹款
     */
    void forceClose(Long campaignId, String reason, Admin admin);

    /**
     * 获取筹款详情
     */
    Campaign getCampaignDetail(Long campaignId);
}