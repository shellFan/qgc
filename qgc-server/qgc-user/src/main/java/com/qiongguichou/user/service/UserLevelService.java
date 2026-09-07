package com.qiongguichou.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.user.entity.UserBadge;
import com.qiongguichou.user.entity.UserLevel;
import com.qiongguichou.user.entity.UserPoints;
import com.qiongguichou.user.entity.PointsFlow;
import com.qiongguichou.user.mapper.UserLevelMapper;
import com.qiongguichou.user.mapper.UserBadgeMapper;
import com.qiongguichou.user.mapper.UserPointsMapper;
import com.qiongguichou.user.mapper.PointsFlowMapper;
import com.qiongguichou.core.mapper.SystemConfigMapper;
import com.qiongguichou.core.entity.SystemConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户等级/积分/徽章服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLevelService {

    private final UserLevelMapper userLevelMapper;
    private final UserBadgeMapper userBadgeMapper;
    private final UserPointsMapper userPointsMapper;
    private final PointsFlowMapper pointsFlowMapper;
    private final SystemConfigMapper systemConfigMapper;

    /** 等级经验阈值 */
    private static final int[] LEVEL_EXP = {0, 100, 500, 2000};
    private static final String[] LEVEL_NAME = {"穷鬼新人", "薯条伙伴", "快乐投喂官", "终极义父"};

    /**
     * 获取用户等级信息，不存在则初始化
     */
    public UserLevel getUserLevel(Long userId) {
        UserLevel level = userLevelMapper.selectOne(
                new LambdaQueryWrapper<UserLevel>().eq(UserLevel::getUserId, userId));
        if (level == null) {
            level = initUserLevel(userId);
        }
        return level;
    }

    /**
     * 增加经验值并自动升级
     */
    @Transactional(rollbackFor = Exception.class)
    public UserLevel addExp(Long userId, int exp) {
        UserLevel level = getUserLevel(userId);
        level.setExp(level.getExp() + exp);
        // 检查升级
        for (int i = LEVEL_EXP.length - 1; i >= 0; i--) {
            if (level.getExp() >= LEVEL_EXP[i] && level.getLevel() < i + 1) {
                level.setLevel(i + 1);
                level.setTitle(LEVEL_NAME[i]);
                break;
            }
        }
        userLevelMapper.updateById(level);
        return level;
    }

    /**
     * 获取用户徽章列表
     */
    public List<UserBadge> getUserBadges(Long userId) {
        return userBadgeMapper.selectList(
                new LambdaQueryWrapper<UserBadge>().eq(UserBadge::getUserId, userId)
                        .orderByDesc(UserBadge::getEarnedTime));
    }

    /**
     * 授予徽章(幂等)
     */
    public boolean awardBadge(Long userId, String badgeCode, String badgeName, String badgeIcon) {
        Long count = userBadgeMapper.selectCount(
                new LambdaQueryWrapper<UserBadge>()
                        .eq(UserBadge::getUserId, userId)
                        .eq(UserBadge::getBadgeCode, badgeCode));
        if (count > 0) {
            return false; // 已有该徽章
        }
        UserBadge badge = new UserBadge();
        badge.setUserId(userId);
        badge.setBadgeCode(badgeCode);
        badge.setBadgeName(badgeName);
        badge.setBadgeIcon(badgeIcon);
        badge.setEarnedTime(LocalDateTime.now());
        userBadgeMapper.insert(badge);
        return true;
    }

    /**
     * 获取用户积分信息，不存在则初始化
     */
    public UserPoints getUserPoints(Long userId) {
        UserPoints points = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId));
        if (points == null) {
            points = initUserPoints(userId);
        }
        return points;
    }

    /**
     * 增加积分
     */
    @Transactional(rollbackFor = Exception.class)
    public UserPoints addPoints(Long userId, int amount, String type, String relatedId, String remark) {
        UserPoints points = getUserPoints(userId);
        points.setPoints(points.getPoints() + amount);
        points.setTotalEarned(points.getTotalEarned() + amount);
        userPointsMapper.updateById(points);

        // 记录流水
        PointsFlow flow = new PointsFlow();
        flow.setUserId(userId);
        flow.setType(type);
        flow.setAmount(amount);
        flow.setBalanceAfter(points.getPoints());
        flow.setRelatedId(relatedId);
        flow.setRemark(remark);
        pointsFlowMapper.insert(flow);
        return points;
    }

    /**
     * 消费积分
     */
    @Transactional(rollbackFor = Exception.class)
    public UserPoints spendPoints(Long userId, int amount, String type, String relatedId, String remark) {
        UserPoints points = getUserPoints(userId);
        if (points.getPoints() < amount) {
            throw new RuntimeException("积分不足");
        }
        points.setPoints(points.getPoints() - amount);
        points.setTotalSpent(points.getTotalSpent() + amount);
        userPointsMapper.updateById(points);

        PointsFlow flow = new PointsFlow();
        flow.setUserId(userId);
        flow.setType(type);
        flow.setAmount(-amount);
        flow.setBalanceAfter(points.getPoints());
        flow.setRelatedId(relatedId);
        flow.setRemark(remark);
        pointsFlowMapper.insert(flow);
        return points;
    }

    /**
     * 检查并授予投喂徽章
     */
    public void checkSupportBadges(Long userId, int totalSupportCount) {
        if (totalSupportCount >= 1) {
            awardBadge(userId, "first_support", "初次投喂", "🤝");
        }
        if (totalSupportCount >= 10) {
            awardBadge(userId, "help_10_people", "十人义父", "⭐");
        }
    }

    /**
     * 检查并授予大额投喂徽章(单次>=50元)
     */
    public void checkBigSupporterBadge(Long userId, long amountFen) {
        if (amountFen >= 5000) {
            awardBadge(userId, "big_supporter", "豪气义父", "💎");
        }
    }

    /**
     * 检查并授予发起筹款徽章
     */
    public void checkCreateBadge(Long userId) {
        awardBadge(userId, "first_create", "初次发起", "🎉");
    }

    private UserLevel initUserLevel(Long userId) {
        UserLevel level = new UserLevel();
        level.setUserId(userId);
        level.setLevel(1);
        level.setExp(0);
        level.setTitle("穷鬼新人");
        userLevelMapper.insert(level);
        return level;
    }

    private UserPoints initUserPoints(Long userId) {
        UserPoints points = new UserPoints();
        points.setUserId(userId);
        points.setPoints(0);
        points.setTotalEarned(0);
        points.setTotalSpent(0);
        userPointsMapper.insert(points);
        return points;
    }
}