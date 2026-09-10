package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.campaign.entity.Proof;

import java.util.Map;

/**
 * 后台-返图管理Service
 */
public interface AdminProofService {

    /**
     * 分页查询返图列表
     */
    IPage<Proof> listProofs(Page<Proof> page, Long campaignId);

    /**
     * 获取返图详情(含图片)
     */
    Map<String, Object> getProofDetail(Long id);

    /**
     * 删除返图
     */
    void deleteProof(Long id, Long adminId);
}