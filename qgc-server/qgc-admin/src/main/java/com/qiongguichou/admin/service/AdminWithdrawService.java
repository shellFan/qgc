package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.Admin;
import com.qiongguichou.wallet.entity.WithdrawOrder;

/**
 * 后台-提现审核服务
 */
public interface AdminWithdrawService {

    /**
     * 分页查询提现订单列表
     */
    IPage<WithdrawOrder> listWithdrawOrders(Page<WithdrawOrder> page, String status);

    /**
     * 审核通过（打款）
     */
    void approveWithdraw(Long withdrawOrderId, Admin admin);

    /**
     * 审核拒绝（退回余额）
     */
    void rejectWithdraw(Long withdrawOrderId, String reason, Admin admin);

    /**
     * 标记打款成功
     */
    void markPaid(Long withdrawOrderId, String transferNo, Admin admin);

    /**
     * 标记打款失败（退回余额）
     */
    void markPayFail(Long withdrawOrderId, String reason, Admin admin);
}