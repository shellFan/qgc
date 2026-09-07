package com.qiongguichou.content.controller;

import com.qiongguichou.common.result.Result;
import com.qiongguichou.content.entity.Ad;
import com.qiongguichou.content.service.AdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告Controller(H5端)
 */
@RestController
@RequestMapping("/api/ad")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    /**
     * 获取指定位置的广告
     */
    @GetMapping("/position/{positionCode}")
    public Result<List<Ad>> getAdsByPosition(@PathVariable String positionCode) {
        return Result.success(adService.getAdsByPosition(positionCode));
    }

    /**
     * 记录广告曝光
     */
    @PostMapping("/{adId}/impression")
    public Result<Void> recordImpression(@PathVariable Long adId) {
        adService.recordImpression(adId);
        return Result.success();
    }

    /**
     * 记录广告点击
     */
    @PostMapping("/{adId}/click")
    public Result<Void> recordClick(@PathVariable Long adId) {
        adService.recordClick(adId);
        return Result.success();
    }
}