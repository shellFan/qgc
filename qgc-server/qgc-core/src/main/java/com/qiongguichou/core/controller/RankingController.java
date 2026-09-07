package com.qiongguichou.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.core.entity.RankingSnapshot;
import com.qiongguichou.core.mapper.RankingSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 排行榜Controller
 */
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingSnapshotMapper rankingSnapshotMapper;

    /**
     * 获取排行榜
     * @param type 排行类型: SADDEST_DAY/MOST_SUPPORTERS/FASTEST_SUCCESS
     */
    @GetMapping("/{type}")
    public Result<List<RankingSnapshot>> getRanking(
            @PathVariable String type,
            @RequestParam(defaultValue = "20") int limit) {
        LocalDate today = LocalDate.now();
        List<RankingSnapshot> rankings = rankingSnapshotMapper.selectList(
                new LambdaQueryWrapper<RankingSnapshot>()
                        .eq(RankingSnapshot::getRankingType, type)
                        .eq(RankingSnapshot::getRankingDate, today)
                        .orderByAsc(RankingSnapshot::getRankNo)
                        .last("LIMIT " + limit));
        return Result.success(rankings);
    }

    /**
     * 获取历史排行榜
     */
    @GetMapping("/{type}/history")
    public Result<List<RankingSnapshot>> getHistoryRanking(
            @PathVariable String type,
            @RequestParam String date,
            @RequestParam(defaultValue = "20") int limit) {
        LocalDate rankingDate = LocalDate.parse(date);
        List<RankingSnapshot> rankings = rankingSnapshotMapper.selectList(
                new LambdaQueryWrapper<RankingSnapshot>()
                        .eq(RankingSnapshot::getRankingType, type)
                        .eq(RankingSnapshot::getRankingDate, rankingDate)
                        .orderByAsc(RankingSnapshot::getRankNo)
                        .last("LIMIT " + limit));
        return Result.success(rankings);
    }
}