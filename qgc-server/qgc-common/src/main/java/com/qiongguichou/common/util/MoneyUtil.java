package com.qiongguichou.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具类(分)
 */
public class MoneyUtil {

    private MoneyUtil() {
    }

    /**
     * 元转分
     */
    public static long yuanToFen(BigDecimal yuan) {
        if (yuan == null) {
            return 0L;
        }
        return yuan.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /**
     * 分转元
     */
    public static BigDecimal fenToYuan(Long fen) {
        if (fen == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(fen).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 分转元字符串
     */
    public static String fenToYuanStr(Long fen) {
        return fenToYuan(fen).toPlainString();
    }
}