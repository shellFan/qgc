package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.mapper.AdminLogMapper;
import com.qiongguichou.admin.service.AdminRandomMessageService;
import com.qiongguichou.campaign.entity.RandomMessage;
import com.qiongguichou.campaign.mapper.RandomMessageMapper;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台-随机留言管理Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminRandomMessageServiceImpl implements AdminRandomMessageService {

    private final RandomMessageMapper randomMessageMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public IPage<RandomMessage> listMessages(Page<RandomMessage> page, String category, Integer enabled) {
        LambdaQueryWrapper<RandomMessage> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(RandomMessage::getCategory, category);
        }
        if (enabled != null) {
            wrapper.eq(RandomMessage::getEnabled, enabled);
        }
        wrapper.orderByDesc(RandomMessage::getCreateTime);
        return randomMessageMapper.selectPage(page, wrapper);
    }

    @Override
    public RandomMessage createMessage(RandomMessage message) {
        message.setEnabled(message.getEnabled() != null ? message.getEnabled() : 1);
        randomMessageMapper.insert(message);
        return message;
    }

    @Override
    public void updateMessage(Long id, RandomMessage message) {
        getMessage(id);
        message.setId(id);
        randomMessageMapper.updateById(message);
    }

    @Override
    public void deleteMessage(Long id) {
        randomMessageMapper.deleteById(id);
    }

    @Override
    public void toggleMessage(Long id) {
        RandomMessage message = getMessage(id);
        message.setEnabled(message.getEnabled() == 1 ? 0 : 1);
        randomMessageMapper.updateById(message);
    }

    @Override
    public int batchImport(List<String> contents, String category, Long adminId) {
        int count = 0;
        for (String content : contents) {
            if (content == null || content.trim().isEmpty()) {
                continue;
            }
            RandomMessage message = new RandomMessage();
            message.setContent(content.trim());
            message.setCategory(category);
            message.setEnabled(1);
            randomMessageMapper.insert(message);
            count++;
        }

        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("BATCH_IMPORT");
        log.setTargetType("RANDOM_MESSAGE");
        log.setAfterData("count=" + count + ",category=" + category);
        adminLogMapper.insert(log);

        return count;
    }

    private RandomMessage getMessage(Long id) {
        RandomMessage message = randomMessageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "随机留言不存在");
        }
        return message;
    }
}