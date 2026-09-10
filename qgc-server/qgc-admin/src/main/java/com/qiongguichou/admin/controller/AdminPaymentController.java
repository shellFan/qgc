package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminPaymentService;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.payment.entity.PaymentOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台-支付管理Controller
 */
@RestController
@RequestMapping("/admin/api/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    /**
     * 分页列表
     */
    @GetMapping
    public Result<IPage<PaymentOrder>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long campaignId,
            @RequestParam(required = false) String orderNo) {
        Page<PaymentOrder> p = new Page<>(page, size);
        return Result.success(adminPaymentService.listPayments(p, status, campaignId, orderNo));
    }

    /**
     * 详情(含支持订单信息)
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(adminPaymentService.getPaymentDetail(id));
    }

    /**
     * 发起退款
     */
    @PostMapping("/{id}/refund")
    public Result<Void> refund(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestAttribute("adminId") Long adminId) {
        Long amount = Long.valueOf(body.get("amount").toString());
        String reason = (String) body.get("reason");
        adminPaymentService.refund(id, amount, reason, adminId);
        return Result.success();
    }
}