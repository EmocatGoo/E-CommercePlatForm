package com.yyblcc.ecommerceplatforms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Optional;
import com.yyblcc.ecommerceplatforms.constant.UsernamePrifixConstant;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.Enum.RoleEnum;
import com.yyblcc.ecommerceplatforms.domain.VO.UserVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.po.User;
import com.yyblcc.ecommerceplatforms.domain.po.UserAddress;
import com.yyblcc.ecommerceplatforms.mapper.UserAddressMapper;
import com.yyblcc.ecommerceplatforms.mapper.UserMapper;
import com.yyblcc.ecommerceplatforms.service.UserAddressService;
import com.yyblcc.ecommerceplatforms.service.UserService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import com.yyblcc.ecommerceplatforms.util.redis.AccountLockCheck;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.util.redis.CacheClient;
import com.yyblcc.ecommerceplatforms.util.chainofresponsibility.FinalCheck;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImplement extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AccountLockCheck accountLockCheck;

    private static final Integer NULL_USER_KEY_TTL = 10;
    private static final String USER_SEARCH_KEYPRIFIX = "search:user:";
    private static final Long USER_SEARCH_TTL = 15L;
    private static final Integer USER_PAGE_TTL = 15;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private UserAddressService userAddressService;
    @Autowired
    private UserAddressMapper userAddressMapper;


    @Override
    /**
     * 用户注册
     */
    @UpdateBloomFilter
    public Result<?> register(UserRegisterDTO userRegisterDTO) {
        User user = new User();
        BeanUtils.copyProperties(userRegisterDTO,user);
        user.setUsername(UsernamePrifixConstant.USER_USERNAME_PRIFIX + userRegisterDTO.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setNickname(UUID.randomUUID().toString().replace("-","").substring(0,12));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        save(user);
        return Result.success("注册成功!");
    }

    @Override
    /**
     * 获取用户个人信息
     */
    public Result<?> profile(Long userId) {
        if (userId == null) {
            return Result.error("发生查询异常!");
        }
        Optional<UserVO> optional = cacheClient.query(
                USER_SEARCH_KEYPRIFIX,
                userId,
                UserVO.class,
                id ->{
                    User user = query().eq("id", id).one();
                    if (user == null) {
                        return null;
                    }
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user,userVO);
                    return userVO;
                },
                USER_SEARCH_TTL,
                TimeUnit.MINUTES
        );
        if (optional.isPresent()) {
            return Result.success(optional.get());
        }
        return Result.success();
    }

    @Override
    /**
     * 注销用户
     */
    public Result<?> delete(Long userId) {
        if (userId == null) {
            return Result.error("查询异常!");
        }
        boolean success = removeById(userId);
        if (success) {
            try{
                stringRedisTemplate.delete(USER_SEARCH_KEYPRIFIX + userId);
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
            //缓存 null 防止穿透
            cacheClient.set(USER_SEARCH_KEYPRIFIX + userId, "null", 2L, TimeUnit.MINUTES);
        }
        return Result.success();
    }

    @Override
    public Result<?> updatePassword(PasswordDTO passwordDTO, HttpServletRequest request) {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Result.error("发生异常!");
        }
        User user = query().eq("id", userId).one();
        if (user == null) {
            stringRedisTemplate.opsForValue().set(USER_SEARCH_KEYPRIFIX + userId,"null",NULL_USER_KEY_TTL, TimeUnit.MINUTES);
            return Result.error("未找到用户!");
        }
        if (passwordDTO.getOldPassword().equals(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()))) {
            user.setPassword(DigestUtils.md5DigestAsHex(passwordDTO.getNewPassword().getBytes()));
            userMapper.update(user);
        }
        stringRedisTemplate.delete(USER_SEARCH_KEYPRIFIX + userId);
        StpUtil.logout(userId);
        return Result.success("密码更新成功!");
    }

    @Override
    public Result<?> modifyProfile(UserUpdateDTO userUpdateDTO) {
        Long userId = AuthContext.getUserId();
        if (userUpdateDTO == null) {
            return Result.error("参数错误");
        }

        String key = USER_SEARCH_KEYPRIFIX + userId;

        // 查数据库
        User user = getById(userId);
        if (user == null) {
            stringRedisTemplate.opsForValue().set(key, "null", NULL_USER_KEY_TTL, TimeUnit.MINUTES);
            return Result.error("未找到用户！");
        }

        // 更新字段
        BeanUtils.copyProperties(userUpdateDTO, user, "id", "password");
        updateById(user);

        // 更新缓存
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(userVO), USER_SEARCH_TTL, TimeUnit.MINUTES);

        return Result.success("修改成功");
    }

    @Override
    public Result<?> pageUser(Integer page, Integer pageSize) {
        String key = "user:page:" + page + ":" +pageSize;
        try{
            String jsonStr = stringRedisTemplate.opsForValue().get(key);
            if (jsonStr != null){
                return Result.success(JSON.parseObject(jsonStr, PageBean.class));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Result.error(e.getMessage());
        }
        Page<User> userPage = new Page<>(page,pageSize);
        Page<User> userPageList = userMapper.selectPage(userPage, null);

        List<UserVO> userVOList = userPageList.getRecords().stream().map(this::convertToVO).toList();
        PageBean<UserVO> pageBean = new PageBean<>(userPageList.getTotal(),userVOList);
        stringRedisTemplate.opsForValue().set(key,JSON.toJSONString(pageBean),USER_PAGE_TTL, TimeUnit.MINUTES);
        return Result.success(pageBean);
    }

    @Override
    public Result<?> getUserByNickName(String nickname,Integer page, Integer pageSize) {
        Page<User> userPage = new Page<>(page, pageSize);
        Page<User> userPageList = userMapper.selectPage(userPage,
                new LambdaQueryWrapper<User>()
                        .like(User::getNickname,nickname)
                        .orderByDesc(User::getCreateTime));
        List<UserVO> userVOList = userPageList.getRecords().stream().map(this::convertToVO).toList();
        PageBean pageBean = new PageBean(userPageList.getTotal(),userVOList);
        return Result.success(pageBean);
    }

    @Override
    public Result<?> login(LoginDTO loginDTO,HttpServletRequest request) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String phone = loginDTO.getPhone();

        String identifier = loginDTO.getLoginType() == 1 ? username : phone;

        if (accountLockCheck.isAccountLocked(identifier)){
            return Result.error("账号已锁定,请稍后再试");
        }

        if (loginDTO.getLoginType() == 1){
            return UsernameLogin(username, password, identifier);
        } else if (loginDTO.getLoginType() == 2) {
            return PhoneLogin(phone, password, identifier,request);
        }
        return Result.error("不支持的登陆类型!");
    }

    @Override
    public Result<?> updateStatus(UserStatusDTO userStatusDTO) {
        Long userId = userStatusDTO.getUserId();
        User exist = query().eq("id", userId).one();
        if (exist == null) {
            return Result.error("查询异常!未找到用户信息!");
        }
        User user =User.builder()
                .id(userId)
                .status(userStatusDTO.getStatus())
                .rejectReason(userStatusDTO.getRejectReason())
                .build();
        userMapper.update(user);
        try {
            stringRedisTemplate.keys("user:page:*").forEach(key ->{stringRedisTemplate.delete(key);});
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Result.error(e.getMessage());
        }

        return Result.success("修改成功!");
    }

    @Override
    public Result<?> checkUserInfo(String username,String phone) {
        String checkDbUsername = UsernamePrifixConstant.USER_USERNAME_PRIFIX + username;
        if(query().eq("username", checkDbUsername).one() != null){
            return Result.error("该用户名已被注册!");
        }else if(query().eq("phone", phone).one() != null){
            return Result.error("该手机号码已被注册!");
        }else{
            return Result.success();
        }
    }

    @Override
    public Result<?> checkEmail(String email) {
        if(query().eq("email", email).one() != null){
            return Result.error("该邮箱已被注册!");
        }
        return Result.success();
    }

    @Override
    @UpdateBloomFilter
    public Result<?> addAddress(UserAddressDTO addressDTO) {
//        Long userId = addressDTO.getUserId();
        //TODO 测试环境
        Long userId = 4L;
        addressDTO.setUserId(userId);
        User user = query().eq("id", userId).one();
        if (user == null) {
            return Result.error("添加地址失败!未找到用户信息!");
        }
//        Long loginId = AuthContext.getUserId();
//        if (!loginId.equals(userId)) {
//            return Result.error("用户信息不一致!");
//        }
        addressDTO.setCreateTime(LocalDateTime.now());
        addressDTO.setUpdateTime(LocalDateTime.now());
        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(addressDTO,address);
        userAddressService.save(address);
        return Result.success();
    }

    @Override
    public Result<?> updateAddress(UserAddressDTO addressDTO) {
        UserAddress dbAddress = userAddressService.query().eq("id", addressDTO.getId()).one();
        if (dbAddress == null) {
            return Result.error("");
        }
        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(addressDTO,address);
        userAddressMapper.updateById(address);
        return Result.success();
    }

    @Override
    public Result<?> deleteAddress(Long addressId) {
        Long userId = AuthContext.getUserId();
        userAddressService.query().eq("user_id", userId).list().forEach(address -> {
            address.setIsDefault(0);
            userAddressMapper.updateById(address);
        });
        int count = userAddressMapper.deleteById(addressId);
        if (count > 0) {
            return Result.success("删除成功!");
        }
        return Result.error("删除失败!");
    }

    @Override
    public Result<?> batchDeleteAddress(List<Long> addressIds) {
        Long userId = AuthContext.getUserId();
        userAddressService.query().eq("user_id", userId).list().forEach(address -> {
            address.setIsDefault(0);
            userAddressMapper.updateById(address);
        });
        int count = userAddressMapper.deleteByIds(addressIds);
        if (count > 0) {
            return Result.success("删除成功!");
        }
        return Result.error("删除失败!");
    }

    @Override
    public Result<?> getUserAddressList(Long userId) {
        List<UserAddress> userAddressList = userAddressService.query().eq("user_id", userId).list();
        return Result.success(userAddressList);
    }

    @Override
    public Result<?> setDefaultAddress(Long addressId) {
        Long userId = AuthContext.getUserId();
        userAddressService.query().eq("user_id", userId).list().forEach(address -> {
            address.setIsDefault(0);
            userAddressMapper.updateById(address);
        });
        if (new LambdaUpdateChainWrapper<>(userAddressMapper).eq(UserAddress::getId, addressId).set(UserAddress::getIsDefault, 1).update()){
            return Result.success("设置成功!");
        }
        return Result.error("设置失败!");
    }



    private Result PhoneLogin(String phone, String password,String identifier, HttpServletRequest request) {
        if (StringUtils.isBlank(phone)) {
            return Result.error("手机号码不能为空!");
        }
        if (StringUtils.isBlank(password)) {
            return Result.error("密码不能为空!");
        }
        User user = query().eq("phone", phone).one();
        if (user == null) {
            log.warn("登陆失败:用户不存在->{}", phone);
            return accountLockCheck.incrementAndCheckLock(identifier);
        }
        String inputPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!inputPassword.equals(user.getPassword())){
            log.warn("登陆失败:账号或密码错误！-> {}",phone);
            return accountLockCheck.incrementAndCheckLock(identifier);
        }
        accountLockCheck.clearFailCount(identifier);
        UserVO userVO = setSession(user);
        return Result.success(userVO);
    }

    private Result UsernameLogin(String username, String password,String identifier) {
        if (StringUtils.isBlank(username)) {
            return Result.error("用户名不能为空!");
        }
        if (StringUtils.isBlank(password)) {
            return Result.error("密码不能为空!");
        }

        String dbUsername = UsernamePrifixConstant.USER_USERNAME_PRIFIX + username;
        User user = query().eq("username", dbUsername).one();
        if (user == null) {
            log.warn("登陆失败:用户不存在->{}",dbUsername);
            return accountLockCheck.incrementAndCheckLock(identifier);
        }
        String inputPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!inputPassword.equals(user.getPassword())) {
            log.warn("登陆失败:账号或密码错误! -> {}",dbUsername);
            return accountLockCheck.incrementAndCheckLock(identifier);
        }
        accountLockCheck.clearFailCount(identifier);
        UserVO userVO = setSession(user);
        return Result.success(userVO);
    }

    /**
     * 设置 Sa-Token 登录信息
     */
    private UserVO setSession(User user) {
        UserVO userVO = convertToVO(user);
        // 使用StpKit.USER进行登录，实现用户会话隔离
        com.yyblcc.ecommerceplatforms.util.StpKit.USER.login(user.getId());
        // 将用户信息和角色存储到对应的Session中
        com.yyblcc.ecommerceplatforms.util.StpKit.USER.getSession().set("USER", userVO);
        com.yyblcc.ecommerceplatforms.util.StpKit.USER.getSession().set("ROLE", RoleEnum.USER);
        com.yyblcc.ecommerceplatforms.util.StpKit.USER.getSession().set("USER_ID", user.getId());
        return userVO;
    }

    private UserVO convertToVO(User user) {
        return UserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .email(user.getEmail())
                .idNumber(user.getIdNumber())
                .sex(user.getSex())
                .defaultAddressId(user.getDefaultAddressId())
                .status(user.getStatus())
                .rejectReason(user.getRejectReason())
                .avatar(user.getAvatar())
                .createTime(user.getCreateTime())
                .build();
    }


}
