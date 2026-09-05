package com.qiongguichou.campaign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.campaign.entity.CampaignCategory;
import com.qiongguichou.campaign.mapper.CampaignCategoryMapper;
import com.qiongguichou.campaign.service.CampaignCategoryService;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 筹款分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignCategoryServiceImpl implements CampaignCategoryService {

    private final CampaignCategoryMapper categoryMapper;

    @Override
    public List<CampaignCategory> getAll() {
        LambdaQueryWrapper<CampaignCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CampaignCategory::getEnabled, 1)
                .orderByAsc(CampaignCategory::getSort);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public CampaignCategory getById(Long id) {
        CampaignCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    @Override
    public CampaignCategory create(CampaignCategory category) {
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public boolean update(CampaignCategory category) {
        CampaignCategory existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public boolean toggleEnabled(Long id) {
        CampaignCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        category.setEnabled(category.getEnabled() == 1 ? 0 : 1);
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public boolean updateSort(Long id, Integer sort) {
        CampaignCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        category.setSort(sort);
        return categoryMapper.updateById(category) > 0;
    }
}