package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.po.UserArticleFavorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleFavoriteMapper extends BaseMapper<UserArticleFavorite> {
}
