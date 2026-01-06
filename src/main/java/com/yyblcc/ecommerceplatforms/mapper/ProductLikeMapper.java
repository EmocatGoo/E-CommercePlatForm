package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.po.UserProductFavorite;
import com.yyblcc.ecommerceplatforms.domain.po.UserProductLike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductLikeMapper extends BaseMapper<UserProductLike> {
    void insertIgnore(Long userId, Long productId);
    void deleteByUserIdAndProductId(Long userId, Long productId);
}
