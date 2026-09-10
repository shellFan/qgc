package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminWalletFlowService;
import com.qiongguichou.wallet.entity.WalletFlow;
import com.qiongguichou.wallet.mapper.WalletFlowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 后台-钱包流水Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminWalletFlowServiceImpl implements AdminWalletFlowService {

    private final WalletFlowMapper walletFlowMapper;

    @Override
    public IPage<WalletFlow> listFlows(Page<WalletFlow> page, Long userId, String type) {
        LambdaQueryWrapper<WalletFlow> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(WalletFlow::getUserId, userId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(WalletFlow::getType, type);
        }
        wrapper.orderByDesc(WalletFlow::getCreateTime);
        return walletFlowMapper.selectPage(page, wrapper);
    }
}