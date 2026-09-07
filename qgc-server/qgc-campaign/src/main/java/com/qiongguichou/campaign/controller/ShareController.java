package com.qiongguichou.campaign.controller;

import com.qiongguichou.campaign.entity.ShareRecord;
import com.qiongguichou.campaign.service.ShareService;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.common.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分享裂变Controller
 */
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    /**
     * 记录分享
     */
    @PostMapping
    public Result<ShareRecord> recordShare(
            @RequestParam Long campaignId,
            @RequestParam String shareType,
            @RequestParam(required = false) String source) {
        Long userId = UserContext.getUserId();
        ShareRecord record = shareService.recordShare(userId, campaignId, shareType, source);
        return Result.success(record);
    }

    /**
     * 通过分享码访问
     */
    @PostMapping("/visit/{shareCode}")
    public Result<Void> trackVisit(
            @PathVariable String shareCode,
            @RequestParam(required = false) String visitorKey) {
        shareService.trackVisit(shareCode, visitorKey);
        return Result.success();
    }

    /**
     * 获取我的分享统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getMyShareStats() {
        Long userId = UserContext.getUserId();
        ShareService.ShareStats stats = shareService.getUserShareStats(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("totalShares", stats.getTotalShares());
        result.put("totalVisitors", stats.getTotalVisitors());
        result.put("totalSupporters", stats.getTotalSupporters());
        result.put("totalSupportAmount", stats.getTotalSupportAmount());
        return Result.success(result);
    }

    /**
     * 获取筹款的分享记录
     */
    @GetMapping("/campaign/{campaignId}")
    public Result<List<ShareRecord>> getCampaignShares(@PathVariable Long campaignId) {
        return Result.success(shareService.getCampaignShareRecords(campaignId));
    }
}