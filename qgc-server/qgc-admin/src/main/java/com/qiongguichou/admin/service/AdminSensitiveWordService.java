package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.content.entity.SensitiveWord;

import java.util.List;

/**
 * 后台-敏感词管理Service
 */
public interface AdminSensitiveWordService {

    /**
     * 分页查询
     */
    IPage<SensitiveWord> listWords(Page<SensitiveWord> page, String category, Integer enabled);

    /**
     * 创建
     */
    SensitiveWord createWord(SensitiveWord word);

    /**
     * 更新
     */
    void updateWord(Long id, SensitiveWord word);

    /**
     * 删除
     */
    void deleteWord(Long id);

    /**
     * 启用/禁用切换
     */
    void toggleWord(Long id);

    /**
     * 批量导入
     */
    int batchImport(List<String> words, String category, Long adminId);
}