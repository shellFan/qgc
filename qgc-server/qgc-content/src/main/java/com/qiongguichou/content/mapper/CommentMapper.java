package com.qiongguichou.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiongguichou.content.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}