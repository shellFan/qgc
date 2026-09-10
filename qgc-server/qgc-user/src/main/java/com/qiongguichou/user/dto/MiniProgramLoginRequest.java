package com.qiongguichou.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class MiniProgramLoginRequest {
    @NotBlank(message = "微信登录code不能为空")
    @Size(max = 512, message = "微信登录code无效")
    private String code;

    @Size(max = 20, message = "昵称最长20个字符")
    private String nickname;

    @Size(max = 500, message = "头像地址无效")
    private String avatar;
}
