package com.qiongguichou.payment.provider.impl;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.bean.order.WxPayNativeOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.qiongguichou.common.enums.PayType;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.payment.config.QgcWxPayConfig;
import com.qiongguichou.payment.entity.PaymentOrder;
import com.qiongguichou.payment.provider.NotifyResult;
import com.qiongguichou.payment.provider.PaymentProvider;
import com.qiongguichou.payment.provider.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Native支付提供者 - 微信Native扫码支付
 * POST /v3/pay/transactions/native, 返回code_url
 * 无需openid/AppId, 适合PC/手机浏览器扫码
 * 普通商户DIRECT: 禁止sp_mchid/sub_mchid
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NativeWechatPaymentProvider implements PaymentProvider {

    private final WxPayService wxPayService;
    private final QgcWxPayConfig qgcWxPayConfig;

    @Override
    public PaymentResult createPayment(PaymentOrder paymentOrder, String openid) {
        try {
            WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
            request.setOutTradeNo(paymentOrder.getOrderNo());
            request.setDescription("穷鬼筹-投喂支持");
            request.setAmount(new WxPayUnifiedOrderV3Request.Amount()
                    .setTotal(paymentOrder.getAmount().intValue())
                    .setCurrency("CNY"));
            // Native支付不需要payer/openid
            request.setNotifyUrl(qgcWxPayConfig.getNotifyUrl());

            // 设置过期时间(5分钟)
            int expireMinutes = qgcWxPayConfig.getNativeExpireMinutes() > 0
                    ? qgcWxPayConfig.getNativeExpireMinutes() : 5;
            OffsetDateTime expireTime = OffsetDateTime.now().plusMinutes(expireMinutes);
            request.setTimeExpire(expireTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            Object result = wxPayService.createOrderV3(TradeTypeEnum.NATIVE, request);
            String codeUrl = extractCodeUrl(result);

            log.info("Native下单成功: orderNo={}, codeUrl={}", paymentOrder.getOrderNo(), codeUrl);

            return PaymentResult.nativePay(codeUrl,
                    expireTime.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (Exception e) {
            log.error("Native下单失败: orderNo={}", paymentOrder.getOrderNo(), e);
            throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAIL);
        }
    }

    static String extractCodeUrl(Object result) {
        if (result instanceof String) {
            return (String) result;
        }
        if (result instanceof WxPayNativeOrderResult) {
            return ((WxPayNativeOrderResult) result).getCodeUrl();
        }
        if (result instanceof WxPayUnifiedOrderV3Result) {
            return ((WxPayUnifiedOrderV3Result) result).getCodeUrl();
        }
        throw new IllegalStateException("微信Native下单返回类型异常: "
            + (result == null ? "null" : result.getClass().getName()));
    }


    @Override
    public String queryPayment(String orderNo) {
        try {
            WxPayOrderQueryV3Result result = wxPayService.queryOrderV3(null, orderNo);
            return result.getTradeState();
        } catch (Exception e) {
            log.error("查询支付状态失败: orderNo={}", orderNo, e);
            return "NOTPAY";
        }
    }

    @Override
    public void closePayment(String orderNo) {
        try {
            wxPayService.closeOrder(orderNo);
            log.info("关闭Native订单成功: orderNo={}", orderNo);
        } catch (Exception e) {
            log.warn("关闭Native订单失败: orderNo={}", orderNo, e);
        }
    }

    @Override
    public void refund(String orderNo, String refundNo, Long refundAmount, Long totalAmount, String reason) {
        try {
            WxPayRefundV3Request request = new WxPayRefundV3Request();
            request.setOutTradeNo(orderNo);
            request.setOutRefundNo(refundNo);
            request.setAmount(new WxPayRefundV3Request.Amount()
                    .setRefund(refundAmount.intValue())
                    .setTotal(totalAmount.intValue())
                    .setCurrency("CNY"));
            request.setReason(reason);
            request.setNotifyUrl(qgcWxPayConfig.getRefundNotifyUrl());

            WxPayRefundV3Result result = wxPayService.refundV3(request);
            log.info("Native退款成功: orderNo={}, refundNo={}", orderNo, refundNo);
        } catch (Exception e) {
            log.error("Native退款失败: orderNo={}, refundNo={}", orderNo, refundNo, e);
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_FAIL);
        }
    }

    @Override
    public NotifyResult handlePayNotify(String notifyData) {
        try {
            WxPayNotifyV3Result notifyResult = wxPayService.parseOrderNotifyV3Result(notifyData, null);
            WxPayNotifyV3Result.DecryptNotifyResult result = notifyResult.getResult();

            NotifyResult nr = NotifyResult.success();
            nr.setOutTradeNo(result.getOutTradeNo());
            nr.setTransactionId(result.getTransactionId());
            nr.setTotalAmount(Long.parseLong(result.getAmount().getTotal() + ""));
            return nr;
        } catch (Exception e) {
            log.error("Native支付回调解析失败", e);
            return NotifyResult.fail(e.getMessage());
        }
    }

    @Override
    public NotifyResult handleRefundNotify(String notifyData) {
        try {
            WxPayRefundNotifyV3Result notifyResult = wxPayService.parseRefundNotifyV3Result(notifyData, null);
            WxPayRefundNotifyV3Result.DecryptNotifyResult result = notifyResult.getResult();

            NotifyResult nr = NotifyResult.success();
            nr.setOutRefundNo(result.getOutRefundNo());
            nr.setRefundStatus(result.getRefundStatus());
            nr.setRefundTransactionId(result.getTransactionId());
            return nr;
        } catch (Exception e) {
            log.error("Native退款回调解析失败", e);
            return NotifyResult.fail(e.getMessage());
        }
    }

    @Override
    public String getPayType() {
        return PayType.NATIVE.name();
    }

    @Override
    public boolean isMock() {
        return false;
    }
}
