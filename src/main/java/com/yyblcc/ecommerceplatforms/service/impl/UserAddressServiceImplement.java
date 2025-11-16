package com.yyblcc.ecommerceplatforms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.po.UserAddress;
import com.yyblcc.ecommerceplatforms.mapper.UserAddressMapper;
import com.yyblcc.ecommerceplatforms.service.UserAddressService;
import org.springframework.stereotype.Service;

@Service
public class UserAddressServiceImplement extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

}
