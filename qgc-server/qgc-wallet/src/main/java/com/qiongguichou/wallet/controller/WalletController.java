package com.qiongguichou.wallet.controller;

import com.qiongguichou.common.result.Result;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.wallet.dto.WalletVO;
import com.qiongguichou.wallet.dto.WithdrawRequest;
import com.qiongguichou.wallet.entity.WalletFlow;
import com.qiongguichou.wallet.entity.WithdrawOrder;
import com.qiongguichou.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 钱包控制器
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * 获取钱包信息
     */
    @GetMapping
    public Result<WalletVO> getWallet() {
        Long userId = UserContext.getUserId();
        return Result.success(walletService.getWallet(userId));
    }

    /**
     * 申请提现
     */
    @PostMapping("/withdraw")
    public Result<WithdrawOrder> applyWithdraw(@RequestBody @Validated WithdrawRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(walletService.applyWithdraw(userId, request));
    }

    /**
     * 提现记录
     */
    @GetMapping("/withdraw/list")
    public Result<List<WithdrawOrder>> getWithdrawList() {
        Long userId = UserContext.getUserId();
        return Result.success(walletService.getWithdrawList(userId));
    }

    /**
     * 流水记录
     */
    @GetMapping("/flow/list")
    public Result<List<WalletFlow>> getFlowList() {
        Long userId = UserContext.getUserId();
        return Result.success(walletService.getFlowList(userId));
    }
}