package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.po.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService extends IService<User> {
    Result<?> register(UserRegisterDTO userRegisterDTO);

    Result<?> profile(Long userId);

    Result<?> delete(Long userId);

    Result<?> updatePassword(PasswordDTO passwordDTO);

    Result<?> modifyProfile(UserUpdateDTO userUpdateDTO);

    Result<?> pageUser(Integer page, Integer pageSize);

    Result<?> getUserByNickName(String nickname,Integer page, Integer pageSize);

    Result<?> login(LoginDTO loginDTO, HttpServletRequest request);

    Result<?> updateStatus(UserStatusDTO userStatusDTO);

    Result<?> checkUserInfo(String username,String phone);

    Result<?> checkEmail(String email);

    Result<?> addAddress(UserAddressDTO addressDTO);

    Result<?> updateAddress(UserAddressDTO addressDTO);

    Result<?> deleteAddress(Long addressId);

    Result<?> batchDeleteAddress(List<Long> addressIds);

    Result<?> getUserAddressList(Long userId);

    Result<?> setDefaultAddress(Long addressId);

    Result<?> updateAvatar(String avatar);

    Result<?> getUserCounts();

    Result getUserDefaultAddress(Long userId);
}
