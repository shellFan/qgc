package com.qiongguichou.admin.controller;

import com.qiongguichou.admin.service.AdminSystemConfigService;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.core.entity.SystemConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台-系统配置Controller
 */
@RestController
@RequestMapping("/admin/api/system-config")
@RequiredArgsConstructor
public class AdminSystemConfigController {

    private final AdminSystemConfigService adminSystemConfigService;

    /**
     * 获取所有配置
     */
    @GetMapping
    public Result<List<SystemConfig>> list() {
        return Result.success(adminSystemConfigService.listAll());
    }

    /**
     * 按分组前缀获取配置
     */
    @GetMapping("/group/{group}")
    public Result<Map<String, String>> listByGroup(@PathVariable String group) {
        return Result.success(adminSystemConfigService.listByGroup(group));
    }

    /**
     * 更新配置值
     */
    @PutMapping("/{key}")
    public Result<Void> update(
            @PathVariable String key,
            @RequestBody Map<String, String> body,
            @RequestAttribute("adminId") Long adminId) {
        String value = body.get("value");
        adminSystemConfigService.updateConfig(key, value, adminId);
        return Result.success();
    }
}