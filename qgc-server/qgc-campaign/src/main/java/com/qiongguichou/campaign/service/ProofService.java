package com.qiongguichou.campaign.service;

import com.qiongguichou.campaign.dto.ProofCreateDTO;
import com.qiongguichou.campaign.dto.ProofDetailVO;

import java.util.List;

/**
 * 返图服务接口
 */
public interface ProofService {

    /**
     * 创建返图
     *
     * @param dto 返图创建请求
     * @return 返图详情
     */
    ProofDetailVO create(ProofCreateDTO dto);

    /**
     * 获取筹款项目的返图列表
     *
     * @param campaignId 筹款项目ID
     * @return 返图详情列表
     */
    List<ProofDetailVO> getByCampaignId(Long campaignId);

    /**
     * 点赞返图
     *
     * @param proofId 返图ID
     */
    void like(Long proofId);

    /**
     * 取消点赞返图
     *
     * @param proofId 返图ID
     */
    void unlike(Long proofId);

    /**
     * 删除返图
     *
     * @param proofId 返图ID
     */
    void delete(Long proofId);
}