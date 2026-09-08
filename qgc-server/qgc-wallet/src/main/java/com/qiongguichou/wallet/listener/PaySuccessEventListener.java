package com.qiongguichou.wallet.listener;

import com.qiongguichou.common.event.PaySuccessEvent;
import com.qiongguichou.core.config.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 支付成功事件监听器
 * 在支付回调事务提交后异步处理非关键逻辑：
 * 1. 缓存清理
 *
 * 注意：钱包入账已在PaymentServiceImpl.processPaySuccess事务内完成，
 * 确保payment/campaign/wallet数据一致性。
 * 此监听器仅处理可重试/可丢失的非关键逻辑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaySuccessEventListener {

    private final RedisService redisService;

    /**
     * 支付成功后：清理缓存
     * AFTER_COMMIT确保支付事务已提交
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaySuccess(PaySuccessEvent event) {
        log.info("处理支付成功事件(缓存清理): campaignId={}, creatorUserId={}, amount={}",
                event.getCampaignId(), event.getCreatorUserId(), event.getEffectiveAmount());

        // 清理筹款相关缓存
        try {
            clearCampaignCache(event.getCampaignId());
        } catch (Exception e) {
            log.warn("清理筹款缓存失败, campaignId={}", event.getCampaignId(), e);
        }
    }

    /**
     * 清理筹款相关缓存
     */
    private void clearCampaignCache(Long campaignId) {
        // 清理详情缓存
        redisService.delete("qgc:cache:campaign:detail:" + campaignId);
        // 清理列表缓存(版本化递增, 无需SCAN)
        redisService.increment("qgc:cache:campaign:list:version");
        // 清理热门缓存(版本化递增, 无需SCAN)
        redisService.increment("qgc:cache:campaign:hot:version");
    }
}