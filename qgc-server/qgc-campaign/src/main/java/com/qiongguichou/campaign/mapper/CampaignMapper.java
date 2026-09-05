package com.qiongguichou.campaign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.dto.CampaignListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筹款项目Mapper接口
 */
@Mapper
public interface CampaignMapper extends BaseMapper<Campaign> {

    /**
     * 分页查询筹款列表(多条件筛选)
     *
     * @param page       分页参数
     * @param status     状态筛选
     * @param categoryId 分类ID筛选
     * @param keyword    关键词搜索
     * @param sort       排序方式: NEW/ALMOST/RANDOM/POPULAR
     * @return 分页结果
     */
    IPage<CampaignListVO> selectCampaignList(IPage<CampaignListVO> page,
                                              @Param("creatorUserId") Long creatorUserId,
                                              @Param("status") String status,
                                              @Param("categoryId") Long categoryId,
                                              @Param("keyword") String keyword,
                                              @Param("sort") String sort);

    /**
     * 查询热门项目(按support_count和raised_amount综合排序)
     *
     * @param limit 数量限制
     * @return 热门项目列表
     */
    List<CampaignListVO> selectHotCampaigns(@Param("limit") int limit);

    /**
     * 更新已筹金额(乐观锁条件更新)
     *
     * @param id          项目ID
     * @param addAmount   增加金额(分)
     * @return 影响行数
     */
    int updateRaisedAmount(@Param("id") Long id, @Param("addAmount") Long addAmount);
}