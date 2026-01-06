package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.po.EventComment;
import com.yyblcc.ecommerceplatforms.domain.po.ProductComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EventCommentMapper extends BaseMapper<EventComment> {
    @Select("""
        SELECT MAX(path)
        FROM tb_event_comment 
        WHERE event_id = #{eventId} AND (root_id IS NULL OR root_id = 0)
    """)
    String selectMaxRootPath(Long productId);

    // 查询同 parentPath 下最大子 path
    @Select("""
        SELECT MAX(path)
        FROM tb_event_comment 
        WHERE path LIKE CONCAT(#{parentPathPrefix}, '%')
    """)
    String selectMaxChildPath(String parentPathPrefix);


}
