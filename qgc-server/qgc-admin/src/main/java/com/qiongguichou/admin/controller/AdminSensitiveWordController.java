package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminSensitiveWordService;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.content.entity.SensitiveWord;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台-敏感词管理Controller
 */
@RestController
@RequestMapping("/admin/api/sensitive-words")
@RequiredArgsConstructor
public class AdminSensitiveWordController {

    private final AdminSensitiveWordService adminSensitiveWordService;

    /**
     * 分页列表
     */
    @GetMapping
    public Result<IPage<SensitiveWord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer enabled) {
        Page<SensitiveWord> p = new Page<>(page, size);
        return Result.success(adminSensitiveWordService.listWords(p, category, enabled));
    }

    /**
     * 创建
     */
    @PostMapping
    public Result<SensitiveWord> create(@RequestBody SensitiveWord word) {
        return Result.success(adminSensitiveWordService.createWord(word));
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SensitiveWord word) {
        adminSensitiveWordService.updateWord(id, word);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminSensitiveWordService.deleteWord(id);
        return Result.success();
    }

    /**
     * 启用/禁用切换
     */
    @PutMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id) {
        adminSensitiveWordService.toggleWord(id);
        return Result.success();
    }

    /**
     * 批量导入
     */
    @PostMapping("/batch")
    public Result<Integer> batchImport(
            @RequestBody Map<String, Object> body,
            @RequestAttribute("adminId") Long adminId) {
        @SuppressWarnings("unchecked")
        List<String> words = (List<String>) body.get("words");
        String category = (String) body.get("category");
        int count = adminSensitiveWordService.batchImport(words, category, adminId);
        return Result.success(count);
    }
}