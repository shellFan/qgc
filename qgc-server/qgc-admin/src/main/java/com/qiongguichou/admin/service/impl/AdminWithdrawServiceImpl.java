package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.Admin;
import com.qiongguichou.admin.service.AdminWithdrawService;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.util.OrderNoUtil;
import com.qiongguichou.wallet.entity.Wallet;
import com.qiongguichou.wallet.entity.WalletFlow;
import com.qiongguichou.wallet.entity.WithdrawOrder;
import com.qiongguichou.wallet.mapper.WalletFlowMapper;
import com.qiongguichou.wallet.mapper.WalletMapper;
import com.qiongguichou.wallet.mapper.WithdrawOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 后台-提现审核服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminWithdrawServiceImpl implements AdminWithdrawService {

    private final WithdrawOrderMapper withdrawOrderMapper;
    private final WalletMapper walletMapper;
    private final WalletFlowMapper walletFlowMapper;
    private final OrderNoUtil orderNoUtil;

    @Override
    public IPage<WithdrawOrder> listWithdrawOrders(Page<WithdrawOrder> page, String status) {
        LambdaQueryWrapper<WithdrawOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(WithdrawOrder::getStatus, status);
        }
        wrapper.orderByDesc(WithdrawOrder::getCreateTime);
        return withdrawOrderMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveWithdraw(Long withdrawOrderId, Admin admin) {
        WithdrawOrder order = getWithdrawOrder(withdrawOrderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有待审核的提现可以审核通过");
        }
        // 审核通过 → 进入处理中状态
        order.setStatus("PROCESSING");
        order.setReviewerId(admin.getId());
        order.setReviewTime(LocalDateTime.now());
        withdrawOrderMapper.updateById(order);
        log.info("提现审核通过: orderId={}, adminId={}", withdrawOrderId, admin.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectWithdraw(Long withdrawOrderId, String reason, Admin admin) {
        WithdrawOrder order = getWithdrawOrder(withdrawOrderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有待审核的提现可以拒绝");
        }
        // 退回余额
        returnBalance(order, reason);

        order.setStatus("REJECTED");
        order.setReviewerId(admin.getId());
        order.setReviewTime(LocalDateTime.now());
        order.setRejectReason(reason);
        withdrawOrderMapper.updateById(order);
        log.info("提现审核拒绝: orderId={}, adminId={}, reason={}", withdrawOrderId, admin.getId(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPaid(Long withdrawOrderId, String transferNo, Admin admin) {
        WithdrawOrder order = getWithdrawOrder(withdrawOrderId);
        if (!"PROCESSING".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有处理中的提现可以标记打款");
        }
        order.setStatus("SUCCESS");
        order.setTransferNo(transferNo);
        order.setTransferTime(LocalDateTime.now());
        withdrawOrderMapper.updateById(order);
        log.info("提现打款成功: orderId={}, transferNo={}", withdrawOrderId, transferNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPayFail(Long withdrawOrderId, String reason, Admin admin) {
        WithdrawOrder order = getWithdrawOrder(withdrawOrderId);
        if (!"PROCESSING".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有处理中的提现可以标记打款失败");
        }
        // 退回余额
        returnBalance(order, reason);

        order.setStatus("FAIL");
        order.setRejectReason(reason);
        withdrawOrderMapper.updateById(order);
        log.info("提现打款失败: orderId={}, reason={}", withdrawOrderId, reason);
    }

    private WithdrawOrder getWithdrawOrder(Long withdrawOrderId) {
        WithdrawOrder order = withdrawOrderMapper.selectById(withdrawOrderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "提现订单不存在");
        }
        return order;
    }

    /**
     * 退回余额（审核拒绝或打款失败时）
     * 退回金额 = 提现金额(amount) = 实际到账(actualAmount) + 手续费(fee)
     */
    private void returnBalance(WithdrawOrder order, String reason) {
        Long returnAmount = order.getAmount();
        Wallet wallet = walletMapper.selectOne(
                new LambdaQueryWrapper<Wallet>().eq(Wallet::getUserId, order.getUserId())
        );
        if (wallet != null) {
            // 乐观锁更新余额（delta为正数，增加余额）
            int rows = walletMapper.updateBalance(order.getUserId(), returnAmount, wallet.getVersion());
            if (rows > 0) {
                // 重新查询最新余额
                Wallet updatedWallet = walletMapper.selectOne(
                        new LambdaQueryWrapper<Wallet>().eq(Wallet::getUserId, order.getUserId())
                );
                // 记录流水
                WalletFlow flow = new WalletFlow();
                flow.setFlowNo(OrderNoUtil.flowNo());
                flow.setUserId(order.getUserId());
                flow.setType("WITHDRAW_FAIL_RETURN");
                flow.setAmount(returnAmount);
                flow.setBalanceAfter(updatedWallet != null ? updatedWallet.getBalance() : wallet.getBalance() + returnAmount);
                flow.setRelatedId(String.valueOf(order.getId()));
                flow.setRelatedType("WITHDRAW_ORDER");
                flow.setRemark(reason);
                walletFlowMapper.insert(flow);
            }
        }
    }
}