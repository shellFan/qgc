package com.qiongguichou.campaign.controller;

import com.qiongguichou.campaign.entity.CampaignCategory;
import com.qiongguichou.campaign.service.CampaignCategoryService;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 筹款分类Controller
 */
@RestController
@RequestMapping("/api/campaign/category")
@RequiredArgsConstructor
public class CampaignCategoryController {

    private final CampaignCategoryService categoryService;

    /**
     * 获取所有启用的分类
     */
    @GetMapping("/list")
    public Result<List<CampaignCategory>> list() {
        List<CampaignCategory> categories = categoryService.getAll();
        return Result.success(categories);
    }
}