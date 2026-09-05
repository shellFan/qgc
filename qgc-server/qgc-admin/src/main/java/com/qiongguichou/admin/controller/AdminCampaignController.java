package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.Admin;
import com.qiongguichou.admin.service.AdminAuthService;
import com.qiongguichou.admin.service.AdminCampaignService;
import com.qiongguichou.common.enums.CampaignStatus;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.campaign.entity.Campaign;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台-筹款管理控制器
 */
@RestController
@RequestMapping("/admin/api/campaigns")
@RequiredArgsConstructor
public class AdminCampaignController {

    private final AdminCampaignService adminCampaignService;
    private final AdminAuthService adminAuthService;

    /**
     * 分页查询筹款列表
     */
    @GetMapping
    public Result<IPage<Campaign>> list(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "20") Integer size,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) String keyword) {
        CampaignStatus campaignStatus = null;
        if (status != null && !status.isEmpty()) {
            campaignStatus = CampaignStatus.valueOf(status);
        }
        Page<Campaign> pageParam = new Page<>(page, size);
        return Result.success(adminCampaignService.listCampaigns(pageParam, campaignStatus, keyword));
    }

    /**
     * 获取筹款详情
     */
    @GetMapping("/{id}")
    public Result<Campaign> detail(@PathVariable Long id) {
        return Result.success(adminCampaignService.getCampaignDetail(id));
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestAttribute("adminId") Long adminId) {
        Admin admin = adminAuthService.getCurrentAdmin(adminId);
        adminCampaignService.approve(id, admin);
        return Result.success();
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                                @RequestBody Map<String, String> params,
                                @RequestAttribute("adminId") Long adminId) {
        Admin admin = adminAuthService.getCurrentAdmin(adminId);
        adminCampaignService.reject(id, params.get("reason"), admin);
        return Result.success();
    }

    /**
     * 风控冻结
     */
    @PostMapping("/{id}/freeze")
    public Result<Void> freeze(@PathVariable Long id,
                                @RequestBody Map<String, String> params,
                                @RequestAttribute("adminId") Long adminId) {
        Admin admin = adminAuthService.getCurrentAdmin(adminId);
        adminCampaignService.riskFreeze(id, params.get("reason"), admin);
        return Result.success();
    }

    /**
     * 解除风控冻结
     */
    @PostMapping("/{id}/unfreeze")
    public Result<Void> unfreeze(@PathVariable Long id, @RequestAttribute("adminId") Long adminId) {
        Admin admin = adminAuthService.getCurrentAdmin(adminId);
        adminCampaignService.unfreeze(id, admin);
        return Result.success();
    }

    /**
     * 强制关闭
     */
    @PostMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id,
                               @RequestBody Map<String, String> params,
                               @RequestAttribute("adminId") Long adminId) {
        Admin admin = adminAuthService.getCurrentAdmin(adminId);
        adminCampaignService.forceClose(id, params.get("reason"), admin);
        return Result.success();
    }
}