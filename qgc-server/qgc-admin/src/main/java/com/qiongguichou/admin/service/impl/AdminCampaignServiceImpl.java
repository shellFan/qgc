package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.Admin;
import com.qiongguichou.admin.service.AdminCampaignService;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.mapper.CampaignMapper;
import com.qiongguichou.common.enums.CampaignStatus;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台-筹款管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCampaignServiceImpl implements AdminCampaignService {

    private final CampaignMapper campaignMapper;

    @Override
    public IPage<Campaign> listCampaigns(Page<Campaign> page, CampaignStatus status, String keyword) {
        LambdaQueryWrapper<Campaign> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Campaign::getStatus, status.name());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Campaign::getTitle, keyword));
        }
        wrapper.orderByDesc(Campaign::getCreateTime);
        return campaignMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long campaignId, Admin admin) {
        Campaign campaign = getCampaignAndCheck(campaignId, "PENDING_REVIEW");
        campaign.setStatus("ACTIVE");
        campaign.setUpdateBy(admin.getId());
        campaignMapper.updateById(campaign);
        log.info("筹款审核通过: campaignId={}, adminId={}", campaignId, admin.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long campaignId, String reason, Admin admin) {
        Campaign campaign = getCampaignAndCheck(campaignId, "PENDING_REVIEW");
        campaign.setStatus("REJECTED");
        campaign.setRejectReason(reason);
        campaign.setUpdateBy(admin.getId());
        campaignMapper.updateById(campaign);
        log.info("筹款审核拒绝: campaignId={}, adminId={}, reason={}", campaignId, admin.getId(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void riskFreeze(Long campaignId, String reason, Admin admin) {
        Campaign campaign = campaignMapper.selectById(campaignId);
        if (campaign == null) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }
        if (!"ACTIVE".equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_ACTIVE, "只有进行中的筹款可以冻结");
        }
        campaign.setStatus("RISK_FROZEN");
        campaign.setCloseReason(reason);
        campaign.setUpdateBy(admin.getId());
        campaignMapper.updateById(campaign);
        log.info("筹款风控冻结: campaignId={}, adminId={}, reason={}", campaignId, admin.getId(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreeze(Long campaignId, Admin admin) {
        Campaign campaign = campaignMapper.selectById(campaignId);
        if (campaign == null) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }
        if (!"RISK_FROZEN".equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有风控冻结的筹款可以解冻");
        }
        campaign.setStatus("ACTIVE");
        campaign.setCloseReason(null);
        campaign.setUpdateBy(admin.getId());
        campaignMapper.updateById(campaign);
        log.info("筹款解除风控冻结: campaignId={}, adminId={}", campaignId, admin.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceClose(Long campaignId, String reason, Admin admin) {
        Campaign campaign = campaignMapper.selectById(campaignId);
        if (campaign == null) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }
        if ("CLOSED".equals(campaign.getStatus()) || "SUCCESS".equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该筹款状态不允许关闭");
        }
        campaign.setStatus("CLOSED");
        campaign.setCloseReason(reason);
        campaign.setUpdateBy(admin.getId());
        campaignMapper.updateById(campaign);
        log.info("筹款强制关闭: campaignId={}, adminId={}, reason={}", campaignId, admin.getId(), reason);
    }

    @Override
    public Campaign getCampaignDetail(Long campaignId) {
        Campaign campaign = campaignMapper.selectById(campaignId);
        if (campaign == null) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }
        return campaign;
    }

    private Campaign getCampaignAndCheck(Long campaignId, String expectedStatus) {
        Campaign campaign = campaignMapper.selectById(campaignId);
        if (campaign == null) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }
        if (!expectedStatus.equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "筹款状态不正确");
        }
        return campaign;
    }
}