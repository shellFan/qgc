package com.qiongguichou.admin.controller;

import com.qiongguichou.common.result.Result;
import com.qiongguichou.content.entity.Ad;
import com.qiongguichou.content.entity.AdPosition;
import com.qiongguichou.content.service.AdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台-广告管理Controller
 */
@RestController
@RequestMapping("/admin/api/ad")
@RequiredArgsConstructor
public class AdminAdController {

    private final AdService adService;

    /**
     * 获取所有广告位
     */
    @GetMapping("/positions")
    public Result<List<AdPosition>> getPositions() {
        return Result.success(adService.getAllPositions());
    }

    /**
     * 获取所有广告
     */
    @GetMapping("/list")
    public Result<List<Ad>> getAdList() {
        return Result.success(adService.getAllAds());
    }

    /**
     * 创建广告
     */
    @PostMapping
    public Result<Ad> createAd(@RequestBody Ad ad) {
        return Result.success(adService.createAd(ad));
    }

    /**
     * 更新广告
     */
    @PutMapping("/{id}")
    public Result<Void> updateAd(@PathVariable Long id, @RequestBody Ad ad) {
        ad.setId(id);
        adService.updateAd(ad);
        return Result.success();
    }

    /**
     * 删除广告
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return Result.success();
    }

    /**
     * 上下架广告
     */
    @PutMapping("/{id}/toggle")
    public Result<Void> toggleAd(@PathVariable Long id) {
        Ad ad = adService.getById(id);
        if (ad != null) {
            ad.setEnabled(ad.getEnabled() == 1 ? 0 : 1);
            adService.updateAd(ad);
        }
        return Result.success();
    }
}