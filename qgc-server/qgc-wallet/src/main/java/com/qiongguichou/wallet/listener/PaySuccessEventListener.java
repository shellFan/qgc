package com.qiongguichou.wallet.listener;

import com.qiongguichou.common.event.PaySuccessEvent;
import com.qiongguichou.core.config.RedisService;
import com.qiongguichou.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 支付成功事件监听器
 * 在支付回调事务提交后异步处理：
 * 1. 钱包入账（独立事务）
 * 2. 缓存清理
 *
 * RC5: 使用@TransactionalEventListener(AFTER_COMMIT)确保事务提交后才处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaySuccessEventListener {

    private final WalletService walletService;
    private final RedisService redisService;

    /**
     * 支付成功后：钱包入账
     * AFTER_COMMIT确保支付事务已提交，避免脏读
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaySuccess(PaySuccessEvent event) {
        log.info("处理支付成功事件: campaignId={}, creatorUserId={}, amount={}",
                event.getCampaignId(), event.getCreatorUserId(), event.getEffectiveAmount());

        try {
            // 1. 钱包入账（WalletService.campaignIncome有独立事务+分布式锁）
            walletService.campaignIncome(
                    event.getCreatorUserId(),
                    event.getEffectiveAmount(),
                    event.getCampaignId(),
                    event.getCampaignTitle()
            );
            log.info("钱包入账成功: userId={}, amount={}", event.getCreatorUserId(), event.getEffectiveAmount());
        } catch (Exception e) {
            // 钱包入账失败不影响支付结果，记录日志后续人工处理
            log.error("钱包入账失败，需人工处理: campaignId={}, creatorUserId={}, amount={}",
                    event.getCampaignId(), event.getCreatorUserId(), event.getEffectiveAmount(), e);
        }

        // 2. 清理筹款相关缓存
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