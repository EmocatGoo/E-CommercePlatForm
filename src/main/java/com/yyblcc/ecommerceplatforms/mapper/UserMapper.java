package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.po.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    void update(User user);
}
