package com.qiongguichou.campaign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.campaign.dto.ProofCreateDTO;
import com.qiongguichou.campaign.dto.ProofDetailVO;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.entity.Proof;
import com.qiongguichou.campaign.entity.ProofImage;
import com.qiongguichou.campaign.entity.ProofLike;
import com.qiongguichou.campaign.mapper.CampaignMapper;
import com.qiongguichou.campaign.mapper.ProofImageMapper;
import com.qiongguichou.campaign.mapper.ProofLikeMapper;
import com.qiongguichou.campaign.mapper.ProofMapper;
import com.qiongguichou.campaign.service.ProofService;
import com.qiongguichou.common.enums.CampaignStatus;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.common.util.XssUtil;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 返图服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProofServiceImpl implements ProofService {

    private final ProofMapper proofMapper;
    private final ProofImageMapper proofImageMapper;
    private final ProofLikeMapper proofLikeMapper;
    private final CampaignMapper campaignMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProofDetailVO create(ProofCreateDTO dto) {
        Long userId = UserContext.getUserId();

        // 校验筹款项目
        Campaign campaign = campaignMapper.selectById(dto.getCampaignId());
        if (campaign == null) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }

        // 校验权限(只有创建者可返图)
        if (!campaign.getCreatorUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有筹款发起人可以提交返图");
        }

        // 校验项目状态(只有ACTIVE/SUCCESS状态可返图)
        String status = campaign.getStatus();
        if (!CampaignStatus.ACTIVE.name().equals(status) && !CampaignStatus.SUCCESS.name().equals(status)) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_ACTIVE, "筹款项目当前状态不允许返图");
        }

        // 构建返图实体
        Proof proof = new Proof();
        proof.setCampaignId(dto.getCampaignId());
        proof.setUserId(userId);
        proof.setTitle(XssUtil.clean(dto.getTitle()));
        proof.setContent(XssUtil.clean(dto.getContent()));
        proof.setLikeCount(0);
        proof.setCommentCount(0);

        proofMapper.insert(proof);

        // 保存图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<ProofImage> imageList = new ArrayList<>();
            for (int i = 0; i < dto.getImages().size(); i++) {
                ProofImage image = new ProofImage();
                image.setProofId(proof.getId());
                image.setImageUrl(dto.getImages().get(i));
                image.setSort(i);
                imageList.add(image);
            }
            imageList.forEach(proofImageMapper::insert);
        }

        // 更新筹款项目的返图状态
        campaign.setProofStatus(1);
        campaignMapper.updateById(campaign);

        return buildProofDetailVO(proof);
    }

    @Override
    public List<ProofDetailVO> getByCampaignId(Long campaignId) {
        LambdaQueryWrapper<Proof> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Proof::getCampaignId, campaignId)
                .orderByDesc(Proof::getCreateTime);

        List<Proof> proofs = proofMapper.selectList(wrapper);
        return proofs.stream()
                .map(this::buildProofDetailVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long proofId) {
        Long userId = UserContext.getUserId();

        // 校验返图存在
        Proof proof = proofMapper.selectById(proofId);
        if (proof == null) {
            throw new BusinessException(ErrorCode.PROOF_NOT_FOUND);
        }

        // 检查是否已点赞
        LambdaQueryWrapper<ProofLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProofLike::getProofId, proofId)
                .eq(ProofLike::getUserId, userId);
        Long count = proofLikeMapper.selectCount(wrapper);
        if (count > 0) {
            return; // 已点赞，幂等处理
        }

        // 保存点赞记录
        ProofLike proofLike = new ProofLike();
        proofLike.setProofId(proofId);
        proofLike.setUserId(userId);
        proofLikeMapper.insert(proofLike);

        // 更新点赞数
        proof.setLikeCount(proof.getLikeCount() + 1);
        proofMapper.updateById(proof);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long proofId) {
        Long userId = UserContext.getUserId();

        // 删除点赞记录
        LambdaQueryWrapper<ProofLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProofLike::getProofId, proofId)
                .eq(ProofLike::getUserId, userId);
        int deleted = proofLikeMapper.delete(wrapper);

        if (deleted > 0) {
            // 更新点赞数
            Proof proof = proofMapper.selectById(proofId);
            if (proof != null && proof.getLikeCount() > 0) {
                proof.setLikeCount(proof.getLikeCount() - 1);
                proofMapper.updateById(proof);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long proofId) {
        Long userId = UserContext.getUserId();

        Proof proof = proofMapper.selectById(proofId);
        if (proof == null) {
            throw new BusinessException(ErrorCode.PROOF_NOT_FOUND);
        }

        // 校验权限(只有创建者可删除)
        if (!proof.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有返图创建者可以删除");
        }

        // 逻辑删除
        proofMapper.deleteById(proofId);

        // 删除图片
        LambdaQueryWrapper<ProofImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProofImage::getProofId, proofId);
        proofImageMapper.delete(imageWrapper);

        // 删除点赞
        LambdaQueryWrapper<ProofLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(ProofLike::getProofId, proofId);
        proofLikeMapper.delete(likeWrapper);
    }

    /**
     * 构建返图详情VO
     */
    private ProofDetailVO buildProofDetailVO(Proof proof) {
        ProofDetailVO vo = new ProofDetailVO();
        vo.setId(proof.getId());
        vo.setCampaignId(proof.getCampaignId());
        vo.setUserId(proof.getUserId());
        vo.setTitle(proof.getTitle());
        vo.setContent(proof.getContent());
        vo.setLikeCount(proof.getLikeCount());
        vo.setCommentCount(proof.getCommentCount());
        vo.setCreateTime(proof.getCreateTime());
        vo.setUpdateTime(proof.getUpdateTime());

        // 图片列表
        LambdaQueryWrapper<ProofImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProofImage::getProofId, proof.getId())
                .orderByAsc(ProofImage::getSort);
        List<ProofImage> images = proofImageMapper.selectList(imageWrapper);
        vo.setImages(images.stream().map(ProofImage::getImageUrl).collect(Collectors.toList()));

        // 当前用户是否已点赞
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null) {
            LambdaQueryWrapper<ProofLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(ProofLike::getProofId, proof.getId())
                    .eq(ProofLike::getUserId, currentUserId);
            vo.setIsLiked(proofLikeMapper.selectCount(likeWrapper) > 0);
        } else {
            vo.setIsLiked(false);
        }

        // 创建者信息
        User creator = userMapper.selectById(proof.getUserId());
        if (creator != null) {
            vo.setCreatorNickname(creator.getNickname());
            vo.setCreatorAvatar(creator.getAvatar());
        }

        // 关联筹款项目信息
        Campaign campaign = campaignMapper.selectById(proof.getCampaignId());
        if (campaign != null) {
            vo.setCampaignTitle(campaign.getTitle());
            vo.setCampaignTargetAmount(campaign.getTargetAmount());
            vo.setCampaignRaisedAmount(campaign.getRaisedAmount());
        }

        return vo;
    }
}