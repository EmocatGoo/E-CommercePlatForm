package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.po.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    void update(Admin admin);
}
