package com.qiongguichou.campaign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.campaign.dto.CampaignCreateDTO;
import com.qiongguichou.campaign.dto.CampaignDetailVO;
import com.qiongguichou.campaign.dto.CampaignListVO;
import com.qiongguichou.campaign.service.CampaignService;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.core.config.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * 筹款项目Controller
 */
@RestController
@RequestMapping("/api/campaign")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final RedisService redisService;

    /**
     * 创建筹款
     */
    @PostMapping
    public Result<CampaignDetailVO> create(@Valid @RequestBody CampaignCreateDTO dto) {
        CampaignDetailVO detail = campaignService.create(dto);
        return Result.success(detail);
    }

    /**
     * 获取筹款详情
     */
    @GetMapping("/{id}")
    public Result<CampaignDetailVO> getById(@PathVariable Long id) {
        CampaignDetailVO detail = campaignService.getById(id);
        return Result.success(detail);
    }

    /**
     * 广场列表
     */
    @GetMapping("/list")
    public Result<IPage<CampaignListVO>> getList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "NEW") String sort,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        IPage<CampaignListVO> page = campaignService.getList(pageNum, pageSize, sort, categoryId, keyword);
        return Result.success(page);
    }

    /**
     * 关闭筹款
     */
    @PostMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String closeReason = body != null ? body.get("closeReason") : null;
        campaignService.close(id, closeReason);
        return Result.success();
    }

    /**
     * 我的筹款
     */
    @GetMapping("/my")
    public Result<IPage<CampaignListVO>> getMyCampaigns(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = UserContext.getUserId();
        IPage<CampaignListVO> page = campaignService.getByCreatorId(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取随机骚话
     */
    @GetMapping("/random-messages")
    public Result<List<String>> getRandomMessages() {
        // 从Redis获取随机骚话，缓存不存在则返回默认
        String key = "qgc:random:messages";
        String cached = redisService.get(key);
        if (cached != null) {
            String[] messages = cached.split("\\|");
            return Result.success(Arrays.asList(messages));
        }

        // 默认骚话
        List<String> defaultMessages = Arrays.asList(
                "省着点花。",
                "别饿死，项目还没上线。",
                "钱不多，义气管够。",
                "投喂穷鬼，功德+1。",
                "穷鬼互助，人间有爱。",
                "目标不大，V我50。",
                "感谢各位义父。",
                "穷鬼的一天从筹款开始。"
        );
        return Result.success(defaultMessages);
    }
}