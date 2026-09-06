package com.qiongguichou.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.common.enums.WalletFlowType;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.util.MoneyUtil;
import com.qiongguichou.common.util.OrderNoUtil;
import com.qiongguichou.wallet.dto.WalletVO;
import com.qiongguichou.wallet.dto.WithdrawRequest;
import com.qiongguichou.wallet.entity.Wallet;
import com.qiongguichou.wallet.entity.WalletFlow;
import com.qiongguichou.wallet.entity.WithdrawOrder;
import com.qiongguichou.wallet.mapper.WalletFlowMapper;
import com.qiongguichou.wallet.mapper.WalletMapper;
import com.qiongguichou.wallet.mapper.WithdrawOrderMapper;
import com.qiongguichou.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 钱包服务实现
 * 余额操作使用乐观锁+分布式锁双重保障
 * 流水表只插入不更新不删除，不可篡改
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;
    private final WalletFlowMapper walletFlowMapper;
    private final WithdrawOrderMapper withdrawOrderMapper;
    private final RedissonClient redissonClient;

    /** 提现手续费率(0.6%) */
    private static final int WITHDRAW_FEE_RATE = 6;

    @Override
    public WalletVO getWallet(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        WalletVO vo = new WalletVO();
        vo.setId(wallet.getId());
        vo.setBalance(wallet.getBalance());
        vo.setBalanceYuan(MoneyUtil.fenToYuanStr(wallet.getBalance()));
        vo.setFrozenAmount(wallet.getFrozenAmount());
        vo.setFrozenAmountYuan(MoneyUtil.fenToYuanStr(wallet.getFrozenAmount()));
        vo.setTotalIncome(wallet.getTotalIncome());
        vo.setTotalIncomeYuan(MoneyUtil.fenToYuanStr(wallet.getTotalIncome()));
        vo.setTotalWithdraw(wallet.getTotalWithdraw());
        vo.setTotalWithdrawYuan(MoneyUtil.fenToYuanStr(wallet.getTotalWithdraw()));
        vo.setTotalSupport(wallet.getTotalSupport());
        vo.setTotalSupportYuan(MoneyUtil.fenToYuanStr(wallet.getTotalSupport()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void campaignIncome(Long userId, Long amount, Long campaignId, String campaignTitle) {
        String lockKey = "qgc:wallet:income:" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY);
            }

            Wallet wallet = getOrCreateWallet(userId);

            // 乐观锁更新余额
            int rows = walletMapper.updateBalance(userId, amount, wallet.getVersion());
            if (rows == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY, "余额更新冲突，请重试");
            }

            // 重新查询最新余额用于流水记录
            Wallet updatedWallet = walletMapper.selectOne(
                    new LambdaQueryWrapper<Wallet>()
                            .eq(Wallet::getUserId, userId)
                            .last("LIMIT 1"));

            // 记录流水
            WalletFlow flow = new WalletFlow();
            flow.setFlowNo(OrderNoUtil.flowNo());
            flow.setUserId(userId);
            flow.setType(WalletFlowType.CAMPAIGN_INCOME.name());
            flow.setAmount(amount);
            flow.setBalanceAfter(updatedWallet != null ? updatedWallet.getBalance() : wallet.getBalance() + amount);
            flow.setRelatedId(String.valueOf(campaignId));
            flow.setRelatedType("CAMPAIGN");
            flow.setRemark("筹款收入: " + campaignTitle);
            walletFlowMapper.insert(flow);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WithdrawOrder applyWithdraw(Long userId, WithdrawRequest request) {
        String lockKey = "qgc:wallet:withdraw:" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY);
            }

            Wallet wallet = getOrCreateWallet(userId);

            // 校验余额
            if (wallet.getBalance() < request.getAmount()) {
                throw new BusinessException(ErrorCode.WALLET_INSUFFICIENT);
            }

            // 计算手续费
            Long fee = Math.max(1L, request.getAmount() * WITHDRAW_FEE_RATE / 1000);
            Long actualAmount = request.getAmount() - fee;

            // 扣减余额(乐观锁)
            int rows = walletMapper.updateBalance(userId, -request.getAmount(), wallet.getVersion());
            if (rows == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY, "余额更新冲突，请重试");
            }

            // 创建提现订单
            WithdrawOrder order = new WithdrawOrder();
            order.setWithdrawNo(OrderNoUtil.withdrawNo());
            order.setUserId(userId);
            order.setAmount(request.getAmount());
            order.setActualAmount(actualAmount);
            order.setFee(fee);
            order.setStatus("PENDING");
            withdrawOrderMapper.insert(order);

            // 重新查询最新余额用于流水记录
            Wallet updatedWallet = walletMapper.selectOne(
                    new LambdaQueryWrapper<Wallet>()
                            .eq(Wallet::getUserId, userId)
                            .last("LIMIT 1"));

            // 记录流水
            WalletFlow flow = new WalletFlow();
            flow.setFlowNo(OrderNoUtil.flowNo());
            flow.setUserId(userId);
            flow.setType(WalletFlowType.WITHDRAW_APPLY.name());
            flow.setAmount(-request.getAmount());
            flow.setBalanceAfter(updatedWallet != null ? updatedWallet.getBalance() : wallet.getBalance() - request.getAmount());
            flow.setRelatedId(String.valueOf(order.getId()));
            flow.setRelatedType("WITHDRAW");
            flow.setRemark("提现申请: " + MoneyUtil.fenToYuanStr(request.getAmount()) + "元");
            walletFlowMapper.insert(flow);

            return order;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<WithdrawOrder> getWithdrawList(Long userId) {
        return withdrawOrderMapper.selectList(
                new LambdaQueryWrapper<WithdrawOrder>()
                        .eq(WithdrawOrder::getUserId, userId)
                        .orderByDesc(WithdrawOrder::getCreateTime));
    }

    @Override
    public List<WalletFlow> getFlowList(Long userId) {
        return walletFlowMapper.selectList(
                new LambdaQueryWrapper<WalletFlow>()
                        .eq(WalletFlow::getUserId, userId)
                        .orderByDesc(WalletFlow::getCreateTime));
    }

    @Override
    public void ensureWallet(Long userId) {
        getOrCreateWallet(userId);
    }

    /**
     * 获取或创建钱包
     */
    private Wallet getOrCreateWallet(Long userId) {
        Wallet wallet = walletMapper.selectOne(
                new LambdaQueryWrapper<Wallet>()
                        .eq(Wallet::getUserId, userId)
                        .last("LIMIT 1"));
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(0L);
            wallet.setFrozenAmount(0L);
            wallet.setTotalIncome(0L);
            wallet.setTotalWithdraw(0L);
            wallet.setTotalSupport(0L);
            wallet.setVersion(0);
            walletMapper.insert(wallet);
        }
        return wallet;
    }
}