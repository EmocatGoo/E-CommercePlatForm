package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.UserService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("/register")
    public Result<?> save(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("userRegisterDTO={}", userRegisterDTO);
        return userService.register(userRegisterDTO);
    }

    @GetMapping("/check")
    public Result<?> check(@RequestParam String username, @RequestParam String phone) {
        log.info("username={},phone={}", username, phone);
        return userService.checkUserInfo(username,phone);
    }

    @GetMapping("/check-email")
    public Result<?> checkEmail(@RequestParam String email) {
        log.info("userInfoCheckDTO={}", email);
        return userService.checkEmail(email);
    }

    /**
     * 用户登录
     * @param loginDTO
     * @param request
     * @return
     */
    //TODO 实现登录功能
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        log.info("loginDTO={}", loginDTO);
        return userService.login(loginDTO, request);
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        Long userId = StpKit.USER.getLoginIdAsLong();
        if (!userId.equals(StpKit.USER.getLoginIdAsLong())){
            return Result.error("用户信息不一致!");
        }
        StpKit.USER.logout(userId);
        return Result.success("已退出登录");
    }

    /**
     * 获取用户个人信息
     * @param
     * @return
     */
    @GetMapping("/profile")
    //TODO 改用上下文获取id
    public Result<?> getUserProfile() {
        Long userId = AuthContext.getUserId();
        return userService.profile(userId);
    }

    /**
     * 模糊查询用户昵称
     * @param nickname
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/nameselect")
    public Result<?> getUserByNickName(@RequestParam String nickname,
                                       @RequestParam(defaultValue = "1") @Min(1) Integer page,
                                       @RequestParam(defaultValue = "10") @Max(20) Integer pageSize) {
        return userService.getUserByNickName(nickname, page, pageSize);
    }

    /**
     * 用户注销
     * @param userId
     * @return
     */
    @DeleteMapping("/remove/{id}")
    public Result<?> userDelete(@PathVariable("id") Long userId) {
        return userService.delete(userId);
    }

    /**
     * 密码修改
     * @param passwordDTO
     * @return
     */
    @PutMapping("/password-update")
    public Result<?> updatePassword(@RequestBody PasswordDTO passwordDTO) {
        return userService.updatePassword(passwordDTO);
    }

    /**
     * 用户修改个人信息
     * @param userUpdateDTO
     * @return
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody UserUpdateDTO userUpdateDTO) {
        return userService.modifyProfile(userUpdateDTO);
    }

    /**
     * 分页查询用户信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1")Integer page, @RequestParam(defaultValue = "5")Integer pageSize){
        return userService.pageUser(page,pageSize);
    }

    /**
     * 管理员封禁用户
     * @param userId
     * @param status
     * @param rejectReason
     * @return
     */
    @PutMapping("/status")
    public Result<?> updateStatus(@RequestParam Long userId,@RequestParam Integer status,@RequestParam String rejectReason) {
        UserStatusDTO userStatusDTO = new UserStatusDTO(userId, status, rejectReason);
        return userService.updateStatus(userStatusDTO);
    }

    /**
     * 用户添加地址
     * @param addressDTO
     * @return
     */
    @PostMapping("/address/add")
    public Result<?> addAddress(@RequestBody UserAddressDTO addressDTO) {
        log.info("address={}", addressDTO);
        return userService.addAddress(addressDTO);
    }

    /**
     * 用户更新地址
     * @param addressDTO
     * @return
     */
    @PutMapping("/address/update")
    public Result<?> updateAddress(@RequestBody UserAddressDTO addressDTO) {
        log.info("address={}", addressDTO);
        return userService.updateAddress(addressDTO);
    }

    /**
     * 用户删除地址
     * @param addressId
     * @return
     */
    @DeleteMapping("/address/delete/{id}")
    public Result<?> deleteAddress(@PathVariable("id") Long addressId) {
        log.info("address={}", addressId);
        return userService.deleteAddress(addressId);
    }

    /**
     * 批量删除地址
     * @param addressIds
     * @return
     */
    @DeleteMapping("/address/batch")
    public Result<?> batchDeleteAddress(@RequestBody List<Long> addressIds) {
        log.info("address={}", addressIds);
        return userService.batchDeleteAddress(addressIds);
    }

    /**
     * 用户获取地址列表
     * @return
     */
    @GetMapping("/address")
    public Result<?> getAddressList() {
        Long userId = StpKit.USER.getLoginIdAsLong();
        log.info("获取用户{}的地址列表", userId);
        return userService.getUserAddressList(userId);
    }

    /**
     * 设置默认地址
     * @param addressId
     * @return
     */
    @PutMapping("/address/setDefault")
    public Result<?> setDefaultAddress(@RequestParam Long addressId) {
        log.info("address={}", addressId);
        return userService.setDefaultAddress(addressId);
    }

    @GetMapping("/getDefaultAddress")
    public Result getDefaultAddress(){
        Long userId = StpKit.USER.getLoginIdAsLong();
        log.info("获取用户:{}的默认地址",userId);
        return userService.getUserDefaultAddress(userId);
    }

    /**
     * 更新用户头像
     * @param avatar 头像 URL
     * @return
     */
    @PutMapping("/updateAvatar")
    public Result<?> updateAvatar(@RequestParam("avatar") String avatar) {
        log.info("更新用户头像: avatar={}", avatar);
        return userService.updateAvatar(avatar);
    }

    @GetMapping("/getUserCounts")
    public Result<?> getUserCounts() {
        log.info("获取用户数量");
        return userService.getUserCounts();
    }

}
