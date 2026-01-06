package com.yyblcc.ecommerceplatforms.controller;

import cn.hutool.db.PageResult;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventApplyDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventCommentAddDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.EventVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.EventQuery;
import com.yyblcc.ecommerceplatforms.service.EventService;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventService eventService;

    @PostMapping("/save")
    public Result save(@Validated @RequestBody EventDTO dto) {
        log.info("添加新活动：{}", dto);
        return eventService.saveEvent(dto);
    }

    @PutMapping("/update")
    public Result update(@Validated @RequestBody EventDTO dto) {
        log.info("修改活动内容:{}",dto);
        return eventService.updateEvent(dto);
    }

    @PostMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        eventService.removeById(id);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return eventService.getDetail(id);
    }

    @GetMapping("/list")
    public Result list(EventQuery eventQuery) {
        return eventService.pageList(eventQuery);
    }

    @PostMapping("/apply")
    public Result apply(@RequestBody EventApplyDTO dto) {
        log.info("用户报名参加活动:{}",dto);
        return eventService.apply(dto);
    }

    @PostMapping("/sign")
    public Result sign(@RequestParam Long eventId) {
        return eventService.signIn(eventId);
    }

    @GetMapping("/applies/{eventId}")
    public Result applies(@PathVariable Long eventId) {
        return eventService.getApplyList(eventId);
    }

    @PostMapping("/comment")
    public Result comment(@RequestBody EventCommentAddDTO dto) {
        log.info("用户评论：{}",dto);
        return eventService.createComment(dto);
    }

    @GetMapping("/getEventsByApply")
    public Result getEventsByApply() {
        return eventService.getEventsByApply();
    }

    @GetMapping("/getMyAttends")
    public Result getMyAttends() {
        return eventService.getMyAttends();
    }

    @GetMapping("/alertEventTime")
    public Result alertEventTime() {
        return eventService.alertEventTime();
    }

    @GetMapping("/getUserApplyCount")
    public Result getUserApplyCount() {
        return eventService.getUserApplyCount();
    }

}
