package com.qiongguichou.wallet.service;

import com.qiongguichou.wallet.dto.WalletVO;
import com.qiongguichou.wallet.dto.WithdrawRequest;
import com.qiongguichou.wallet.entity.WalletFlow;
import com.qiongguichou.wallet.entity.WithdrawOrder;

import java.util.List;

/**
 * 钱包服务接口
 */
public interface WalletService {

    /**
     * 获取钱包信息
     */
    WalletVO getWallet(Long userId);

    /**
     * 筹款收入入账
     */
    void campaignIncome(Long userId, Long amount, Long campaignId, String campaignTitle);

    /**
     * 申请提现
     */
    WithdrawOrder applyWithdraw(Long userId, WithdrawRequest request);

    /**
     * 获取提现记录
     */
    List<WithdrawOrder> getWithdrawList(Long userId);

    /**
     * 获取流水记录
     */
    List<WalletFlow> getFlowList(Long userId);

    /**
     * 确保用户钱包存在(不存在则创建)
     */
    void ensureWallet(Long userId);
}