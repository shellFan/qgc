package com.qiongguichou.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.user.entity.UserBadge;
import com.qiongguichou.user.entity.UserLevel;
import com.qiongguichou.user.entity.UserPoints;
import com.qiongguichou.user.entity.PointsFlow;
import com.qiongguichou.user.service.UserLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户中心 - 等级/徽章/积分/个人主页
 */
@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserLevelService userLevelService;

    /**
     * 获取我的等级信息
     */
    @GetMapping("/level")
    public Result<UserLevel> getMyLevel() {
        Long userId = UserContext.getUserId();
        return Result.success(userLevelService.getUserLevel(userId));
    }

    /**
     * 获取我的徽章列表
     */
    @GetMapping("/badges")
    public Result<List<UserBadge>> getMyBadges() {
        Long userId = UserContext.getUserId();
        return Result.success(userLevelService.getUserBadges(userId));
    }

    /**
     * 获取我的积分信息
     */
    @GetMapping("/points")
    public Result<UserPoints> getMyPoints() {
        Long userId = UserContext.getUserId();
        return Result.success(userLevelService.getUserPoints(userId));
    }

    /**
     * 获取我的积分流水
     */
    @GetMapping("/points/flow")
    public Result<List<PointsFlow>> getMyPointsFlow() {
        Long userId = UserContext.getUserId();
        return Result.success(userLevelService.getUserPointsFlow(userId));
    }

    /**
     * 获取用户个人主页
     */
    @GetMapping("/{userId}")
    public Result<Map<String, Object>> getUserProfile(@PathVariable Long userId) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("level", userLevelService.getUserLevel(userId));
        profile.put("badges", userLevelService.getUserBadges(userId));
        profile.put("points", userLevelService.getUserPoints(userId));
        return Result.success(profile);
    }

    /**
     * 获取我的完整信息(等级+徽章+积分)
     */
    @GetMapping("/mine")
    public Result<Map<String, Object>> getMyFullProfile() {
        Long userId = UserContext.getUserId();
        Map<String, Object> profile = new HashMap<>();
        profile.put("level", userLevelService.getUserLevel(userId));
        profile.put("badges", userLevelService.getUserBadges(userId));
        profile.put("points", userLevelService.getUserPoints(userId));
        return Result.success(profile);
    }
}