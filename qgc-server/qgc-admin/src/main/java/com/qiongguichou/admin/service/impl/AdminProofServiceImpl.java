package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.mapper.AdminLogMapper;
import com.qiongguichou.admin.service.AdminProofService;
import com.qiongguichou.campaign.entity.Proof;
import com.qiongguichou.campaign.entity.ProofImage;
import com.qiongguichou.campaign.mapper.ProofImageMapper;
import com.qiongguichou.campaign.mapper.ProofMapper;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台-返图管理Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminProofServiceImpl implements AdminProofService {

    private final ProofMapper proofMapper;
    private final ProofImageMapper proofImageMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public IPage<Proof> listProofs(Page<Proof> page, Long campaignId) {
        LambdaQueryWrapper<Proof> wrapper = new LambdaQueryWrapper<>();
        if (campaignId != null) {
            wrapper.eq(Proof::getCampaignId, campaignId);
        }
        wrapper.orderByDesc(Proof::getCreateTime);
        return proofMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getProofDetail(Long id) {
        Proof proof = proofMapper.selectById(id);
        if (proof == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "返图不存在");
        }

        List<ProofImage> images = proofImageMapper.selectList(
                new LambdaQueryWrapper<ProofImage>()
                        .eq(ProofImage::getProofId, id)
                        .orderByAsc(ProofImage::getSort));

        Map<String, Object> detail = new HashMap<>();
        detail.put("proof", proof);
        detail.put("images", images);
        return detail;
    }

    @Override
    public void deleteProof(Long id, Long adminId) {
        Proof proof = proofMapper.selectById(id);
        if (proof == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "返图不存在");
        }

        // 逻辑删除
        proofMapper.deleteById(id);

        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("DELETE");
        log.setTargetType("PROOF");
        log.setTargetId(String.valueOf(id));
        adminLogMapper.insert(log);
    }
}