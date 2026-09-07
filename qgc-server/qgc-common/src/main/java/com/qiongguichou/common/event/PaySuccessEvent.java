package com.qiongguichou.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 支付成功事件
 * 在支付回调事务提交后触发，用于异步处理钱包入账、缓存清理等
 *
 * 事件流程：
 * 1. PaymentServiceImpl.handlePayNotify → @Transactional 内处理支付
 * 2. processPaySuccess → 更新支持订单 + 更新筹款金额 + 检查超额
 * 3. 发布 PaySuccessEvent → 事务提交后
 * 4. PaySuccessEventListener → 钱包入账 + 缓存清理
 */
@Getter
public class PaySuccessEvent extends ApplicationEvent {

    private final Long campaignId;
    private final Long creatorUserId;
    private final Long supporterUserId;
    private final Long effectiveAmount;
    private final String campaignTitle;

    public PaySuccessEvent(Object source, Long campaignId, Long creatorUserId,
                           Long supporterUserId, Long effectiveAmount, String campaignTitle) {
        super(source);
        this.campaignId = campaignId;
        this.creatorUserId = creatorUserId;
        this.supporterUserId = supporterUserId;
        this.effectiveAmount = effectiveAmount;
        this.campaignTitle = campaignTitle;
    }
}