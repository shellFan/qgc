package com.qiongguichou.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单号生成工具
 */
public class OrderNoUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    private OrderNoUtil() {
    }

    /**
     * 生成订单号: 前缀 + 时间 + 序列 + 随机数
     */
    public static String generate(String prefix) {
        String time = LocalDateTime.now().format(FORMATTER);
        long seq = SEQUENCE.incrementAndGet() % 10000;
        int random = (int) (Math.random() * 100);
        return prefix + time + String.format("%04d", seq) + String.format("%02d", random);
    }

    public static String campaignNo() {
        return generate("C");
    }

    public static String supportNo() {
        return generate("S");
    }

    public static String paymentNo() {
        return generate("P");
    }

    public static String refundNo() {
        return generate("R");
    }

    public static String withdrawNo() {
        return generate("W");
    }

    public static String flowNo() {
        return generate("F");
    }
}