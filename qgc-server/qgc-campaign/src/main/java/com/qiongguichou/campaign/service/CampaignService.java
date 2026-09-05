package com.qiongguichou.campaign.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.campaign.dto.CampaignCreateDTO;
import com.qiongguichou.campaign.dto.CampaignDetailVO;
import com.qiongguichou.campaign.dto.CampaignListVO;
import com.qiongguichou.campaign.entity.Campaign;

import java.util.List;

/**
 * 筹款服务接口
 */
public interface CampaignService {

    /**
     * 创建筹款项目
     *
     * @param dto 创建请求
     * @return 筹款详情
     */
    CampaignDetailVO create(CampaignCreateDTO dto);

    /**
     * 获取筹款详情
     *
     * @param id 筹款ID
     * @return 筹款详情
     */
    CampaignDetailVO getById(Long id);

    /**
     * 分页查询筹款列表
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param sort       排序方式: NEW/ALMOST/RANDOM/POPULAR
     * @param categoryId 分类ID筛选
     * @param keyword    关键词搜索
     * @return 分页结果
     */
    IPage<CampaignListVO> getList(int pageNum, int pageSize, String sort, Long categoryId, String keyword);

    /**
     * 获取热门筹款列表
     *
     * @param limit 数量限制
     * @return 热门项目列表
     */
    List<CampaignListVO> getHotList(int limit);

    /**
     * 关闭筹款项目
     *
     * @param id          筹款ID
     * @param closeReason 关闭原因
     */
    void close(Long id, String closeReason);

    /**
     * 更新浏览量(Redis increment)
     *
     * @param id 筹款ID
     */
    void updateViewCount(Long id);

    /**
     * 批量从Redis同步浏览量到MySQL
     */
    void syncViewCounts();

    /**
     * 扫描已过期项目更新状态
     */
    void checkExpired();

    /**
     * 获取我创建的筹款列表
     *
     * @param userId  用户ID
     * @param status  状态筛选
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<CampaignListVO> getByCreatorId(Long userId, String status, int pageNum, int pageSize);

    /**
     * 审核筹款项目
     *
     * @param id           筹款ID
     * @param approved     是否通过
     * @param rejectReason 拒绝原因(审核不通过时)
     */
    void review(Long id, boolean approved, String rejectReason);
}