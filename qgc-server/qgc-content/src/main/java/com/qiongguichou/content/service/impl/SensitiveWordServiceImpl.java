package com.qiongguichou.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.content.entity.SensitiveWord;
import com.qiongguichou.content.mapper.SensitiveWordMapper;
import com.qiongguichou.content.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感词服务实现 - 本地词库DFA算法
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;

    /** DFA敏感词树 */
    private Map<String, Object> sensitiveWordMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return findSensitiveWords(text).size() > 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> findSensitiveWords(String text) {
        Set<String> result = new HashSet<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                result.add(text.substring(i, i + length));
                i = i + length - 1;
            }
        }
        return result;
    }

    @Override
    public void refreshCache() {
        List<SensitiveWord> words = sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getEnabled, 1));
        Map<String, Object> newMap = new HashMap<>(words.size());
        for (SensitiveWord sw : words) {
            addWordToMap(newMap, sw.getWord());
        }
        this.sensitiveWordMap = newMap;
        log.info("敏感词缓存刷新完成，共{}个敏感词", words.size());
    }

    /**
     * 将敏感词加入DFA树
     */
    @SuppressWarnings("unchecked")
    private void addWordToMap(Map<String, Object> map, String word) {
        Map<String, Object> current = map;
        for (int i = 0; i < word.length(); i++) {
            String c = String.valueOf(word.charAt(i));
            Object obj = current.get(c);
            if (obj == null) {
                Map<String, Object> next = new HashMap<>();
                current.put(c, next);
                current = next;
            } else {
                current = (Map<String, Object>) obj;
            }
        }
        current.put("isEnd", "1");
    }

    /**
     * DFA算法检查敏感词
     */
    @SuppressWarnings("unchecked")
    private int checkSensitiveWord(String text, int beginIndex) {
        int length = 0;
        Map<String, Object> current = sensitiveWordMap;
        for (int i = beginIndex; i < text.length(); i++) {
            String c = String.valueOf(text.charAt(i));
            Object obj = current.get(c);
            if (obj == null) {
                break;
            }
            length++;
            current = (Map<String, Object>) obj;
            if ("1".equals(current.get("isEnd"))) {
                return length;
            }
        }
        // 未完整匹配
        if (length > 0 && !"1".equals(current.get("isEnd"))) {
            return 0;
        }
        return length;
    }
}