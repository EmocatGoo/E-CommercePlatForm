package com.yyblcc.ecommerceplatforms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.domain.DTO.WorkShopDTO;
import com.yyblcc.ecommerceplatforms.domain.Enum.WorkShopStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductListVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductVO;
import com.yyblcc.ecommerceplatforms.domain.VO.WorkShopVO;
import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.domain.query.PageQuery;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.CraftsmanMapper;
import com.yyblcc.ecommerceplatforms.mapper.UserCollectMapper;
import com.yyblcc.ecommerceplatforms.mapper.WorkShopMapper;
import com.yyblcc.ecommerceplatforms.service.CraftsmanService;
import com.yyblcc.ecommerceplatforms.service.ProductService;
import com.yyblcc.ecommerceplatforms.service.WorkShopService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkShopServiceImplement extends ServiceImpl<WorkShopMapper, WorkShop> implements WorkShopService {
    private final WorkShopMapper workShopMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CraftsmanService craftsmanService;
    private final UserCollectMapper userCollectMapper;
    private final ProductService productService;

    @Override
    public Result reviewWorkshop(Long workshopId, Integer status) {
        boolean success = new LambdaUpdateChainWrapper<>(workShopMapper).eq(WorkShop::getId, workshopId).set(WorkShop::getReviewStatus, status).update();

        if (success){
            try{
                return Result.success("更新成功");
            }catch (Exception e){
                throw new RuntimeException(e.getMessage());
            }
        }
        return Result.error("更新失败");
    }

    @Override
    public Result adminSetWorkShopStatus(Long workshopId, Integer status) {
        int row = workShopMapper.update(new LambdaUpdateWrapper<WorkShop>()
                .eq(WorkShop::getId, workshopId)
                .set(WorkShop::getStatus, status));
        if (row > 0){
            return Result.success("工作室状态已更新为：" + status);
        }
        return Result.error("发生错误!");
    }

    @Override
    public Result pageWorkShop(PageQuery query) {
        Page<WorkShop> workShopList = workShopMapper.selectPage(new Page<>(query.getPage(),query.getPageSize()),
                new LambdaQueryWrapper<WorkShop>()
                        .like(query.getKeyword() != null , WorkShop::getWorkshopName, query.getKeyword())
                        .orderByAsc(WorkShop::getId));
        List<WorkShopVO> workShopVOList = new ArrayList<>();

        workShopList.getRecords().forEach(workShop -> {
            WorkShopVO workShopVO = convertToVO(workShop);
            workShopVOList.add(workShopVO);
        });
        PageBean pageBean = new PageBean(workShopList.getTotal(),workShopVOList);

        return Result.success(pageBean);
    }

    @Override
    public Result getWorkShopByCraftsmanId(Long craftsmanId) {

        WorkShop workShop = query().eq("craftsman_id", craftsmanId).one();
        if (workShop == null){
            return Result.error("您还未创建工作室");
        }
        WorkShopVO workShopVO = convertToVO(workShop);
        return Result.success(workShopVO);
    }

    @Override
    public Result signUpWorkShop(Long craftsmanId, WorkShopDTO workShopDTO) {
        if (StringUtils.isBlank(workShopDTO.getWorkshopName())){
            return Result.error("工作室名称不能为空!");
        }

        String workshopStr = stringRedisTemplate.opsForValue().get("workshop:craftsman:" + craftsmanId);
        if (StringUtils.isNotBlank(workshopStr) && !workshopStr.isEmpty()){
            return Result.error("请勿重复申请！！！");
        }

        WorkShop existWorkShop = query().eq("craftsman_id", craftsmanId).one();
        if (existWorkShop != null){
            WorkShopVO workShopVO = convertToVO(existWorkShop);
            stringRedisTemplate.opsForValue().set("workshop:craftsman:"+existWorkShop.getCraftsmanId(),JSON.toJSONString(workShopVO),Duration.ofMinutes(10));
            return Result.error("请勿重复申请！！！");
        }
        WorkShop workShop = new WorkShop();
        BeanUtils.copyProperties(workShopDTO,workShop);
        workShop.setCraftsmanId(craftsmanId);
        workShop.setReviewStatus(WorkShopStatusEnum.PENDING.getCode());
        workShop.setCreateTime(LocalDateTime.now());
        save(workShop);
        return Result.success("工作室申请成功!请等待管理员审核!");
    }

    @Override
    public Result getWorkShopDetail(Long id) {
        WorkShop workShop = query().eq("id", id).one();
        if (workShop == null){
            return Result.error("未找到工作室信息");
        }

        WorkShopVO workShopVO = convertToVO(workShop);
        return Result.success(workShopVO);
    }

    @Override
    public Result visitWorkShop(Long id) {
        WorkShop workShop = query().eq("id", id).one();
        if (workShop == null){
            return Result.error("未找到工作室信息");
        }
        new LambdaUpdateChainWrapper<>(workShopMapper).eq(WorkShop::getId,id).set(WorkShop::getVisitCount,workShop.getVisitCount() + 1).update();
        return Result.success();
    }

    @Override
    @UpdateBloomFilter
    public Result collectWorkShop(Long workShopId) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        userCollectMapper.Collect(userId,workShopId);
        Integer finalStatus = userCollectMapper.getFinalStatus(userId,workShopId);
        int delta = (finalStatus == 1) ? 1 : -1;
        workShopMapper.update(null,new LambdaUpdateWrapper<WorkShop>()
                .eq(WorkShop::getId,workShopId)
                .setSql("collection_count = collection_count + " + delta));
        return Result.success(finalStatus == 1 ? "收藏成功" : "取消收藏");
    }

    @Override
    public Result getWorkShopStatus(Long craftsmanId) {
        Craftsman craftsman = craftsmanService.query().eq("id", craftsmanId).one();
        try{
            String statusStr = stringRedisTemplate.opsForValue().get("workshop:status:" + craftsman.getWorkshopId());
            if (statusStr != null){
                if (statusStr.isEmpty()){
                    return Result.error("未找到对应工作室");
                }
                return Result.success(Integer.parseInt(statusStr));
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        WorkShop workShop = query().eq("craftsman_id", craftsmanId).one();
        if (workShop != null){
            stringRedisTemplate.opsForValue().set("workshop:status:" + workShop.getId(),workShop.getStatus().toString(),Duration.ofMinutes(15));
            return Result.success(workShop.getStatus());
        }
        return Result.error("未找到对应工作室");
    }

    @Override
    public void viewWorkShop(Long id) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        try {
            String today = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String viewKey = "workshop:view:" + id + ":" + today;

            Boolean isMember = stringRedisTemplate.opsForSet().isMember(viewKey, userId.toString());
            if (Boolean.TRUE.equals(isMember)) {
                log.info("用户{}今天已查看过工作室{}", userId, id);
                return;
            }
            stringRedisTemplate.opsForSet().add(viewKey, userId.toString());
            long secondsUntilMidnight  = getSecondsUntilTomorrowMidnight();
            stringRedisTemplate.expire(viewKey,secondsUntilMidnight, TimeUnit.SECONDS);
            boolean success = update(new LambdaUpdateWrapper<WorkShop>()
                    .eq(WorkShop::getId, id)
                    .setSql("visit_count = visit_count + 1"));
            if (success) {
                log.info("工作室id为：{} 工作室浏览量+1", id);
            }
        }catch (Exception e){
            log.warn("记录工作室{}浏览量发生异常，用户{}：{}", id, userId, e.getMessage());
        }
    }

    @Override
    public Result<PageBean<WorkShopVO>> frontPage(PageQuery query) {
        List<WorkShopVO> list = workShopMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()),
                        new LambdaQueryWrapper<WorkShop>()
                                .like(query.getKeyword() != null, WorkShop::getWorkshopName, query.getKeyword())
                                .eq(WorkShop::getReviewStatus, WorkShopStatusEnum.APPROVE.getCode())
                                .orderByDesc(WorkShop::getVisitCount))
                .getRecords()
                .stream()
                .map(this::convertToVO)
                .toList();
        PageBean<WorkShopVO> pageBean = new PageBean<>((long)list.size(), list);
        return Result.success(pageBean);
    }

    @Override
    public Result<Boolean> checkCollect(Long workShopId) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        UserCollect userCollect = userCollectMapper.selectOne(
                new LambdaQueryWrapper<UserCollect>()
                        .eq(UserCollect::getUserId, userId)
                        .eq(UserCollect::getWorkShopId, workShopId));
        return Result.success(userCollect != null);
    }

    private long getSecondsUntilTomorrowMidnight(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, tomorrowMidnight).getSeconds() + 3600;
    }

    @Override
    public Result setWorkShopStatus(Long craftsmanId, Integer status) {
        WorkShop workShop = query().eq("craftsman_id", craftsmanId).one();
        if (workShop == null){
            return Result.error("未找到对应工作室");
        }
        boolean success = new LambdaUpdateChainWrapper<>(workShopMapper)
                .eq(WorkShop::getCraftsmanId, craftsmanId)
                .set(WorkShop::getStatus, status)
                .update();
        if (success){
            try{
                stringRedisTemplate.keys("workshop:page:*").forEach(stringRedisTemplate::delete);
                stringRedisTemplate.opsForValue().set("workshop:status:" + workShop.getId(),status.toString(),Duration.ofMinutes(15));
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
            return Result.success();
        }
        return null;
    }

    @Override
    public Result updateWorkShop(WorkShopDTO workShopDTO) {
        Long craftsmanId = AuthContext.getUserId();
        WorkShop workShop = query().eq("craftsman_id", craftsmanId).one();
        BeanUtils.copyProperties(workShopDTO,workShop);
        updateById(workShop);
        stringRedisTemplate.keys("workshop:page:*").forEach(stringRedisTemplate::delete);
        return Result.success("修改成功");

    }

    @Override
    public Result selectWorkShopName(String workshopName) {
        List<WorkShopVO> workShopList = query().like("workshop_name", workshopName).list().stream().map(this::convertToVO).toList();
        return Result.success(workShopList);
    }

    @Override
    public Result<String> updateMasterPieces(List<String> masterPieces) {
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        WorkShop workShop = query().eq("craftsman_id", craftsmanId).one();
        workShop.setMasterpieceCollection(masterPieces);
        if (updateById(workShop)) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    public WorkShopVO convertToVO(WorkShop workShop) {
        WorkShopVO workShopVO = new WorkShopVO();
        BeanUtils.copyProperties(workShop,workShopVO);
        Craftsman craftsman = craftsmanService.query().eq("id", workShop.getCraftsmanId()).one();
        workShopVO.setCraftsmanId(craftsman.getId());
        workShopVO.setCraftsmanName(craftsman.getName());
        workShopVO.setCraftsmanPhone(craftsman.getPhone());
        workShopVO.setCraftsmanEmail(craftsman.getEmail());
        List<Product> productList = productService.query().eq("craftsman_id", workShop.getCraftsmanId()).list();
        List<ProductVO> products = new ArrayList<>();
        productList.forEach(product -> {
            ProductVO productVO = new ProductVO();
            BeanUtils.copyProperties(product,productVO);
            productVO.setCraftsmanName(craftsman.getName());
            products.add(productVO);
        });
        workShopVO.setProducts(products);
        return workShopVO;
    }

}
