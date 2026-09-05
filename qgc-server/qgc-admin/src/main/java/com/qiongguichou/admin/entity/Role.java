package com.qiongguichou.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色表
 */
@Data
@TableName("qgc_role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码: SUPER_ADMIN/OPERATOR/AUDITOR/FINANCE */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 描述 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}