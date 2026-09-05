package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.Admin;
import com.qiongguichou.admin.service.AdminAuthService;
import com.qiongguichou.admin.service.AdminWithdrawService;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.wallet.entity.WithdrawOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台-提现审核控制器
 */
@RestController
@RequestMapping("/admin/api/withdraws")
@RequiredArgsConstructor
public class AdminWithdrawController {

    private final AdminWithdrawService adminWithdrawService;
    private final AdminAuthService adminAuthService;

    /**
     * 分页查询提现订单列表
     */
    @GetMapping
    public Result<IPage<WithdrawOrder>> list(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "20") Integer size,
                                              @RequestParam(required = false) String status) {
        Page<WithdrawOrder> pageParam = new Page<>(page, size);
        return Result.success(adminWithdrawService.listWithdrawOrders(pageParam, status));
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestAttribute("adminId") Long adminId) {
        Admin admin = adminAuthService.getCurrentAdmin(adminId);
        adminWithdrawService.approveWithdraw(id, admin);
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
        adminWithdrawService.rejectWithdraw(id, params.get("reason"), admin);
        return Result.success();
    }

    /**
     * 标记打款成功
     */
    @PostMapping("/{id}/paid")
    public Result<Void> paid(@PathVariable Long id,
                              @RequestBody Map<String, String> params,
                              @RequestAttribute("adminId") Long adminId) {
        Admin admin = adminAuthService.getCurrentAdmin(adminId);
        adminWithdrawService.markPaid(id, params.get("transferNo"), admin);
        return Result.success();
    }

    /**
     * 标记打款失败
     */
    @PostMapping("/{id}/pay-fail")
    public Result<Void> payFail(@PathVariable Long id,
                                 @RequestBody Map<String, String> params,
                                 @RequestAttribute("adminId") Long adminId) {
        Admin admin = adminAuthService.getCurrentAdmin(adminId);
        adminWithdrawService.markPayFail(id, params.get("reason"), admin);
        return Result.success();
    }
}