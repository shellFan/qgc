package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.admin.service.AdminShareTemplateService;
import com.qiongguichou.campaign.entity.ShareTemplate;
import com.qiongguichou.campaign.mapper.ShareTemplateMapper;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台-分享模板管理Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminShareTemplateServiceImpl implements AdminShareTemplateService {

    private final ShareTemplateMapper shareTemplateMapper;

    @Override
    public List<ShareTemplate> listTemplates(Long categoryId) {
        LambdaQueryWrapper<ShareTemplate> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(ShareTemplate::getCategoryId, categoryId);
        }
        wrapper.orderByAsc(ShareTemplate::getSort);
        return shareTemplateMapper.selectList(wrapper);
    }

    @Override
    public ShareTemplate createTemplate(ShareTemplate template) {
        template.setEnabled(template.getEnabled() != null ? template.getEnabled() : 1);
        shareTemplateMapper.insert(template);
        return template;
    }

    @Override
    public void updateTemplate(Long id, ShareTemplate template) {
        getTemplate(id);
        template.setId(id);
        shareTemplateMapper.updateById(template);
    }

    @Override
    public void deleteTemplate(Long id) {
        shareTemplateMapper.deleteById(id);
    }

    @Override
    public void toggleTemplate(Long id) {
        ShareTemplate template = getTemplate(id);
        template.setEnabled(template.getEnabled() == 1 ? 0 : 1);
        shareTemplateMapper.updateById(template);
    }

    private ShareTemplate getTemplate(Long id) {
        ShareTemplate template = shareTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分享模板不存在");
        }
        return template;
    }
}