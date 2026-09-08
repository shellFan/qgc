package com.qiongguichou.payment.controller;

import com.qiongguichou.common.result.Result;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.payment.dto.PayRequest;
import com.qiongguichou.payment.dto.PayResult;
import com.qiongguichou.payment.entity.PaymentOrder;
import com.qiongguichou.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 发起支付
     * POST /api/payment/pay
     */
    @PostMapping("/pay")
    public Result<PayResult> pay(@RequestBody @Validated PayRequest request) {
        Long userId = UserContext.getUserId();
        String openid = UserContext.getOpenid();
        PayResult result = paymentService.pay(userId, openid, request);
        return Result.success(result);
    }

    /**
     * 微信支付回调
     * POST /api/payment/notify/pay
     * 注意：此接口不需要登录，由微信服务器调用
     */
    @PostMapping("/notify/pay")
    public String handlePayNotify(HttpServletRequest request) {
        try {
            String body = readBody(request);
            return paymentService.handlePayNotify(body);
        } catch (Exception e) {
            log.error("支付回调异常", e);
            return "FAIL";
        }
    }

    /**
     * 微信退款回调
     * POST /api/payment/notify/refund
     */
    @PostMapping("/notify/refund")
    public String handleRefundNotify(HttpServletRequest request) {
        try {
            String body = readBody(request);
            return paymentService.handleRefundNotify(body);
        } catch (Exception e) {
            log.error("退款回调异常", e);
            return "FAIL";
        }
    }

    /**
     * 查询支付状态（前端轮询用）
     * GET /api/payment/{orderNo}/status
     * 前端JSAPI支付后轮询此接口确认支付结果
     */
    @GetMapping("/{orderNo}/status")
    public Result<Map<String, Object>> getPaymentStatus(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        PaymentOrder payment = paymentService.getByOrderNo(orderNo);
        if (payment == null) {
            return Result.error(1, "订单不存在");
        }
        // 安全校验：只能查自己的订单
        if (!payment.getSupporterUserId().equals(userId)) {
            return Result.error(1, "无权查看此订单");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", payment.getOrderNo());
        data.put("status", payment.getStatus());
        data.put("amount", payment.getAmount());
        return Result.success(data);
    }

    private String readBody(HttpServletRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}