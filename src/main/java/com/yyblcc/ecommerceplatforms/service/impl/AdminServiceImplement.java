package com.yyblcc.ecommerceplatforms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.constant.PasswordConstant;
import com.yyblcc.ecommerceplatforms.constant.StatusConstant;
import com.yyblcc.ecommerceplatforms.constant.UsernamePrifixConstant;
import com.yyblcc.ecommerceplatforms.domain.DTO.AdminDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.LoginDTO;
import com.yyblcc.ecommerceplatforms.domain.Enum.RoleEnum;
import com.yyblcc.ecommerceplatforms.domain.VO.AdminVO;
import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.mapper.AdminMapper;
import com.yyblcc.ecommerceplatforms.mapper.CraftsmanMapper;
import com.yyblcc.ecommerceplatforms.mapper.WorkShopMapper;
import com.yyblcc.ecommerceplatforms.service.AdminService;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.service.WorkShopService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AdminServiceImplement extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final Long TTL = 10L;
    private static final String LOCK_KEY_PRIFIX = "login:lock:admin:";
    private static final String FAIL_KEY_PRIFIX = "login:fail:admin:";
    private static final Long LOCK_TTL = 60L;
    @Autowired
    private WorkShopService workShopService;
    @Autowired
    private WorkShopMapper workShopMapper;
    @Autowired
    private CraftsmanMapper craftsmanMapper;

    /**
     * 添加管理员
     * @param adminDTO
     * @return
     */
    @Override
    @UpdateBloomFilter
    public Result<?> saveAdmin(AdminDTO adminDTO) {
        if ("".equals(adminDTO.getUsername())){
            return Result.error("账号为空!");
        }
        String username = UsernamePrifixConstant.ADMIN_USERNAME_PRIFIX + adminDTO.getUsername();
        Admin existing = query().eq("username", username).one();
        // TODO 考虑是否添加随机数解决账号相同问题
        if (existing != null) {
            return Result.error("此账号已存在!");
        }
        Admin admin =Admin.builder()
                //设置账号固定前缀
                .username(username)
                //设置账号为启用
                .status(StatusConstant.ENABLE)
                //设置管理员姓名
                .name(adminDTO.getName())
                //设置默认密码为123456
                .password(DigestUtils.md5DigestAsHex(PasswordConstant.PASSWORD.getBytes(StandardCharsets.UTF_8)))
                //设置管理员权限
                .role(RoleEnum.ADMIN)
                //设置注册时间
                .createTime(LocalDateTime.now())
                .build();
        save(admin);
        try {
            Set<String> keys = stringRedisTemplate.keys("admin:page:*");
            keys.forEach(key -> {stringRedisTemplate.delete(key);});
        }catch (Exception e){
            log.error(e.getMessage());
            return Result.error("未找到相关key");
        }
        return Result.success("注册成功!");
    }

    /**
     * 设置普通管理员状态
     * @param status
     * @param id
     * @return
     */
    @Override
    public Result<?> updateAdminStatus(Integer status,Long id) {
        Admin exist = query().eq("id", id).one();
        if (exist == null) {
            return Result.error("未找到用户!");
        }
        Admin admin = Admin.builder()
                .id(id)
                .status(status)
                .build();
        adminMapper.update(admin);
        try {
            Set<String> keys = stringRedisTemplate.keys("admin:page:*");
            keys.forEach(key -> {stringRedisTemplate.delete(key);});
        }catch (Exception e){
            log.error(e.getMessage());
            return Result.error("未找到相关key");
        }
        return Result.success("设置成功!");
    }

    /**
     * 更新管理员信息
     * @param adminDTO
     * @return
     */
    @Override
    public Result<?> updateAdmin(AdminDTO adminDTO) {
        String username = UsernamePrifixConstant.ADMIN_USERNAME_PRIFIX + adminDTO.getUsername();
        if ("".equals(username)){
            return Result.error("账号为空！更新失败！");
        }
        String name = adminDTO.getName();
        Admin admin = query().eq("username", username).one();
        if (admin == null) {
            return Result.error("未找到用户！");
        }
        admin.setName(name);
        adminMapper.update(admin);
        try {
            Set<String> keys = stringRedisTemplate.keys("admin:page:*");
            keys.forEach(key -> {stringRedisTemplate.delete(key);});
        }catch (Exception e){
            log.error(e.getMessage());
            return Result.error("未找到相关key");
        }
        return Result.success("更新成功!");
    }

    /**
     * 删除指定管理员
     * @param id
     * @return
     */
    @Override
    public Result<?> remove(Long id) {
        Admin admin = query().eq("id", id).one();
        if (admin == null) {
            return Result.error("未找到用户！");
        }
        adminMapper.deleteById(id);
        try {
            Set<String> keys = stringRedisTemplate.keys("admin:page:*");
            keys.forEach(key -> {stringRedisTemplate.delete(key);});
        }catch (Exception e){
            log.error(e.getMessage());
            return Result.error("未找到相关key");
        }
        return Result.success("删除成功！");
    }

    /**
     * 批量删除管理员
     * @param ids
     * @return
     */
    @Override
    public Result<?> batchRemove(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("ID 列表不能为空");
        }
        List<Long> existIdList = adminMapper.selectBatchIds(ids).stream().map(Admin::getId).toList();
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

        adminMapper.deleteBatchIds(ids);
        try {
            Set<String> keys = stringRedisTemplate.keys("admin:page:*");
            keys.forEach(key -> {stringRedisTemplate.delete(key);});
        }catch (Exception e){
            log.error(e.getMessage());
            return Result.error("未找到相关key");
        }
        return Result.success("批量删除成功!");
    }

    /**
     * 查询所有管理员信息
     * @return
     */
    @Override
    public Result<?> getAdmins() {
        List<Admin> adminList = adminMapper.selectList(null);
        List<AdminVO> adminVOs = new ArrayList<>();
        for (Admin admin : adminList) {
            AdminVO adminVO = new AdminVO();
            BeanUtils.copyProperties(admin, adminVO);
            adminVOs.add(adminVO);
        }
        return Result.success(adminVOs);
    }

    /**
     * 分页查询管理员信息
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Result<PageBean> page(Integer page, Integer pageSize){
        String key = "admin:page:" + page + ":" +pageSize;
        try{
            String jsonStr = stringRedisTemplate.opsForValue().get(key);
            if (jsonStr != null){
                return Result.success(JSON.parseObject(jsonStr, PageBean.class));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Result.error(e.getMessage());
        }

        Page<Admin> adminPage = new Page<>(page, pageSize);
        Page<Admin> pageList = adminMapper.selectPage(adminPage, null);

        List<AdminVO> adminVOs = pageList.getRecords().stream().map(this::convertToVO).toList();

        PageBean pageBean = new PageBean(pageList.getTotal(), adminVOs);

        stringRedisTemplate.opsForValue().set(key,JSON.toJSONString(pageBean),TTL, TimeUnit.MINUTES);
        return Result.success(pageBean);
    }

    @Override
    public Result login(LoginDTO loginDTO, HttpServletRequest request) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 1. 检查是否被锁定
        String lockKey = LOCK_KEY_PRIFIX + username;
        String locked = stringRedisTemplate.opsForValue().get(lockKey);
        if (locked != null) {
            return Result.error("账号已锁定，请稍后再试！");
        }

        // 2. 校验用户名
        if (StringUtils.isBlank(username)) {
            return Result.error("未输入用户名！");
        }
        if (StringUtils.isBlank(password)) {
            return Result.error("未输入密码！");
        }

        // 3. 查询用户
        String dbUsername = UsernamePrifixConstant.ADMIN_USERNAME_PRIFIX + username;
        Admin admin = query().eq("username", dbUsername).one();
        if (admin == null) {
            log.warn("登录失败：用户不存在 -> {}",dbUsername);
            return tryLock(username);
        }

        // 4. 校验密码
        String inputMd5 = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!inputMd5.equals(admin.getPassword())) {
            log.warn("登录失败：密码错误 -> {}", dbUsername);
            return tryLock(username);
        }

        AdminVO adminVO = convertToVO(admin);
        // 5. 登录成功 清除失败记录
        clearFailCount(username);
        setSession(request, admin);
        return Result.success(adminVO);
    }

    @Override
    public Result resetPassword(Long adminId) {
        //TODO 记得添加缓存
        Admin admin = query().eq("id", adminId).one();
        if (admin == null) {
            return Result.error("没有找到对应管理员!");
        }
        admin.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.PASSWORD.getBytes(StandardCharsets.UTF_8)));
        adminMapper.update(admin);
        try {
            Set<String> keys = stringRedisTemplate.keys("admin:page:*");
            keys.forEach(key -> {stringRedisTemplate.delete(key);});
        }catch (Exception e){
            log.error(e.getMessage());
            return Result.error("未找到相关key");
        }
        return Result.success("重置密码成功!");
    }

    @Override
    public Result getAdminByName(String name, Integer page, Integer pageSize) {
        Page<Admin> adminPage = new Page<>(page, pageSize);
        Page<Admin> pageList = adminMapper.selectPage(adminPage,new LambdaQueryWrapper<Admin>()
                .like(Admin::getName,name)
                .orderByDesc(Admin::getCreateTime));
        List<AdminVO> adminVOs = pageList.getRecords().stream().map(this::convertToVO).toList();
        PageBean pageBean = new PageBean(pageList.getTotal(), adminVOs);
        return Result.success(pageBean);
    }

    @Override
    public Result pageReview(Integer page, Integer pageSize) {
        Page<Craftsman> craftsmanPage = new Page<>(page, pageSize);
        Page<Craftsman> craftsmanList = craftsmanMapper.selectPage(craftsmanPage,
                new LambdaQueryWrapper<Craftsman>()
                        .eq(Craftsman::getReviewStatus,0)
                        .orderByDesc(Craftsman::getCreateTime));


        return null;
    }


    private AdminVO convertToVO(Admin admin) {
        return AdminVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .name(admin.getName())
                .avatar(admin.getAvatar())
                .role(admin.getRole())
                .build();
    }

    /**
     * 记录一次登录失败，5 次后锁定 60 秒
     */
    private Result tryLock(String username) {
        String failKey = FAIL_KEY_PRIFIX + username;
        String lockKey = LOCK_KEY_PRIFIX + username;

        Long failCount = stringRedisTemplate.opsForValue().increment(failKey);
        if (failCount == null){
            failCount = 1L;
        }
        if (failCount >= 5) {
            stringRedisTemplate.opsForValue().set(lockKey, "1", LOCK_TTL, TimeUnit.SECONDS);

            stringRedisTemplate.delete(failKey);
            return Result.error("错误次数过多，账号已被锁定");
        } else {
            stringRedisTemplate.expire(failKey, LOCK_TTL, TimeUnit.SECONDS);
            return Result.error("用户名或密码错误！");
        }
    }

    /**
     * 清除失败计数
     */
    private void clearFailCount(String username) {
        String failKey = FAIL_KEY_PRIFIX + username;
        stringRedisTemplate.delete(failKey);
    }

    /**
     * 设置 Session
     */
    private void setSession(HttpServletRequest request, Admin admin) {
        // 使用StpKit.ADMIN进行登录，实现管理员会话隔离
        com.yyblcc.ecommerceplatforms.util.StpKit.ADMIN.login(admin.getId());
        // 将用户信息和角色存储到对应的Session中
        com.yyblcc.ecommerceplatforms.util.StpKit.ADMIN.getSession().set("USER", admin);
        com.yyblcc.ecommerceplatforms.util.StpKit.ADMIN.getSession().set("ROLE", admin.getRole());
        com.yyblcc.ecommerceplatforms.util.StpKit.ADMIN.getSession().set("USER_ID", admin.getId());
    }


}
