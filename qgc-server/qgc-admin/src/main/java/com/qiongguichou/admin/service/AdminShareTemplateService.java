package com.qiongguichou.admin.service;

import com.qiongguichou.campaign.entity.ShareTemplate;

import java.util.List;

/**
 * 后台-分享模板管理Service
 */
public interface AdminShareTemplateService {

    /**
     * 列表(按categoryId筛选)
     */
    List<ShareTemplate> listTemplates(Long categoryId);

    /**
     * 创建
     */
    ShareTemplate createTemplate(ShareTemplate template);

    /**
     * 更新
     */
    void updateTemplate(Long id, ShareTemplate template);

    /**
     * 删除
     */
    void deleteTemplate(Long id);

    /**
     * 启用/禁用切换
     */
    void toggleTemplate(Long id);
}