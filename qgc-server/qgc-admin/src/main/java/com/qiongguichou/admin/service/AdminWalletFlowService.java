package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.wallet.entity.WalletFlow;

/**
 * 后台-钱包流水Service
 */
public interface AdminWalletFlowService {

    /**
     * 分页查询钱包流水
     */
    IPage<WalletFlow> listFlows(Page<WalletFlow> page, Long userId, String type);
}