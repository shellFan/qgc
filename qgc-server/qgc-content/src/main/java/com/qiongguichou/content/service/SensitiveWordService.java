package com.qiongguichou.content.service;

import java.util.Set;

/**
 * 敏感词服务
 */
public interface SensitiveWordService {

    /**
     * 检测文本是否包含敏感词
     */
    boolean containsSensitiveWord(String text);

    /**
     * 获取文本中的敏感词列表
     */
    Set<String> findSensitiveWords(String text);

    /**
     * 刷新敏感词缓存
     */
    void refreshCache();
}