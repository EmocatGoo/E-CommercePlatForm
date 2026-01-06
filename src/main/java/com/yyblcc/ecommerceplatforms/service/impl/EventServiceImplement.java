package com.yyblcc.ecommerceplatforms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventApplyDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventCommentAddDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.EventApplyVO;
import com.yyblcc.ecommerceplatforms.domain.VO.EventVO;
import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.domain.query.EventQuery;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.EventApplyMapper;
import com.yyblcc.ecommerceplatforms.mapper.EventCommentMapper;
import com.yyblcc.ecommerceplatforms.mapper.EventMapper;
import com.yyblcc.ecommerceplatforms.service.EventCommentService;
import com.yyblcc.ecommerceplatforms.service.EventService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import com.yyblcc.ecommerceplatforms.util.commentPath.PathUtils;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImplement extends ServiceImpl<EventMapper, Event> implements EventService {
    private final EventMapper eventMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final EventApplyMapper eventApplyMapper;
    private final RocketMQTemplate rocketMQTemplate;
    private final EventCommentMapper eventCommentMapper;
    private final EventCommentService eventCommentService;


    @Override
    @UpdateBloomFilter
    public Result<String> saveEvent(EventDTO dto) {
        Event event = new Event();
        BeanUtils.copyProperties(dto, event);
        event.setCreateTime(LocalDateTime.now());
        event.setUpdateTime(LocalDateTime.now());
        event.setStatus(0);
        save(event);
        stringRedisTemplate.keys("event:page").forEach(stringRedisTemplate::delete);
        return Result.success("活动添加成功");
    }

    @Override
    public Result<String> updateEvent(EventDTO dto) {
        Event event = new Event();
        BeanUtils.copyProperties(dto, event);
        eventMapper.updateById(event);
        stringRedisTemplate.keys("event:page").forEach(stringRedisTemplate::delete);
        return Result.success("修改成功");
    }

    @Override
    public Result<EventVO> getDetail(Long id) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        Event event = query().eq("id", id).one();
        if (event == null) {
            return Result.error("没有找到对应活动");
        }
        EventVO vo = new EventVO();
        BeanUtils.copyProperties(event, vo);
        EventApply apply = eventApplyMapper.selectOne(
                new LambdaQueryWrapper<EventApply>()
                        .eq(EventApply::getEventId, id)
                        .eq(EventApply::getUserId, userId));
        vo.setUserApplied(apply != null);
        return Result.success(vo);
    }

    @Override
    public Result<PageBean<EventVO>> pageList(EventQuery eventQuery) {
        checkStatus();
        Page<Event> eventPage = eventMapper.selectPage(new Page<>(eventQuery.getPage(),eventQuery.getPageSize()),
                new LambdaQueryWrapper<Event>()
                        .like(eventQuery.getTitle() != null, Event::getTitle,eventQuery.getTitle())
                        .eq(eventQuery.getStatus() != null, Event::getStatus, eventQuery.getStatus())
                        .orderByAsc(Event::getCreateTime));

        List<EventVO> voList = eventPage.getRecords().stream().map(this::convertToEventVO).toList();
        PageBean<EventVO> pageBean = new PageBean<>(eventPage.getTotal(),voList);
        return Result.success(pageBean);
    }


    @Override
    //TODO 如果活动有人数限制需要加锁抢名额
    public Result<String> apply(EventApplyDTO dto) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        Long eventId = dto.getEventId();
        Event event = query().eq("id", eventId).one();

        if (event == null) {
            return Result.error("没有找到对应活动");
        }

        EventApply apply = eventApplyMapper.selectOne(new LambdaQueryWrapper<EventApply>()
                .eq(EventApply::getEventId, eventId)
                .eq(EventApply::getUserId, userId)
                .orderByDesc(EventApply::getCreateTime)
                .last("FOR UPDATE"));

        if (apply != null) {
            return Result.error("已报名，请勿重复操作");
        }

        EventApply eventApply = new EventApply();
        BeanUtils.copyProperties(dto, eventApply);
        eventApply.setUserId(userId);
        eventApply.setCreateTime(LocalDateTime.now());
        eventApply.setStatus(1);
        eventApplyMapper.insert(eventApply);

        eventMapper.update(new LambdaUpdateWrapper<Event>().eq(Event::getId, eventId).setSql("people_num = people_num + 1"));

        return Result.success("报名成功");
    }

    @Override
    public Result<String> signIn(Long eventId) {
        Long userId = AuthContext.getUserId();
        EventApply eventApply = eventApplyMapper.selectOne(new LambdaQueryWrapper<EventApply>()
                .eq(EventApply::getEventId, eventId)
                .eq(EventApply::getUserId, userId)
                .orderByDesc(EventApply::getCreateTime)
                .last("FOR UPDATE"));
        if (eventApply == null) {
            return Result.error("未找到对应报名信息，签到失败");
        }

        eventApply.setSignTime(LocalDateTime.now());
        eventApplyMapper.updateById(eventApply);
        return Result.success("签到成功");
    }

    @Override
    public Result<List<EventApplyVO>> getApplyList(Long eventId) {
        List<EventApplyVO> applyVoList = eventApplyMapper.selectList(new LambdaQueryWrapper<EventApply>()
                .eq(EventApply::getEventId, eventId)
                .orderByDesc(EventApply::getCreateTime)
                .last("FOR UPDATE")).stream().map(this::convertToEventApplyVO).toList();
        if (applyVoList.isEmpty()) {
            return Result.success();
        }
        return Result.success(applyVoList);
    }

    @Override
    @UpdateBloomFilter
    public Result<String> createComment(EventCommentAddDTO dto) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        boolean isReply = dto.getReplyToUserId() != null;

        Long rootId = null;
        String parentPath = null;
        Long parentId = null;
        int depth = 1;

        if (isReply) {
            parentId = dto.getParentCommentId();
            EventComment parent = eventCommentService.getById(parentId);

            if (parent == null) {
                return Result.error("父评论不存在");
            }

            rootId = (parent.getRootId() == null || parent.getRootId() == 0)
                    ? parent.getId()
                    : parent.getRootId();

            parentPath = parent.getPath();
            depth = parent.getDepth() + 1;
        }

        String nextPathSegment = null;

        //A（根评论）
        //├── B（回复 A）
        //│   ├── C（回复 B）
        //│   └── D（回复 B）
        //└── E（回复 A）
        //A   → 0001
        //B   → 0001.0001
        //C   → 0001.0001.0001
        //D   → 0001.0001.0002
        //E   → 0001.0002
        //非回复评论,找当前路径上最大的path,如0001.0001.0002.0003  0003就是最大的路径,
        if (!isReply) {
            // 根评论找同一个 productId 下 path 最大的根
            String maxRootPath = eventCommentMapper.selectMaxRootPath(dto.getEventId());
            nextPathSegment = PathUtils.genRootNextPath(maxRootPath);
            parentPath = nextPathSegment;
        } else {
            // 回复评论找同 parent 下的最大子 path
            String maxChildPath = eventCommentMapper.selectMaxChildPath(parentPath + "%");
            nextPathSegment = PathUtils.genChildNextPath(parentPath, maxChildPath);
        }

        // 4. 保存评论
        EventComment comment = EventComment.builder()
                .eventId(dto.getEventId())
                .userId(userId)
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .rootId(rootId)
                .path(nextPathSegment)
                .depth(depth)
                .likeCount(0)
                .replyCount(0)
                .createTime(LocalDateTime.now())
                .build();

        eventCommentService.save(comment);

        // 5. 如果是回复，父评论 replyCount +1
        if (isReply) {
            update().setSql("reply_count = reply_count + 1").eq("id", parentId).update();
        }

        return Result.success("评论成功");
    }

    @Override
    public Result<List<EventVO>> getEventsByApply() {
        String key = "event:hot";
        try{
            String cacheStr = stringRedisTemplate.opsForValue().get(key);
            if (cacheStr != null){
                if (cacheStr.isEmpty()){
                    return Result.success();
                }
                return Result.success(JSON.parseObject(cacheStr, new TypeReference<List<EventVO>>(){}));
            }
        } catch (RuntimeException e) {
            log.warn("缓存查询异常");
        }
        List<Event> events = eventMapper.selectList(
                new LambdaQueryWrapper<Event>()
                        .orderByDesc(Event::getPeopleNum)
                        .last("limit 6"));
        List<EventVO> eventVos = events.stream().map(this::convertToEventVO).toList();
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(eventVos),Duration.ofMinutes(10 + new Random().nextInt(0,5)));
        return Result.success(eventVos);
    }

    @Override
    public Result<List<EventApplyVO>> getMyAttends() {
        Long userId = StpKit.USER.getLoginIdAsLong();
        List<EventApply> myAttendEvents = eventApplyMapper.selectList(new LambdaQueryWrapper<EventApply>()
                .eq(EventApply::getUserId, userId)
                .orderByDesc(EventApply::getCreateTime));
        return Result.success(myAttendEvents.stream().map(this::convertToEventApplyVO).toList());
    }

    @Override
    public Result<Integer> alertEventTime() {
        Long userId = StpKit.USER.getLoginIdAsLong();
        List<EventApply> myAttendEvents = eventApplyMapper.selectList(new LambdaQueryWrapper<EventApply>()
                .eq(EventApply::getUserId, userId)
                .orderByDesc(EventApply::getCreateTime));
        List<Event> alertEvents = new ArrayList<>();
        for (EventApply myAttendEvent : myAttendEvents) {
            Event event = eventMapper.selectOne(new LambdaQueryWrapper<Event>().eq(Event::getId, myAttendEvent.getEventId()));
            if (event.getStartTime().isAfter(LocalDateTime.now()) && 
                event.getStartTime().isBefore(LocalDateTime.now().plusHours(24))) {
                alertEvents.add(event);
            }
        }
        return Result.success(alertEvents.size());

    }

    @Override
    public Result getUserApplyCount() {
        Long userId = StpKit.USER.getLoginIdAsLong();
        Long count = eventApplyMapper.selectCount(new LambdaQueryWrapper<EventApply>().eq(EventApply::getUserId, userId));
        return Result.success(count);
    }

    private void checkStatus() {
        rocketMQTemplate.asyncSend("event-status-check-topic", "check", new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送成功{}", sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("发送失败{}", throwable);
            }
        });
    }

    private EventVO convertToEventVO(Event event) {
        EventVO vo = new EventVO();
        BeanUtils.copyProperties(event, vo);
        return vo;
    }

    private EventApplyVO convertToEventApplyVO(EventApply eventApply) {
        EventApplyVO applyVO = new EventApplyVO();
        BeanUtils.copyProperties(eventApply, applyVO);
        Event dbEvent = eventMapper.selectOne(new LambdaQueryWrapper<Event>()
                .eq(Event::getId, eventApply.getEventId()));
        BeanUtils.copyProperties(dbEvent, applyVO);
        applyVO.setEventStatus(dbEvent.getStatus());
        return applyVO;
    }


}
