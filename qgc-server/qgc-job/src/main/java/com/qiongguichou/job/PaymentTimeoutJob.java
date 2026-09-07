package com.qiongguichou.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qiongguichou.common.enums.PaymentStatus;
import com.qiongguichou.common.enums.SupportStatus;
import com.qiongguichou.payment.entity.PaymentOrder;
import com.qiongguichou.payment.entity.SupportOrder;
import com.qiongguichou.payment.mapper.PaymentOrderMapper;
import com.qiongguichou.payment.mapper.SupportOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付超时定时任务
 * 每2分钟检查：超过30分钟未支付的订单→CLOSED
 */
@Slf4j
@Component
public class PaymentTimeoutJob {

    /** 支付超时时间(分钟) */
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private SupportOrderMapper supportOrderMapper;

    /**
     * 每2分钟执行：关闭超时支付订单
     * 1. PaymentOrder: CREATED → CLOSED (createTime + 30min < now)
     * 2. 关联SupportOrder: CREATED → CLOSED
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void closeTimeoutPayments() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        log.info("开始执行支付超时关闭任务, threshold={}", timeoutThreshold);

        try {
            // 1. 查询超时的支付订单
            LambdaQueryWrapper<PaymentOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PaymentOrder::getStatus, PaymentStatus.CREATED.name())
                    .lt(PaymentOrder::getCreateTime, timeoutThreshold);
            List<PaymentOrder> timeoutOrders = paymentOrderMapper.selectList(queryWrapper);

            if (timeoutOrders.isEmpty()) {
                log.info("无超时支付订单");
                return;
            }

            int closedCount = 0;
            for (PaymentOrder order : timeoutOrders) {
                try {
                    // 2. 关闭支付订单: CREATED → CLOSED
                    LambdaUpdateWrapper<PaymentOrder> paymentWrapper = new LambdaUpdateWrapper<>();
                    paymentWrapper.eq(PaymentOrder::getId, order.getId())
                            .eq(PaymentOrder::getStatus, PaymentStatus.CREATED.name())
                            .set(PaymentOrder::getStatus, PaymentStatus.CLOSED.name())
                            .set(PaymentOrder::getUpdateTime, LocalDateTime.now());
                    int updated = paymentOrderMapper.update(null, paymentWrapper);

                    if (updated > 0) {
                        // 3. 取消关联的支持订单: CREATED → CLOSED
                        if (order.getSupportNo() != null) {
                            LambdaUpdateWrapper<SupportOrder> supportWrapper = new LambdaUpdateWrapper<>();
                            supportWrapper.eq(SupportOrder::getSupportNo, order.getSupportNo())
                                    .eq(SupportOrder::getStatus, SupportStatus.CREATED.name())
                                    .set(SupportOrder::getStatus, SupportStatus.CLOSED.name())
                                    .set(SupportOrder::getUpdateTime, LocalDateTime.now());
                            supportOrderMapper.update(null, supportWrapper);
                        }
                        closedCount++;
                    }
                } catch (Exception e) {
                    log.error("关闭超时支付订单失败, orderId={}", order.getId(), e);
                }
            }

            log.info("支付超时关闭任务完成: total={}, closed={}", timeoutOrders.size(), closedCount);
        } catch (Exception e) {
            log.error("支付超时关闭任务执行失败", e);
        }
    }
}