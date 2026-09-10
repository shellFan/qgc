package com.qiongguichou.admin.controller;

import com.qiongguichou.admin.service.AdminShareTemplateService;
import com.qiongguichou.campaign.entity.ShareTemplate;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台-分享模板管理Controller
 */
@RestController
@RequestMapping("/admin/api/share-templates")
@RequiredArgsConstructor
public class AdminShareTemplateController {

    private final AdminShareTemplateService adminShareTemplateService;

    /**
     * 列表
     */
    @GetMapping
    public Result<List<ShareTemplate>> list(
            @RequestParam(required = false) Long categoryId) {
        return Result.success(adminShareTemplateService.listTemplates(categoryId));
    }

    /**
     * 创建
     */
    @PostMapping
    public Result<ShareTemplate> create(@RequestBody ShareTemplate template) {
        return Result.success(adminShareTemplateService.createTemplate(template));
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ShareTemplate template) {
        adminShareTemplateService.updateTemplate(id, template);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminShareTemplateService.deleteTemplate(id);
        return Result.success();
    }

    /**
     * 启用/禁用切换
     */
    @PutMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id) {
        adminShareTemplateService.toggleTemplate(id);
        return Result.success();
    }
}