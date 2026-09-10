package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.campaign.entity.CampaignCategory;

import java.util.List;

/**
 * 后台-分类管理Service
 */
public interface AdminCategoryService {

    /**
     * 分页查询分类列表
     */
    IPage<CampaignCategory> listCategories(Page<CampaignCategory> page, Integer enabled);

    /**
     * 获取分类详情
     */
    CampaignCategory getCategory(Long id);

    /**
     * 创建分类
     */
    CampaignCategory createCategory(CampaignCategory category);

    /**
     * 更新分类
     */
    void updateCategory(Long id, CampaignCategory category);

    /**
     * 删除分类(禁用)
     */
    void deleteCategory(Long id);

    /**
     * 启用/禁用切换
     */
    void toggleCategory(Long id);
}