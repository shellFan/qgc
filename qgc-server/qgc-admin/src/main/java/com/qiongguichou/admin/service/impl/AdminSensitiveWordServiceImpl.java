package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.mapper.AdminLogMapper;
import com.qiongguichou.admin.service.AdminSensitiveWordService;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.content.entity.SensitiveWord;
import com.qiongguichou.content.mapper.SensitiveWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台-敏感词管理Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminSensitiveWordServiceImpl implements AdminSensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public IPage<SensitiveWord> listWords(Page<SensitiveWord> page, String category, Integer enabled) {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(SensitiveWord::getCategory, category);
        }
        if (enabled != null) {
            wrapper.eq(SensitiveWord::getEnabled, enabled);
        }
        wrapper.orderByDesc(SensitiveWord::getCreateTime);
        return sensitiveWordMapper.selectPage(page, wrapper);
    }

    @Override
    public SensitiveWord createWord(SensitiveWord word) {
        word.setEnabled(word.getEnabled() != null ? word.getEnabled() : 1);
        sensitiveWordMapper.insert(word);
        return word;
    }

    @Override
    public void updateWord(Long id, SensitiveWord word) {
        getWord(id);
        word.setId(id);
        sensitiveWordMapper.updateById(word);
    }

    @Override
    public void deleteWord(Long id) {
        sensitiveWordMapper.deleteById(id);
    }

    @Override
    public void toggleWord(Long id) {
        SensitiveWord word = getWord(id);
        word.setEnabled(word.getEnabled() == 1 ? 0 : 1);
        sensitiveWordMapper.updateById(word);
    }

    @Override
    public int batchImport(List<String> words, String category, Long adminId) {
        int count = 0;
        for (String w : words) {
            if (w == null || w.trim().isEmpty()) {
                continue;
            }
            // 检查是否已存在
            Long exists = sensitiveWordMapper.selectCount(
                    new LambdaQueryWrapper<SensitiveWord>()
                            .eq(SensitiveWord::getWord, w.trim())
                            .eq(SensitiveWord::getCategory, category));
            if (exists > 0) {
                continue;
            }
            SensitiveWord word = new SensitiveWord();
            word.setWord(w.trim());
            word.setCategory(category);
            word.setEnabled(1);
            sensitiveWordMapper.insert(word);
            count++;
        }

        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("BATCH_IMPORT");
        log.setTargetType("SENSITIVE_WORD");
        log.setAfterData("count=" + count + ",category=" + category);
        adminLogMapper.insert(log);

        return count;
    }

    private SensitiveWord getWord(Long id) {
        SensitiveWord word = sensitiveWordMapper.selectById(id);
        if (word == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "敏感词不存在");
        }
        return word;
    }
}