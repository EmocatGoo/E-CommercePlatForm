package com.yyblcc.ecommerceplatforms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.DTO.WorkShopDTO;
import com.yyblcc.ecommerceplatforms.domain.Enum.WorkShopStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.VO.WorkShopVO;
import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.mapper.WorkShopMapper;
import com.yyblcc.ecommerceplatforms.service.CraftsmanService;
import com.yyblcc.ecommerceplatforms.service.WorkShopService;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class WorkShopServiceImplement extends ServiceImpl<WorkShopMapper, WorkShop> implements WorkShopService {

    @Autowired
    private WorkShopMapper workShopMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CraftsmanService craftsmanService;

    @Override
    public Result reviewWorkshop(Long workshopId, Integer status) {
        boolean success = new LambdaUpdateChainWrapper<>(workShopMapper).eq(WorkShop::getId, workshopId).set(WorkShop::getReviewStatus, status).update();

        if (success){
            try{
                Set<String> keys = stringRedisTemplate.keys("workshop:*");
                keys.forEach(key -> stringRedisTemplate.delete(key));
                return Result.success("更新成功!");
            }catch (Exception e){
                throw new RuntimeException(e.getMessage());
            }
        }
        return Result.error("更新失败");
    }

    @Override
    public Result banWorkshop(Long workshopId) {
        boolean success = new LambdaUpdateChainWrapper<>(workShopMapper).eq(WorkShop::getId, workshopId).set(WorkShop::getStatus, 0).update();
        if (success){
            Set<String> keys = stringRedisTemplate.keys("workshop:*");
            keys.forEach(key ->{
                stringRedisTemplate.delete(key);
            });
            return Result.success("工作室已被关闭");
        }
        return Result.error("发生错误!");
    }

    @Override
    public Result pageWorkShop(Integer page, Integer pageSize) {
        String key = "workshop:page:" + page + ":" +pageSize;
        String workshopStr = stringRedisTemplate.opsForValue().get(key);
        if (workshopStr != null){
            return Result.success(JSON.parseObject(workshopStr, PageBean.class));
        }
        Page<WorkShop> workShopPage = new Page<>(page,pageSize);
        Page<WorkShop> workShopList = workShopMapper.selectPage(workShopPage,null);
        List<WorkShopVO> workShopVOList = new ArrayList<>();

        workShopList.getRecords().forEach(workShop -> {
            Long craftsmanId = workShop.getCraftsmanId();
            Craftsman craftsman = craftsmanService.query().eq("id", craftsmanId).one();
            WorkShopVO workShopVO = convertToVO(workShop);
            workShopVO.setCraftsmanName(craftsman.getName());
            workShopVO.setCraftsmanPhone(craftsman.getPhone());
            workShopVO.setCraftsmanEmail(craftsman.getEmail());
            workShopVOList.add(workShopVO);
        });

        stringRedisTemplate.opsForValue().set(key,JSON.toJSONString(workShopVOList),10, TimeUnit.MINUTES);
        PageBean pageBean = new PageBean(workShopList.getTotal(),workShopVOList);

        return Result.success(pageBean);
    }

    @Override
    public Result getWorkShopByCraftsmanId(Long craftsmanId) {
        try{
            String workShopStr = stringRedisTemplate.opsForValue().get("workshop:craftsmanId:" + craftsmanId);
            if (workShopStr != null){
                if ("".equals(workShopStr)){
                    return Result.error("您还未创建工作室!");
                }
                return Result.success(JSON.parseObject(workShopStr,WorkShopVO.class));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Result.error(e.getMessage());
        }
        WorkShop workShop = query().eq("craftsman_id", craftsmanId).one();
        if (workShop == null){
            stringRedisTemplate.opsForValue().set("workshop:craftsmanId:"+craftsmanId,"null", Duration.ofMinutes(5));
            return Result.error("您还未创建工作室!");
        }
        WorkShopVO workShopVO = convertToVO(workShop);
        Craftsman craftsman = craftsmanService.query().eq("id", workShop.getCraftsmanId()).one();
        workShopVO.setCraftsmanName(craftsman.getName());
        workShopVO.setCraftsmanPhone(craftsman.getPhone());
        workShopVO.setCraftsmanEmail(craftsman.getEmail());
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
            return Result.error("未找到工作室信息!");
        }
        return Result.success(workShop);
    }

    @Override
    public Result visitWorkShop(Long id) {
        WorkShop workShop = query().eq("id", id).one();
        if (workShop == null){
            return Result.error("未找到工作室信息!");
        }
        new LambdaUpdateChainWrapper<>(workShopMapper).eq(WorkShop::getId,id).set(WorkShop::getVisitCount,workShop.getVisitCount() + 1).update();
        return Result.success();
    }

    @Override
    public Result setWorkShopStatus(Long craftsmanId, Integer status) {
        WorkShop workShop = query().eq("craftsman_id", craftsmanId).one();
        if (workShop == null){
            return Result.error("未找到对应工作室!");
        }
        boolean success = new LambdaUpdateChainWrapper<>(workShopMapper).eq(WorkShop::getCraftsmanId, craftsmanId).set(WorkShop::getStatus, status).update();
        if (success){
            stringRedisTemplate.keys("workshop:page:*").forEach(key -> stringRedisTemplate.delete(key));
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
        stringRedisTemplate.keys("workshop:page:*").forEach(key -> stringRedisTemplate.delete(key));
        return Result.success("修改成功!");

    }

    @Override
    public Result selectWorkShopName(String workshopName) {
        List<WorkShopVO> workShopList = query().like("workshop_name", workshopName).list().stream().map(this::convertToVO).toList();
        return Result.success(workShopList);
    }

    public WorkShopVO convertToVO(WorkShop workShop) {
        return WorkShopVO.builder()
                .id(workShop.getId())
                .workshopName(workShop.getWorkshopName())
                .workshopLogo(workShop.getWorkshopLogo())
                .location(workShop.getLocation())
                .status(workShop.getStatus())
                .reviewStatus(workShop.getReviewStatus())
                .story(workShop.getStory())
                .build();
    }

}
