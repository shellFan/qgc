package com.qiongguichou.campaign.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 返图创建请求DTO
 */
@Data
public class ProofCreateDTO {

    /**
     * 筹款项目ID
     */
    @NotNull(message = "筹款项目ID不能为空")
    private Long campaignId;

    /**
     * 返图标题
     */
    @NotBlank(message = "返图标题不能为空")
    @Size(max = 128, message = "标题最长128个字符")
    private String title;

    /**
     * 返图内容
     */
    @NotBlank(message = "返图内容不能为空")
    @Size(max = 5000, message = "内容最长5000个字符")
    private String content;

    /**
     * 图片URL列表
     */
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;
}