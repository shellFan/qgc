package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminCategoryService;
import com.qiongguichou.campaign.entity.CampaignCategory;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台-分类管理Controller
 */
@RestController
@RequestMapping("/admin/api/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    /**
     * 分页列表
     */
    @GetMapping
    public Result<IPage<CampaignCategory>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer enabled) {
        Page<CampaignCategory> p = new Page<>(page, size);
        return Result.success(adminCategoryService.listCategories(p, enabled));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<CampaignCategory> detail(@PathVariable Long id) {
        return Result.success(adminCategoryService.getCategory(id));
    }

    /**
     * 创建
     */
    @PostMapping
    public Result<CampaignCategory> create(@RequestBody CampaignCategory category) {
        return Result.success(adminCategoryService.createCategory(category));
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CampaignCategory category) {
        adminCategoryService.updateCategory(id, category);
        return Result.success();
    }

    /**
     * 删除(禁用)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 启用/禁用切换
     */
    @PutMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id) {
        adminCategoryService.toggleCategory(id);
        return Result.success();
    }
}