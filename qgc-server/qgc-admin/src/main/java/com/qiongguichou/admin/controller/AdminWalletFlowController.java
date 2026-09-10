package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminWalletFlowService;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.wallet.entity.WalletFlow;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台-钱包流水Controller
 */
@RestController
@RequestMapping("/admin/api/wallet/flows")
@RequiredArgsConstructor
public class AdminWalletFlowController {

    private final AdminWalletFlowService adminWalletFlowService;

    /**
     * 分页列表
     */
    @GetMapping
    public Result<IPage<WalletFlow>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type) {
        Page<WalletFlow> p = new Page<>(page, size);
        return Result.success(adminWalletFlowService.listFlows(p, userId, type));
    }
}