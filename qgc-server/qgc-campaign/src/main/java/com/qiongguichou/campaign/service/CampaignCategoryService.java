package com.qiongguichou.campaign.service;

import com.qiongguichou.campaign.entity.CampaignCategory;

import java.util.List;

/**
 * 筹款分类服务接口
 */
public interface CampaignCategoryService {

    /**
     * 获取所有启用的分类
     *
     * @return 分类列表
     */
    List<CampaignCategory> getAll();

    /**
     * 根据ID获取分类
     *
     * @param id 分类ID
     * @return 分类
     */
    CampaignCategory getById(Long id);

    /**
     * 创建分类
     *
     * @param category 分类信息
     * @return 创建后的分类
     */
    CampaignCategory create(CampaignCategory category);

    /**
     * 更新分类
     *
     * @param category 分类信息
     * @return 是否成功
     */
    boolean update(CampaignCategory category);

    /**
     * 切换启用/禁用状态
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean toggleEnabled(Long id);

    /**
     * 更新排序
     *
     * @param id   分类ID
     * @param sort 排序值
     * @return 是否成功
     */
    boolean updateSort(Long id, Integer sort);
}