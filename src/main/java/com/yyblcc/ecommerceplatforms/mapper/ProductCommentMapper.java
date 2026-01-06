package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.po.ProductComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductComment> {
    List<ProductComment> listByProductId(@Param("productId") Long productId,
                               @Param("offset") int offset,
                               @Param("size") int size);

    List<ProductComment> listChildren(@Param("path") String path, @Param("currentId") Long currentId);

    List<ProductComment> listByRootId(@Param("rootId") Long rootId);

    @Select("""
        SELECT MAX(path)
        FROM tb_product_comment 
        WHERE product_id = #{productId} AND (root_id IS NULL OR root_id = 0)
    """)
    String selectMaxRootPath(Long productId);

    // 查询同 parentPath 下最大子 path
    @Select("""
        SELECT MAX(path)
        FROM tb_product_comment 
        WHERE path LIKE CONCAT(#{parentPathPrefix}, '%')
    """)
    String selectMaxChildPath(String parentPathPrefix);


}
