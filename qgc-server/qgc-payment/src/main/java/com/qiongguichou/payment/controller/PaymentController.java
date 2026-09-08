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
 * 支持三种支付模式: MOCK/NATIVE/JSAPI
 * - MOCK: 开发环境模拟
 * - NATIVE: 扫码支付, 返回code_url
 * - JSAPI: 微信公众号支付, 返回wxPayParams
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
     * payType参数可选, 不传则使用后端配置的默认模式
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
     * 不需要JWT/OAuth/RateLimit/CSRF, 需要APIv3验签+解密+金额/mchid校验
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
     * 前端JSAPI/Native支付后轮询此接口确认支付结果
     * 返回payType供前端区分支付模式
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
        data.put("payType", payment.getPayType());
        // Native支付: 返回codeUrl和过期时间(用于二维码刷新)
        if (payment.getCodeUrl() != null) {
            data.put("codeUrl", payment.getCodeUrl());
        }
        if (payment.getExpireTime() != null) {
            data.put("expireTime", payment.getExpireTime().toString());
        }
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