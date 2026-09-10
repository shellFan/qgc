package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.mapper.AdminLogMapper;
import com.qiongguichou.admin.service.AdminCategoryService;
import com.qiongguichou.campaign.entity.CampaignCategory;
import com.qiongguichou.campaign.mapper.CampaignCategoryMapper;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 后台-分类管理Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CampaignCategoryMapper campaignCategoryMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public IPage<CampaignCategory> listCategories(Page<CampaignCategory> page, Integer enabled) {
        LambdaQueryWrapper<CampaignCategory> wrapper = new LambdaQueryWrapper<>();
        if (enabled != null) {
            wrapper.eq(CampaignCategory::getEnabled, enabled);
        }
        wrapper.orderByAsc(CampaignCategory::getSort);
        return campaignCategoryMapper.selectPage(page, wrapper);
    }

    @Override
    public CampaignCategory getCategory(Long id) {
        CampaignCategory category = campaignCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    @Override
    public CampaignCategory createCategory(CampaignCategory category) {
        category.setEnabled(category.getEnabled() != null ? category.getEnabled() : 1);
        campaignCategoryMapper.insert(category);
        return category;
    }

    @Override
    public void updateCategory(Long id, CampaignCategory category) {
        getCategory(id);
        category.setId(id);
        campaignCategoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        CampaignCategory category = getCategory(id);
        category.setEnabled(0);
        campaignCategoryMapper.updateById(category);
    }

    @Override
    public void toggleCategory(Long id) {
        CampaignCategory category = getCategory(id);
        category.setEnabled(category.getEnabled() == 1 ? 0 : 1);
        campaignCategoryMapper.updateById(category);
    }

    private void logAction(Long adminId, String action, String targetId, String afterData) {
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction(action);
        log.setTargetType("CATEGORY");
        log.setTargetId(targetId);
        log.setAfterData(afterData);
        adminLogMapper.insert(log);
    }
}