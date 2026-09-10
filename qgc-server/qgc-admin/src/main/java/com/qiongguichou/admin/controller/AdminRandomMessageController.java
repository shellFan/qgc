package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminRandomMessageService;
import com.qiongguichou.campaign.entity.RandomMessage;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台-随机留言管理Controller
 */
@RestController
@RequestMapping("/admin/api/random-messages")
@RequiredArgsConstructor
public class AdminRandomMessageController {

    private final AdminRandomMessageService adminRandomMessageService;

    /**
     * 分页列表
     */
    @GetMapping
    public Result<IPage<RandomMessage>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer enabled) {
        Page<RandomMessage> p = new Page<>(page, size);
        return Result.success(adminRandomMessageService.listMessages(p, category, enabled));
    }

    /**
     * 创建
     */
    @PostMapping
    public Result<RandomMessage> create(@RequestBody RandomMessage message) {
        return Result.success(adminRandomMessageService.createMessage(message));
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RandomMessage message) {
        adminRandomMessageService.updateMessage(id, message);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminRandomMessageService.deleteMessage(id);
        return Result.success();
    }

    /**
     * 启用/禁用切换
     */
    @PutMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id) {
        adminRandomMessageService.toggleMessage(id);
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
        List<String> contents = (List<String>) body.get("contents");
        String category = (String) body.get("category");
        int count = adminRandomMessageService.batchImport(contents, category, adminId);
        return Result.success(count);
    }
}