package com.qiongguichou.payment.provider.impl;

import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NativeWechatPaymentProviderTest {

    @Test
    public void acceptsNativeSdkStringResult() {
        assertEquals("weixin://wxpay/bizpayurl?pr=test",
            NativeWechatPaymentProvider.extractCodeUrl("weixin://wxpay/bizpayurl?pr=test"));
    }

    @Test
    public void acceptsNativeSdkObjectResult() {
        WxPayUnifiedOrderV3Result result = new WxPayUnifiedOrderV3Result();
        result.setCodeUrl("weixin://wxpay/bizpayurl?pr=test");
        assertEquals("weixin://wxpay/bizpayurl?pr=test", NativeWechatPaymentProvider.extractCodeUrl(result));
    }
}
