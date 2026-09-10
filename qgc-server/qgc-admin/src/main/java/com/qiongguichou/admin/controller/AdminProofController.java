package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminProofService;
import com.qiongguichou.campaign.entity.Proof;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台-返图管理Controller
 */
@RestController
@RequestMapping("/admin/api/proofs")
@RequiredArgsConstructor
public class AdminProofController {

    private final AdminProofService adminProofService;

    /**
     * 分页列表
     */
    @GetMapping
    public Result<IPage<Proof>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long campaignId) {
        Page<Proof> p = new Page<>(page, size);
        return Result.success(adminProofService.listProofs(p, campaignId));
    }

    /**
     * 详情(含图片)
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(adminProofService.getProofDetail(id));
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                                @RequestAttribute("adminId") Long adminId) {
        adminProofService.deleteProof(id, adminId);
        return Result.success();
    }
}