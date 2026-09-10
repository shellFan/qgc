package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.campaign.entity.RandomMessage;

import java.util.List;

/**
 * 后台-随机留言管理Service
 */
public interface AdminRandomMessageService {

    /**
     * 分页查询
     */
    IPage<RandomMessage> listMessages(Page<RandomMessage> page, String category, Integer enabled);

    /**
     * 创建
     */
    RandomMessage createMessage(RandomMessage message);

    /**
     * 更新
     */
    void updateMessage(Long id, RandomMessage message);

    /**
     * 删除
     */
    void deleteMessage(Long id);

    /**
     * 启用/禁用切换
     */
    void toggleMessage(Long id);

    /**
     * 批量导入
     */
    int batchImport(List<String> contents, String category, Long adminId);
}