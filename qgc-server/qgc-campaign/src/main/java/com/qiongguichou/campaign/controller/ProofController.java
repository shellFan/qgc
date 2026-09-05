package com.qiongguichou.campaign.controller;

import com.qiongguichou.campaign.dto.ProofCreateDTO;
import com.qiongguichou.campaign.dto.ProofDetailVO;
import com.qiongguichou.campaign.service.ProofService;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 返图Controller
 */
@RestController
@RequestMapping("/api/campaign/proof")
@RequiredArgsConstructor
public class ProofController {

    private final ProofService proofService;

    /**
     * 创建返图
     */
    @PostMapping
    public Result<ProofDetailVO> create(@Valid @RequestBody ProofCreateDTO dto) {
        ProofDetailVO detail = proofService.create(dto);
        return Result.success(detail);
    }

    /**
     * 获取筹款项目的返图
     */
    @GetMapping("/{campaignId}")
    public Result<List<ProofDetailVO>> getByCampaignId(@PathVariable Long campaignId) {
        List<ProofDetailVO> proofs = proofService.getByCampaignId(campaignId);
        return Result.success(proofs);
    }

    /**
     * 点赞返图
     */
    @PostMapping("/{proofId}/like")
    public Result<Void> like(@PathVariable Long proofId) {
        proofService.like(proofId);
        return Result.success();
    }

    /**
     * 取消点赞返图
     */
    @DeleteMapping("/{proofId}/like")
    public Result<Void> unlike(@PathVariable Long proofId) {
        proofService.unlike(proofId);
        return Result.success();
    }
}