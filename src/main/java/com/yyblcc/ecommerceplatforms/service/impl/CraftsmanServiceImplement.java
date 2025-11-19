package com.yyblcc.ecommerceplatforms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.constant.PasswordConstant;
import com.yyblcc.ecommerceplatforms.constant.StatusConstant;
import com.yyblcc.ecommerceplatforms.constant.UsernamePrifixConstant;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.Enum.RoleEnum;
import com.yyblcc.ecommerceplatforms.domain.Enum.WorkShopStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.VO.CraftsmanVO;
import com.yyblcc.ecommerceplatforms.domain.VO.WorkShopVO;
import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.domain.query.CraftsmanQuery;
import com.yyblcc.ecommerceplatforms.mapper.CraftsmanMapper;
import com.yyblcc.ecommerceplatforms.mapper.WorkShopMapper;
import com.yyblcc.ecommerceplatforms.service.CraftsmanAuthService;
import com.yyblcc.ecommerceplatforms.service.CraftsmanService;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.service.WorkShopService;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import com.yyblcc.ecommerceplatforms.util.redis.AccountLockCheck;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author YuYiBlackcat
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CraftsmanServiceImplement extends ServiceImpl<CraftsmanMapper, Craftsman> implements CraftsmanService {

    private final CraftsmanMapper craftsmanMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final AccountLockCheck accountLockCheck;
    private static final Long TTL = 10L;
    private final WorkShopService workShopService;
    private final WorkShopServiceImplement workShopServiceImplement;
    private final WorkShopMapper workShopMapper;
    private final CraftsmanAuthService craftsmanAuthService;

    /**
     * 匠人注册
     * @param craftsmanDTO
     * @return
     */
    @Override
    @UpdateBloomFilter
    public Result<?> saveCraftsman(CraftsmanDTO craftsmanDTO) {
        if ("".equals(craftsmanDTO.getUsername())){
            return Result.error("账号为空!");
        }
        String username = UsernamePrifixConstant.CRAFTSMAN_USERNAME_PRIFIX + craftsmanDTO.getUsername();
        Craftsman existing = query().eq("username", username).one();
        // TODO 考虑是否添加随机数解决账号相同问题
        if (existing != null) {
            return Result.error("此账号已存在!");
        }
        Craftsman craftsman =Craftsman.builder()
                //设置账号固定前缀
                .username(username)
                //设置匠人名称
                .name(craftsmanDTO.getName())
                //设置账号为待审核
                .status(StatusConstant.WAITING_REVIEW)
                //设置密码
                .password(DigestUtils.md5DigestAsHex(craftsmanDTO.getPassword().getBytes(StandardCharsets.UTF_8)))
                //设置注册时间
                .createTime(LocalDateTime.now())
                //设置最后操作时间
                .updateTime(LocalDateTime.now())
                .build();
        save(craftsman);
        return Result.success("注册成功!");
    }

    /**
     * 设置匠人认证状态
     * @param status
     * @param id
     * @return
     */
    @Override
    public Result<?> updateCraftsmanStatus(Integer status,Long id) {
        Craftsman exist = query().eq("id", id).one();
        if (exist == null) {
            return Result.error("未找到用户!");
        }
        Craftsman craftsman = Craftsman.builder()
                .id(id)
                .status(status)
                .build();
        craftsmanMapper.update(craftsman);
        Set<String> keys = stringRedisTemplate.keys("craftsman:*");
        keys.forEach(key -> {stringRedisTemplate.delete(key);});
        return Result.success("设置成功!");
    }

    /**
     * 更新匠人信息
     *
     * @param craftsmanDTO
     * @param request
     * @return
     */
    @Override
    public Result<?> updateCraftsman(CraftsmanDTO craftsmanDTO, HttpServletRequest request) {
        //TODO 记得替换为getAttribute or 启动拦截器获取上下文
        Long craftsmanId = AuthContext.getUserId();
        Craftsman craftsman = query().eq("id", craftsmanId).one();
        if (craftsman == null) {
            return Result.error("未找到用户！");
        }
        BeanUtils.copyProperties(craftsmanDTO,craftsman);
        craftsman.setUpdateTime(LocalDateTime.now());
        craftsmanMapper.update(craftsman);
        Set<String> keys = stringRedisTemplate.keys("craftsman:*");
        keys.forEach(key -> {stringRedisTemplate.delete(key);});
        return Result.success("更新成功!");
    }

    /**
     * 匠人注销
     * @param id
     * @return
     */
    @Override
    public Result<?> remove(Long id) {
        Craftsman craftsman = query().eq("id", id).one();
        if (craftsman == null) {
            return Result.error("未找到用户！");
        }
        craftsmanMapper.deleteById(id);
        return Result.success("删除成功！");
    }

    /**
     * 匠人批量注销
     * @param ids
     * @return
     */
    @Override
    public Result<?> batchRemove(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("ID 列表不能为空");
        }
        List<Long> existIdList = craftsmanMapper.selectBatchIds(ids).stream().map(Craftsman::getId).toList();
        // 检查是否存在无效的 ID
        List<Long> invalidIds = new ArrayList<>();
        for (Long id : ids) {
            if (!existIdList.contains(id)) {
                invalidIds.add(id);
            }
        }
        if (!invalidIds.isEmpty()) {
            return Result.error("以下 ID 对应的记录不存在：" + invalidIds + "，无法进行删除操作");
        }

        craftsmanMapper.deleteBatchIds(ids);
        return Result.success("批量删除成功!");
    }

    /**
     * 查询所有匠人信息
     * @return
     */
    @Override
    public Result<?> getCraftsman() {
        List<Craftsman> craftsmenList = craftsmanMapper.selectList(null);
        List<CraftsmanVO> craftsmanVOList = new ArrayList<>();
        for (Craftsman craftsman : craftsmenList) {
            CraftsmanVO craftsmanVO = new CraftsmanVO();
            BeanUtils.copyProperties(craftsman, craftsmanVO);
            craftsmanVOList.add(craftsmanVO);
        }
        return Result.success(craftsmanVOList);
    }

    /**
     * 分页查询匠人信息
     * @param craftsmanQuery
     * @return
     */
    @Override
    public Result<PageBean> page(CraftsmanQuery craftsmanQuery){
        String key = "craftsman:page:" + craftsmanQuery.getPage() + ":" +craftsmanQuery.getPageSize();
        try{
            String jsonStr = stringRedisTemplate.opsForValue().get(key);
            if (jsonStr != null){
                return Result.success(JSON.parseObject(jsonStr, PageBean.class));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Result.error(e.getMessage());
        }

        Page<Craftsman> craftsmanPage = new Page<>(craftsmanQuery.getPage(), craftsmanQuery.getPageSize());
        Page<Craftsman> pageList = craftsmanMapper.selectPage(craftsmanPage, null);

        List<CraftsmanVO> craftsmanVOList = pageList.getRecords().stream().map(this::convertToVO).toList();

        PageBean<CraftsmanVO> pageBean = new PageBean<>(pageList.getTotal(), craftsmanVOList);

        stringRedisTemplate.opsForValue().set(key,JSON.toJSONString(pageBean),TTL, TimeUnit.MINUTES);
        return Result.success(pageBean);
    }



    @Override
    public Result<?> getProfile(Long craftsmanId) {
        if (craftsmanId == null){
            return Result.error("出现查询异常!");
        }
        String key = "craftsman:profile:" + craftsmanId;
        try{
            String craftsmanStr = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(craftsmanStr) && !("null".equals(craftsmanStr))){
                return Result.success(JSON.parseObject(craftsmanStr, CraftsmanVO.class));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Result.error(e.getMessage());
        }
        Craftsman craftsman = query().eq("id", craftsmanId).one();
        if (craftsman == null){
            stringRedisTemplate.opsForValue().set(key,"null", Duration.ofMinutes(10));
            return Result.error("未找到匠人信息!");
        }
        CraftsmanVO craftsmanVO = convertToVO(craftsman);
        return Result.success(craftsmanVO);
    }

    @Override
    public Result<?> updatePassword(PasswordDTO passwordDTO, HttpServletRequest request) {
        Long craftsmanId = Long.valueOf(request.getHeader("USER_ID"));
        if (!Objects.equals(RoleEnum.CRAFTSMAN.toString(), request.getHeader("ROLE"))){
            return Result.error("未找到匠人信息!");
        }
        try{
            String craftsmanStr = stringRedisTemplate.opsForValue().get("craftsman:profile:" + craftsmanId);
            if ("null".equals(craftsmanStr)){
                return Result.error("未找到匠人信息!");
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Result.error(e.getMessage());
        }
        String oldPassword = passwordDTO.getOldPassword();
        String newPassword = passwordDTO.getNewPassword();
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)){
            return Result.error("密码不能为空!");
        }
        Craftsman craftsman = query().eq("id", craftsmanId).one();
        if (craftsman == null){
            stringRedisTemplate.opsForValue().set("craftsman:profile:" + craftsmanId,"null",Duration.ofMinutes(10));
            return Result.error("未找到匠人信息!");
        }
        craftsman.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8)));
        craftsmanMapper.update(craftsman);
        Set<String> keys = stringRedisTemplate.keys("craftsman:*");
        keys.forEach(key -> {stringRedisTemplate.delete(key);});
        return Result.success("密码修改成功!");
    }

    @Override
    public Result<?> updateCraftsmanReviewStatus(Integer reviewStatus, Long id) {
        Craftsman exist = query().eq("id", id).one();
        if (exist == null) {
            return Result.error("未找到用户!");
        }
        Craftsman craftsman = Craftsman.builder()
                .id(id)
                .reviewStatus(reviewStatus)
                .build();
        craftsmanMapper.update(craftsman);
        Set<String> keys = stringRedisTemplate.keys("craftsman:*");
        keys.forEach(key -> {stringRedisTemplate.delete(key);});
        return Result.success("设置成功!");
    }

    @Override
    public Result resetPassword(Long craftsmanId) {
        Craftsman craftsman = query().eq("id", craftsmanId).one();
        if (craftsman == null){
            return Result.error("查询异常!");
        }
        craftsman.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.PASSWORD.getBytes(StandardCharsets.UTF_8)));
        craftsmanMapper.update(craftsman);
        stringRedisTemplate.keys("craftsman:*").forEach(key -> {stringRedisTemplate.delete(key);});
        return Result.success("密码重置成功!");
    }

    @Override
    public Result nameSelect(CraftsmanQuery craftsmanQuery) {
        String name = craftsmanQuery.getName();
        Page<Craftsman> page = new Page<>(craftsmanQuery.getPage(), craftsmanQuery.getPageSize());
        Page<Craftsman> craftsmanPage = craftsmanMapper.selectPage(page,
                new LambdaQueryWrapper<Craftsman>()
                        .like(Craftsman::getName,name)
                        .orderByDesc(Craftsman::getCreateTime));

        List<CraftsmanVO> craftsmanVOList = craftsmanPage.getRecords().stream().map(this::convertToVO).toList();
        PageBean pageBean = new PageBean(craftsmanPage.getTotal(),craftsmanVOList);
        return Result.success(pageBean);
    }



    @Override
    public Result<?> checkCraftsmanInfo(String username, String phone) {
        String checkDbUsername = UsernamePrifixConstant.CRAFTSMAN_USERNAME_PRIFIX + username;
        if (query().eq("username", checkDbUsername).one() != null){
            return Result.error("该用户名已被注册!");
        } else if (query().eq("phone", phone).one() != null) {
            return Result.error("改手机号已被注册!");
        }
        return Result.success();
    }

    @Override
    public Result<?> checkEmail(String email) {
        if (query().eq("email",email).one() != null) {
            return Result.error("该邮箱已被注册!");
        }
        return Result.success();
    }

    @Override
    public Result signUpAuth(CraftsmanAuthDTO craftsmanAuthDTO) {
        Long craftsmanId = AuthContext.getUserId();
        craftsmanAuthDTO.setCraftsmanId(craftsmanId);
        if (craftsmanAuthService.save(craftsmanAuthDTO)){
            return Result.success("提交成功，请等待管理员审核!");
        }
        return Result.error("提交失败，请检查提交材料是否符合要求!");
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
            return UsernameLogin(username, password, identifier,request);
        } else if (loginDTO.getLoginType() == 2) {
            return PhoneLogin(phone, password, identifier,request);
        }
        return Result.error("不支持的登陆类型!");
    }

    private Result PhoneLogin(String phone, String password,String identifier, HttpServletRequest request) {
        if (StringUtils.isBlank(phone)) {
            return Result.error("手机号码不能为空!");
        }
        if (StringUtils.isBlank(password)) {
            return Result.error("密码不能为空!");
        }
        Craftsman craftsman = query().eq("phone", phone).one();
        if (craftsman == null) {
            log.warn("登陆失败:用户不存在->{}", phone);
            return accountLockCheck.incrementAndCheckLock(identifier);
        }
        String inputPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!inputPassword.equals(craftsman.getPassword())){
            log.warn("登陆失败:账号或密码错误！-> {}",phone);
            return accountLockCheck.incrementAndCheckLock(identifier);
        }
        accountLockCheck.clearFailCount(identifier);
        setSession(request,craftsman);
        return Result.success("登陆成功!");
    }

    private Result UsernameLogin(String username, String password,String identifier,HttpServletRequest request) {
        if (StringUtils.isBlank(username)) {
            return Result.error("用户名不能为空!");
        }
        if (StringUtils.isBlank(password)) {
            return Result.error("密码不能为空!");
        }

        String dbUsername = UsernamePrifixConstant.CRAFTSMAN_USERNAME_PRIFIX + username;
        Craftsman craftsman = query().eq("username", dbUsername).one();
        if (craftsman == null) {
            log.warn("登陆失败:用户不存在->{}",dbUsername);
            return accountLockCheck.incrementAndCheckLock(identifier);
        }
        String inputPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!inputPassword.equals(craftsman.getPassword())) {
            log.warn("登陆失败:账号或密码错误! -> {}",dbUsername);
            return accountLockCheck.incrementAndCheckLock(identifier);
        }
        accountLockCheck.clearFailCount(identifier);
        setSession(request,craftsman);
        return Result.success("登陆成功!");
    }

    private CraftsmanVO convertToVO(Craftsman craftsman) {
        return CraftsmanVO.builder()
                .id(craftsman.getId())
                .username(craftsman.getUsername())
                .phone(craftsman.getPhone())
                .name(craftsman.getName())
                .reviewStatus(craftsman.getReviewStatus())
                .status(craftsman.getStatus())
                .introduction(craftsman.getIntroduction())
                .email(craftsman.getEmail())
                .avatar(craftsman.getAvatar())
                .createTime(craftsman.getCreateTime())
                .build();
    }

    /**
     * 设置 Session
     */
    private void setSession(HttpServletRequest request, Craftsman craftsman) {
        HttpSession session = request.getSession(true);
        CraftsmanVO craftsmanVO = convertToVO(craftsman);
        session.setAttribute("USER", craftsmanVO);
        session.setAttribute("ROLE", RoleEnum.CRAFTSMAN);
        session.setAttribute("USER_ID", craftsman.getId());
        session.setMaxInactiveInterval(30 * 60);
    }

}
