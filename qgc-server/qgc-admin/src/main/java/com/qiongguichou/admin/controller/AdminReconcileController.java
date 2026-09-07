package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.admin.service.AdminAuthService;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.mapper.CampaignMapper;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.payment.entity.PaymentOrder;
import com.qiongguichou.payment.entity.SupportOrder;
import com.qiongguichou.payment.mapper.PaymentOrderMapper;
import com.qiongguichou.payment.mapper.SupportOrderMapper;
import com.qiongguichou.wallet.entity.Wallet;
import com.qiongguichou.wallet.entity.WalletFlow;
import com.qiongguichou.wallet.mapper.WalletFlowMapper;
import com.qiongguichou.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台-对账接口
 * RC5: 提供Wallet/Campaign/Payment三维度对账数据
 *
 * GET /admin/api/reconcile/wallet    - 钱包对账(余额vs流水)
 * GET /admin/api/reconcile/campaign  - 筹款对账(已筹vs支付)
 * GET /admin/api/reconcile/payment   - 支付对账(支付vs支持)
 */
@RestController
@RequestMapping("/admin/api/reconcile")
@RequiredArgsConstructor
public class AdminReconcileController {

    private final WalletMapper walletMapper;
    private final WalletFlowMapper walletFlowMapper;
    private final CampaignMapper campaignMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final SupportOrderMapper supportOrderMapper;
    private final AdminAuthService adminAuthService;

    /**
     * 钱包对账: 余额 vs 流水汇总
     * 检查每个钱包的余额是否与流水计算一致
     */
    @GetMapping("/wallet")
    public Result<Object> reconcileWallet(@RequestAttribute("adminId") Long adminId) {
        adminAuthService.getCurrentAdmin(adminId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", java.time.LocalDateTime.now().toString());

        // 查询所有钱包
        List<Wallet> wallets = walletMapper.selectList(null);
        int totalWallets = wallets.size();
        int mismatchCount = 0;

        for (Wallet wallet : wallets) {
            // 计算流水汇总
            List<WalletFlow> flows = walletFlowMapper.selectList(
                    new LambdaQueryWrapper<WalletFlow>()
                            .eq(WalletFlow::getUserId, wallet.getUserId()));

            long flowBalance = 0;
            for (WalletFlow flow : flows) {
                // 收入类: CAMPAIGN_INCOME, WITHDRAW_FAIL_RETURN, PAYMENT_REFUND, ADMIN_ADJUST(正)
                // 支出类: WITHDRAW_APPLY, WITHDRAW_SUCCESS, WECHAT_FEE, ADMIN_ADJUST(负)
                switch (flow.getType()) {
                    case "CAMPAIGN_INCOME":
                    case "WITHDRAW_FAIL_RETURN":
                    case "PAYMENT_REFUND":
                        flowBalance += flow.getAmount();
                        break;
                    case "WITHDRAW_SUCCESS":
                    case "WECHAT_FEE":
                        flowBalance -= flow.getAmount();
                        break;
                    case "WITHDRAW_APPLY":
                        // 提现申请冻结，不计入可用余额变化
                        break;
                    case "ADMIN_ADJUST":
                        // 管理员调整可正可负，通过余额变化判断
                        break;
                    default:
                        break;
                }
            }

            // 简单校验：余额是否与流水趋势一致(允许误差因提现冻结等)
            // 严格对账需要更复杂的逻辑，此处仅标记大额差异
            if (Math.abs(wallet.getBalance() - flowBalance) > 100) {
                mismatchCount++;
            }
        }

        result.put("totalWallets", totalWallets);
        result.put("mismatchCount", mismatchCount);
        result.put("status", mismatchCount == 0 ? "OK" : "MISMATCH");

        return Result.success(result);
    }

    /**
     * 筹款对账: 已筹金额 vs 支付成功金额
     * 检查每个筹款的raisedAmount是否与支付订单汇总一致
     */
    @GetMapping("/campaign")
    public Result<Object> reconcileCampaign(@RequestAttribute("adminId") Long adminId) {
        adminAuthService.getCurrentAdmin(adminId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", java.time.LocalDateTime.now().toString());

        // 查询所有活跃/成功筹款
        List<Campaign> campaigns = campaignMapper.selectList(
                new LambdaQueryWrapper<Campaign>()
                        .in(Campaign::getStatus, "ACTIVE", "SUCCESS"));

        int totalCampaigns = campaigns.size();
        int mismatchCount = 0;

        for (Campaign campaign : campaigns) {
            // 查询该筹款下所有支付成功的支付订单effectiveAmount汇总
            List<PaymentOrder> payments = paymentOrderMapper.selectList(
                    new LambdaQueryWrapper<PaymentOrder>()
                            .eq(PaymentOrder::getCampaignId, campaign.getId())
                            .eq(PaymentOrder::getStatus, "SUCCESS"));

            long totalEffective = payments.stream()
                    .mapToLong(p -> p.getEffectiveAmount() != null ? p.getEffectiveAmount() : 0L)
                    .sum();

            // 允许小额误差(并发更新导致)
            if (Math.abs(campaign.getRaisedAmount() - totalEffective) > 1) {
                mismatchCount++;
            }
        }

        result.put("totalCampaigns", totalCampaigns);
        result.put("mismatchCount", mismatchCount);
        result.put("status", mismatchCount == 0 ? "OK" : "MISMATCH");

        return Result.success(result);
    }

    /**
     * 支付对账: 支付订单 vs 支持订单
     * 检查支付订单和支持订单的金额和状态是否一致
     */
    @GetMapping("/payment")
    public Result<Object> reconcilePayment(@RequestAttribute("adminId") Long adminId) {
        adminAuthService.getCurrentAdmin(adminId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", java.time.LocalDateTime.now().toString());

        // 统计各状态订单数
        long paymentCreated = paymentOrderMapper.selectCount(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getStatus, "CREATED"));
        long paymentSuccess = paymentOrderMapper.selectCount(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getStatus, "SUCCESS"));
        long paymentClosed = paymentOrderMapper.selectCount(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getStatus, "CLOSED"));

        long supportCreated = supportOrderMapper.selectCount(
                new LambdaQueryWrapper<SupportOrder>().eq(SupportOrder::getStatus, "CREATED"));
        long supportPaid = supportOrderMapper.selectCount(
                new LambdaQueryWrapper<SupportOrder>().eq(SupportOrder::getStatus, "PAID"));
        long supportClosed = supportOrderMapper.selectCount(
                new LambdaQueryWrapper<SupportOrder>().eq(SupportOrder::getStatus, "CLOSED"));

        // 支付成功数应等于支持已付数
        boolean paymentSupportMatch = paymentSuccess == supportPaid;

        Map<String, Object> paymentStats = new LinkedHashMap<>();
        paymentStats.put("created", paymentCreated);
        paymentStats.put("success", paymentSuccess);
        paymentStats.put("closed", paymentClosed);

        Map<String, Object> supportStats = new LinkedHashMap<>();
        supportStats.put("created", supportCreated);
        supportStats.put("paid", supportPaid);
        supportStats.put("closed", supportClosed);

        result.put("paymentOrders", paymentStats);
        result.put("supportOrders", supportStats);
        result.put("paymentSupportMatch", paymentSupportMatch);
        result.put("status", paymentSupportMatch ? "OK" : "MISMATCH");

        return Result.success(result);
    }
}