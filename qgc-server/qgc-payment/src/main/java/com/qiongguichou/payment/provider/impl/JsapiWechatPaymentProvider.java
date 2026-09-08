package com.qiongguichou.payment.provider.impl;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * JSAPI支付提供者 - 微信公众号H5支付
 * 需要openid/AppId, 仅在微信浏览器内使用
 * POST /v3/pay/transactions/jsapi
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "qgc.pay.mode", havingValue = "JSAPI")
public class JsapiWechatPaymentProvider implements PaymentProvider {

    private final WxPayService wxPayService;
    private final QgcWxPayConfig qgcWxPayConfig;

    @Override
    public PaymentResult createPayment(PaymentOrder paymentOrder, String openid) {
        if (openid == null || openid.isEmpty()) {
            throw new BusinessException(ErrorCode.PAYMENT_PARAMS_ERROR, "JSAPI支付需要openid");
        }
        try {
            WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
            request.setOutTradeNo(paymentOrder.getOrderNo());
            request.setDescription("穷鬼筹-投喂支持");
            request.setAmount(new WxPayUnifiedOrderV3Request.Amount()
                    .setTotal(paymentOrder.getAmount().intValue())
                    .setCurrency("CNY"));
            request.setPayer(new WxPayUnifiedOrderV3Request.Payer().setOpenid(openid));
            request.setNotifyUrl(qgcWxPayConfig.getNotifyUrl());

            WxPayMpOrderResult wxResult = wxPayService.createOrderV3(TradeTypeEnum.JSAPI, request);

            PaymentResult.WxPayParams params = new PaymentResult.WxPayParams();
            params.setAppId(wxResult.getAppId());
            params.setTimeStamp(wxResult.getTimeStamp());
            params.setNonceStr(wxResult.getNonceStr());
            params.setPackageValue(wxResult.getPackageValue());
            params.setSignType(wxResult.getSignType());
            params.setPaySign(wxResult.getPaySign());

            log.info("JSAPI下单成功: orderNo={}", paymentOrder.getOrderNo());
            return PaymentResult.jsapiPay(params);
        } catch (Exception e) {
            log.error("JSAPI下单失败: orderNo={}", paymentOrder.getOrderNo(), e);
            throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAIL);
        }
    }

    @Override
    public String queryPayment(String orderNo) {
        try {
            WxPayOrderQueryV3Result result = wxPayService.queryOrderV3(null, orderNo);
            return result.getTradeState();
        } catch (Exception e) {
            log.error("查询JSAPI支付状态失败: orderNo={}", orderNo, e);
            return "NOTPAY";
        }
    }

    @Override
    public void closePayment(String orderNo) {
        try {
            wxPayService.closeOrder(orderNo);
            log.info("关闭JSAPI订单成功: orderNo={}", orderNo);
        } catch (Exception e) {
            log.warn("关闭JSAPI订单失败: orderNo={}", orderNo, e);
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

            wxPayService.refundV3(request);
            log.info("JSAPI退款成功: orderNo={}, refundNo={}", orderNo, refundNo);
        } catch (Exception e) {
            log.error("JSAPI退款失败: orderNo={}, refundNo={}", orderNo, refundNo, e);
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
            log.error("JSAPI支付回调解析失败", e);
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
            log.error("JSAPI退款回调解析失败", e);
            return NotifyResult.fail(e.getMessage());
        }
    }

    @Override
    public String getPayType() {
        return PayType.JSAPI.name();
    }

    @Override
    public boolean isMock() {
        return false;
    }
}