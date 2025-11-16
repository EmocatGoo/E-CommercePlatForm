package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.po.Craftsman;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CraftsmanMapper extends BaseMapper<Craftsman> {

    void update(Craftsman craftsman);
}
