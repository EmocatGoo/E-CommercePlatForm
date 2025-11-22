package com.yyblcc.ecommerceplatforms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventApplyDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.EventApplyVO;
import com.yyblcc.ecommerceplatforms.domain.VO.EventVO;
import com.yyblcc.ecommerceplatforms.domain.po.Event;
import com.yyblcc.ecommerceplatforms.domain.po.EventApply;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.EventQuery;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.EventApplyMapper;
import com.yyblcc.ecommerceplatforms.mapper.EventMapper;
import com.yyblcc.ecommerceplatforms.service.EventService;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImplement extends ServiceImpl<EventMapper, Event> implements EventService {
    private final EventMapper eventMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final EventApplyMapper eventApplyMapper;
    private final RocketMQTemplate rocketMQTemplate;


    @Override
    @UpdateBloomFilter
    public Result saveEvent(EventDTO dto) {
        Event event = new Event();
        BeanUtils.copyProperties(dto, event);
        event.setCreateTime(LocalDateTime.now());
        event.setUpdateTime(LocalDateTime.now());
        event.setStatus(0);
        save(event);
        return Result.success("活动添加成功");
    }

    @Override
    public Result updateEvent(EventDTO dto) {
        Event event = new Event();
        BeanUtils.copyProperties(dto, event);
        eventMapper.updateById(event);
        stringRedisTemplate.keys("event:page").forEach(stringRedisTemplate::delete);
        return Result.success("修改成功");
    }

    @Override
    public Result getDetail(Long id) {
        Event event = query().eq("id", id).one();
        if (event == null) {
            return Result.error("没有找到对应活动");
        }
        EventVO vo = new EventVO();
        BeanUtils.copyProperties(event, vo);
        return Result.success(vo);
    }

    @Override
    public Result pageList(EventQuery eventQuery) {
        String key = "event:page:" + eventQuery.getPage() + ":" + eventQuery.getPageSize();

        try{
            String cacheStr = stringRedisTemplate.opsForValue().get(key);
            if (cacheStr != null) {
                if (cacheStr.isEmpty()) {
                    return Result.success();
                }
                return Result.success(JSON.parseObject(cacheStr, PageBean.class));
            }
        }catch (Exception e){
            throw new BusinessException(e.getMessage());
        }

        Page<Event> eventPage = eventMapper.selectPage(new Page<>(eventQuery.getPage(),eventQuery.getPageSize()),
                new LambdaQueryWrapper<Event>()
                        .like(eventQuery.getTitle() != null, Event::getTitle,eventQuery.getTitle())
                        .eq(eventQuery.getStatus() != null, Event::getStatus, eventQuery.getStatus())
                        .orderByDesc(Event::getCreateTime)
                        .last("FOR UPDATE"));

        List<EventVO> voList = eventPage.getRecords().stream().map(this::convertToEventVO).toList();
        PageBean<EventVO> pageBean = new PageBean<>(eventPage.getTotal(),voList);
        stringRedisTemplate.opsForValue().set(key,JSON.toJSONString(pageBean), Duration.ofMinutes(15));
        return Result.success(pageBean);
    }

    @Override
    //TODO 如果活动有人数限制需要加锁抢名额
    public Result apply(EventApplyDTO dto, Long userId) {
        Long eventId = dto.getEventId();
        Event event = query().eq("id", eventId).one();

        if (event == null) {
            throw new BusinessException("没有找到对应活动");
        }

        EventApply apply = eventApplyMapper.selectOne(new LambdaQueryWrapper<EventApply>()
                .eq(EventApply::getEventId, eventId)
                .eq(EventApply::getUserId, userId)
                .orderByDesc(EventApply::getCreateTime)
                .last("FOR UPDATE"));

        if (apply != null) {
            throw new BusinessException("已报名，请勿重复操作");
        }

        EventApply eventApply = new EventApply();
        BeanUtils.copyProperties(dto, eventApply);
        eventApply.setCreateTime(LocalDateTime.now());
        eventApply.setStatus(1);
        eventApplyMapper.insert(eventApply);

        eventMapper.update(new LambdaUpdateWrapper<Event>().eq(Event::getId, eventId).setSql("people_num = people_num + 1"));

        return Result.success("报名成功");
    }

    @Override
    public Result signIn(Long eventId) {
        Long userId = AuthContext.getUserId();
        EventApply eventApply = eventApplyMapper.selectOne(new LambdaQueryWrapper<EventApply>()
                .eq(EventApply::getEventId, eventId)
                .eq(EventApply::getUserId, userId)
                .orderByDesc(EventApply::getCreateTime)
                .last("FOR UPDATE"));
        if (eventApply == null) {
            throw new BusinessException("未找到对应报名信息，签到失败");
        }

        eventApply.setSignTime(LocalDateTime.now());
        eventApplyMapper.updateById(eventApply);
        return Result.success("签到成功");
    }

    @Override
    public Result getApplyList(Long eventId) {
        List<EventApplyVO> applyVoList = eventApplyMapper.selectList(new LambdaQueryWrapper<EventApply>()
                .eq(EventApply::getEventId, eventId)
                .orderByDesc(EventApply::getCreateTime)
                .last("FOR UPDATE")).stream().map(this::convertToEventApplyVO).toList();
        if (applyVoList.isEmpty()) {
            return Result.success();
        }
        return Result.success(applyVoList);
    }

    private EventVO convertToEventVO(Event event) {
        EventVO vo = new EventVO();
        BeanUtils.copyProperties(event, vo);
        return vo;
    }

    private EventApplyVO convertToEventApplyVO(EventApply eventApply) {
        EventApplyVO vo = new EventApplyVO();
        BeanUtils.copyProperties(eventApply, vo);
        return vo;
    }


}
