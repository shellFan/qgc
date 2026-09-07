package com.qiongguichou.campaign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.campaign.dto.CampaignCreateDTO;
import com.qiongguichou.campaign.dto.CampaignDetailVO;
import com.qiongguichou.campaign.dto.CampaignListVO;
import com.qiongguichou.campaign.dto.ProofDetailVO;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.entity.CampaignCategory;
import com.qiongguichou.campaign.entity.CampaignImage;
import com.qiongguichou.campaign.mapper.CampaignCategoryMapper;
import com.qiongguichou.campaign.mapper.CampaignImageMapper;
import com.qiongguichou.campaign.mapper.CampaignMapper;
import com.qiongguichou.campaign.service.CampaignService;
import com.qiongguichou.campaign.service.ProofService;
import com.qiongguichou.common.enums.CampaignStatus;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.util.MoneyUtil;
import com.qiongguichou.common.util.OrderNoUtil;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.common.util.XssUtil;
import com.qiongguichou.core.config.RedisService;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 筹款服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignMapper campaignMapper;
    private final CampaignCategoryMapper categoryMapper;
    private final CampaignImageMapper campaignImageMapper;
    private final UserMapper userMapper;
    private final RedisService redisService;
    private final ProofService proofService;

    private static final Set<Integer> VALID_DURATION_HOURS = new HashSet<>(Arrays.asList(24, 48, 72));
    private static final long MIN_TARGET_AMOUNT_FEN = 1L;
    private static final long MAX_TARGET_AMOUNT_FEN = 10000L;

    // 缓存Key前缀
    private static final String CACHE_CAMPAIGN_DETAIL = "qgc:cache:campaign:detail:";
    private static final String CACHE_CAMPAIGN_LIST = "qgc:cache:campaign:list:";
    private static final String CACHE_HOT_LIST = "qgc:cache:campaign:hot";
    private static final String CACHE_LIST_VERSION_KEY = "qgc:cache:campaign:list:version";
    private static final String CACHE_HOT_VERSION_KEY = "qgc:cache:campaign:hot:version";
    private static final long CACHE_DETAIL_TTL_MINUTES = 10;
    private static final long CACHE_LIST_TTL_MINUTES = 5;
    private static final long CACHE_HOT_TTL_MINUTES = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CampaignDetailVO create(CampaignCreateDTO dto) {
        Long userId = UserContext.getUserId();

        // 校验分类
        CampaignCategory category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null || category.getEnabled() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类不存在或已禁用");
        }

        // 校验金额(1~10000分)
        long targetAmountFen = MoneyUtil.yuanToFen(dto.getTargetAmount());
        if (targetAmountFen < MIN_TARGET_AMOUNT_FEN || targetAmountFen > MAX_TARGET_AMOUNT_FEN) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "目标金额范围为0.01~100元");
        }

        // 校验时长(24/48/72)
        if (!VALID_DURATION_HOURS.contains(dto.getDurationHours())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "筹款时长只支持24/48/72小时");
        }

        // 构建实体
        Campaign campaign = new Campaign();
        campaign.setCampaignNo(OrderNoUtil.campaignNo());
        campaign.setCreatorUserId(userId);
        campaign.setCategoryId(dto.getCategoryId());
        campaign.setTitle(XssUtil.clean(dto.getTitle()));
        campaign.setDescription(XssUtil.clean(dto.getDescription()));
        campaign.setCover(dto.getCover());
        campaign.setTargetAmount(targetAmountFen);
        campaign.setRaisedAmount(0L);
        campaign.setSupportCount(0);
        campaign.setViewCount(0);
        campaign.setDurationHours(dto.getDurationHours());
        campaign.setStartTime(LocalDateTime.now());
        campaign.setEndTime(LocalDateTime.now().plusHours(dto.getDurationHours()));
        campaign.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : "PUBLIC");
        campaign.setAllowRanking(dto.getAllowRanking() != null ? dto.getAllowRanking() : 1);
        campaign.setAllowComment(dto.getAllowComment() != null ? dto.getAllowComment() : 1);
        campaign.setProofStatus(0);
        campaign.setCreateBy(userId);
        campaign.setUpdateBy(userId);

        // 根据need_review配置决定status
        String needReview = redisService.get("qgc:config:campaign:need_review");
        if ("true".equals(needReview)) {
            campaign.setStatusEnum(CampaignStatus.PENDING_REVIEW);
        } else {
            campaign.setStatusEnum(CampaignStatus.ACTIVE);
        }

        // 保存项目
        campaignMapper.insert(campaign);

        // 保存图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<CampaignImage> imageList = new ArrayList<>();
            for (int i = 0; i < dto.getImages().size(); i++) {
                CampaignImage image = new CampaignImage();
                image.setCampaignId(campaign.getId());
                image.setImageUrl(dto.getImages().get(i));
                image.setSort(i);
                imageList.add(image);
            }
            imageList.forEach(campaignImageMapper::insert);
        }

        // 清理列表缓存和热门缓存
        clearListCache();
        clearHotCache();

        return getById(campaign.getId());
    }

    @Override
    public CampaignDetailVO getById(Long id) {
        // 尝试从Redis缓存读取
        try {
            String cacheKey = CACHE_CAMPAIGN_DETAIL + id;
            String cached = redisService.get(cacheKey);
            if (cached != null) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                CampaignDetailVO cachedVo = mapper.readValue(cached, CampaignDetailVO.class);
                // 缓存命中，仍增加浏览量
                redisService.incrementViewCount(id);
                return cachedVo;
            }
        } catch (Exception e) {
            log.warn("读取筹款详情缓存失败, id={}", id, e);
        }

        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }

        // 构建详情VO
        CampaignDetailVO vo = new CampaignDetailVO();
        vo.setId(campaign.getId());
        vo.setCampaignNo(campaign.getCampaignNo());
        vo.setCreatorUserId(campaign.getCreatorUserId());
        vo.setCategoryId(campaign.getCategoryId());
        vo.setTitle(campaign.getTitle());
        vo.setDescription(campaign.getDescription());
        vo.setCover(campaign.getCover());
        vo.setTargetAmount(campaign.getTargetAmount());
        vo.setRaisedAmount(campaign.getRaisedAmount());
        vo.setSupportCount(campaign.getSupportCount());
        vo.setViewCount(campaign.getViewCount());
        vo.setDurationHours(campaign.getDurationHours());
        vo.setStartTime(campaign.getStartTime());
        vo.setEndTime(campaign.getEndTime());
        vo.setVisibility(campaign.getVisibility());
        vo.setAllowRanking(campaign.getAllowRanking());
        vo.setAllowComment(campaign.getAllowComment());
        vo.setStatus(campaign.getStatus());
        vo.setRejectReason(campaign.getRejectReason());
        vo.setCloseReason(campaign.getCloseReason());
        vo.setProofStatus(campaign.getProofStatus());
        vo.setCreateTime(campaign.getCreateTime());
        vo.setUpdateTime(campaign.getUpdateTime());

        // 计算剩余金额
        long remaining = campaign.getTargetAmount() - campaign.getRaisedAmount();
        vo.setRemainingAmount(Math.max(0L, remaining));

        // 计算进度百分比(最大100)
        int progress = 0;
        if (campaign.getTargetAmount() > 0) {
            progress = (int) (campaign.getRaisedAmount() * 100 / campaign.getTargetAmount());
            progress = Math.min(progress, 100);
        }
        vo.setProgressPercent(progress);

        // 分类信息
        if (campaign.getCategoryId() != null) {
            CampaignCategory category = categoryMapper.selectById(campaign.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 创建者信息
        User creator = userMapper.selectById(campaign.getCreatorUserId());
        if (creator != null) {
            vo.setCreatorNickname(creator.getNickname());
            vo.setCreatorAvatar(creator.getAvatar());
        }

        // 当前用户是否为创建者
        Long currentUserId = UserContext.getUserId();
        vo.setIsCreator(currentUserId != null && currentUserId.equals(campaign.getCreatorUserId()));

        // 图片列表
        LambdaQueryWrapper<CampaignImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(CampaignImage::getCampaignId, id)
                .orderByAsc(CampaignImage::getSort);
        List<CampaignImage> images = campaignImageMapper.selectList(imageWrapper);
        vo.setImages(images.stream().map(CampaignImage::getImageUrl).collect(Collectors.toList()));

        // 返图信息
        if (campaign.getProofStatus() != null && campaign.getProofStatus() == 1) {
            List<ProofDetailVO> proofs = proofService.getByCampaignId(id);
            if (!proofs.isEmpty()) {
                vo.setProofInfo(proofs.get(0));
            }
        }

        // 写入Redis缓存
        try {
            String cacheKey = CACHE_CAMPAIGN_DETAIL + id;
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            String json = mapper.writeValueAsString(vo);
            redisService.set(cacheKey, json, CACHE_DETAIL_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写入筹款详情缓存失败, id={}", id, e);
        }

        // Redis增加浏览量
        redisService.incrementViewCount(id);

        return vo;
    }

    @Override
    public IPage<CampaignListVO> getList(int pageNum, int pageSize, String sort, Long categoryId, String keyword) {
        // 首页列表(无keyword)才走缓存, 使用版本化key避免SCAN扫描
        String listVersion = null;
        if (keyword == null || keyword.isEmpty()) {
            try {
                listVersion = redisService.get(CACHE_LIST_VERSION_KEY);
                if (listVersion == null) {
                    listVersion = "1";
                    redisService.set(CACHE_LIST_VERSION_KEY, listVersion, 24, TimeUnit.HOURS);
                }
                String cacheKey = CACHE_CAMPAIGN_LIST + listVersion + ":" + pageNum + ":" + pageSize + ":" + sort + ":" + (categoryId != null ? categoryId : 0);
                String cached = redisService.get(cacheKey);
                if (cached != null) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    return mapper.readValue(cached, new com.fasterxml.jackson.core.type.TypeReference<Page<CampaignListVO>>() {});
                }
            } catch (Exception e) {
                log.warn("读取筹款列表缓存失败", e);
            }
        }

        Page<CampaignListVO> page = new Page<>(pageNum, pageSize);
        IPage<CampaignListVO> result = campaignMapper.selectCampaignList(page, null, "ACTIVE", categoryId, keyword, sort);

        // 填充计算字段
        for (CampaignListVO vo : result.getRecords()) {
            fillCalculatedFields(vo);
        }

        // RANDOM排序: SQL使用ORDER BY id(快速), Java层shuffle实现随机效果, 避免ORDER BY RAND()全表扫描
        if ("RANDOM".equals(sort) && result.getRecords() != null && result.getRecords().size() > 1) {
            java.util.Collections.shuffle(result.getRecords());
        }

        // 首页列表(无keyword)写入缓存
        if (keyword == null || keyword.isEmpty()) {
            try {
                if (listVersion == null) {
                    listVersion = redisService.get(CACHE_LIST_VERSION_KEY);
                    if (listVersion == null) {
                        listVersion = "1";
                        redisService.set(CACHE_LIST_VERSION_KEY, listVersion, 24, TimeUnit.HOURS);
                    }
                }
                String cacheKey = CACHE_CAMPAIGN_LIST + listVersion + ":" + pageNum + ":" + pageSize + ":" + sort + ":" + (categoryId != null ? categoryId : 0);
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                String json = mapper.writeValueAsString(result);
                redisService.set(cacheKey, json, CACHE_LIST_TTL_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("写入筹款列表缓存失败", e);
            }
        }

        return result;
    }

    @Override
    public List<CampaignListVO> getHotList(int limit) {
        // 使用版本化key避免SCAN扫描
        try {
            String hotVersion = redisService.get(CACHE_HOT_VERSION_KEY);
            if (hotVersion == null) {
                hotVersion = "1";
                redisService.set(CACHE_HOT_VERSION_KEY, hotVersion, 24, TimeUnit.HOURS);
            }
            String cacheKey = CACHE_HOT_LIST + ":" + hotVersion + ":" + limit;
            String cached = redisService.get(cacheKey);
            if (cached != null) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                return mapper.readValue(cached, new com.fasterxml.jackson.core.type.TypeReference<List<CampaignListVO>>() {});
            }
        } catch (Exception e) {
            log.warn("读取热门筹款缓存失败", e);
        }

        List<CampaignListVO> list = campaignMapper.selectHotCampaigns(limit);
        for (CampaignListVO vo : list) {
            fillCalculatedFields(vo);
        }

        // 写入缓存
        try {
            String hotVersion = redisService.get(CACHE_HOT_VERSION_KEY);
            if (hotVersion == null) {
                hotVersion = "1";
                redisService.set(CACHE_HOT_VERSION_KEY, hotVersion, 24, TimeUnit.HOURS);
            }
            String cacheKey = CACHE_HOT_LIST + ":" + hotVersion + ":" + limit;
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            String json = mapper.writeValueAsString(list);
            redisService.set(cacheKey, json, CACHE_HOT_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写入热门筹款缓存失败", e);
        }

        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id, String closeReason) {
        Long userId = UserContext.getUserId();
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }

        // 校验权限(只有创建者可关闭)
        if (!campaign.getCreatorUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有创建者可以关闭筹款");
        }

        // 校验状态
        if (!CampaignStatus.ACTIVE.name().equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_ACTIVE);
        }

        campaign.setStatusEnum(CampaignStatus.CLOSED);
        campaign.setCloseReason(XssUtil.clean(closeReason));
        campaign.setUpdateBy(userId);
        campaignMapper.updateById(campaign);

        // 清理缓存
        clearDetailCache(id);
        clearListCache();
        clearHotCache();
    }

    @Override
    public void updateViewCount(Long id) {
        redisService.incrementViewCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncViewCounts() {
        // 使用SCAN扫描浏览量缓存key
        Set<String> keys = redisService.getKeysByPattern("qgc:campaign:views:*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                // 提取campaignId
                String idStr = key.replace("qgc:campaign:views:", "");
                Long campaignId = Long.parseLong(idStr);
                Integer viewCount = redisService.getViewCount(campaignId);
                if (viewCount != null && viewCount > 0) {
                    // 原子更新MySQL(使用SQL增量更新而非read-modify-write)
                    campaignMapper.incrementViewCount(campaignId, viewCount);
                    // 清除Redis缓存
                    redisService.clearViewCount(campaignId);
                }
            } catch (Exception e) {
                log.error("同步浏览量失败, key={}", key, e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkExpired() {
        // 查询所有ACTIVE状态且已过期的项目
        LambdaQueryWrapper<Campaign> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Campaign::getStatus, CampaignStatus.ACTIVE.name())
                .le(Campaign::getEndTime, LocalDateTime.now());

        List<Campaign> expiredCampaigns = campaignMapper.selectList(wrapper);
        for (Campaign campaign : expiredCampaigns) {
            // 判断是否筹满
            if (campaign.getRaisedAmount() >= campaign.getTargetAmount()) {
                campaign.setStatusEnum(CampaignStatus.SUCCESS);
            } else {
                campaign.setStatusEnum(CampaignStatus.EXPIRED);
            }
            campaignMapper.updateById(campaign);
            log.info("项目已过期, id={}, status={}", campaign.getId(), campaign.getStatus());
        }
    }

    @Override
    public IPage<CampaignListVO> getByCreatorId(Long userId, String status, int pageNum, int pageSize) {
        Page<CampaignListVO> page = new Page<>(pageNum, pageSize);
        IPage<CampaignListVO> result = campaignMapper.selectCampaignList(page, userId, status, null, null, "NEW");

        for (CampaignListVO vo : result.getRecords()) {
            fillCalculatedFields(vo);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long id, boolean approved, String rejectReason) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }

        if (!CampaignStatus.PENDING_REVIEW.name().equals(campaign.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "项目不在待审核状态");
        }

        if (approved) {
            campaign.setStatusEnum(CampaignStatus.ACTIVE);
        } else {
            campaign.setStatusEnum(CampaignStatus.REJECTED);
            campaign.setRejectReason(XssUtil.clean(rejectReason));
        }

        campaignMapper.updateById(campaign);

        // 清理缓存
        clearDetailCache(id);
        clearListCache();
        clearHotCache();
    }

    /**
     * 填充计算字段
     */
    private void fillCalculatedFields(CampaignListVO vo) {
        // 剩余金额
        long remaining = vo.getTargetAmount() - vo.getRaisedAmount();
        vo.setRemainingAmount(Math.max(0L, remaining));

        // 进度百分比(最大100)
        int progress = 0;
        if (vo.getTargetAmount() != null && vo.getTargetAmount() > 0) {
            progress = (int) (vo.getRaisedAmount() * 100 / vo.getTargetAmount());
            progress = Math.min(progress, 100);
        }
        vo.setProgressPercent(progress);

        // 剩余时间(秒)
        if (vo.getEndTime() != null) {
            long seconds = java.time.Duration.between(LocalDateTime.now(), vo.getEndTime()).getSeconds();
            vo.setRemainingTime(Math.max(0L, seconds));
        } else {
            vo.setRemainingTime(0L);
        }
    }

    /**
     * 清理筹款详情缓存
     */
    public void clearDetailCache(Long campaignId) {
        try {
            redisService.delete(CACHE_CAMPAIGN_DETAIL + campaignId);
        } catch (Exception e) {
            log.warn("清理筹款详情缓存失败, id={}", campaignId, e);
        }
    }

    /**
     * 清理列表缓存(版本化: 递增版本号, 旧key自然过期)
     */
    public void clearListCache() {
        try {
            // 递增版本号, 旧版本key自然过期, 无需SCAN扫描
            redisService.increment(CACHE_LIST_VERSION_KEY);
        } catch (Exception e) {
            log.warn("清理筹款列表缓存失败", e);
        }
    }

    /**
     * 清理热门缓存(版本化: 递增版本号, 旧key自然过期)
     */
    public void clearHotCache() {
        try {
            // 递增版本号, 旧版本key自然过期, 无需SCAN扫描
            redisService.increment(CACHE_HOT_VERSION_KEY);
        } catch (Exception e) {
            log.warn("清理热门筹款缓存失败", e);
        }
    }

    /**
     * 清理指定筹款的所有相关缓存(供支付后调用)
     */
    public void clearCampaignCache(Long campaignId) {
        clearDetailCache(campaignId);
        clearListCache();
        clearHotCache();
    }
}