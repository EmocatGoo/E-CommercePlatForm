package com.yyblcc.ecommerceplatforms.controller;

import cn.hutool.db.PageResult;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventApplyDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventCommentAddDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.EventVO;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.EventQuery;
import com.yyblcc.ecommerceplatforms.service.EventService;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
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


    // 1. 发布活动（管理员 + 匠人）
    @PostMapping("/save")
//    @PreAuthorize("hasAnyRole('ADMIN','CRAFTSMAN')")
    public Result save(@Validated @RequestBody EventDTO dto) {
        log.info("添加新活动：{}", dto);
        return eventService.saveEvent(dto);
    }

    @PutMapping("/update")
    public Result update(@Validated @RequestBody EventDTO dto) {
        log.info("修改活动内容:{}",dto);
        return eventService.updateEvent(dto);
    }

    // 2. 删除活动
    @PostMapping("/delete/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN','CRAFTSMAN')")
    public Result delete(@PathVariable Long id) {
        eventService.removeById(id);
        return Result.success();
    }

    // 3. 活动详情
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return eventService.getDetail(id);
    }

    // 4. 活动列表（分页）
    @GetMapping("/list")
    public Result list(EventQuery eventQuery) {
        return eventService.pageList(eventQuery);
    }

    // 6. 报名活动
    @PostMapping("/apply")
    public Result apply(@Validated @RequestBody EventApplyDTO dto) {
        Long userId = AuthContext.getUserId();
        log.info("用户：{} 报名参加活动:{}",userId,dto);
        return eventService.apply(dto, userId);
    }

    // 7. 签到
    @PostMapping("/sign")
//    @PreAuthorize("hasAnyRole('ADMIN','CRAFTSMAN')")
    public Result sign(@RequestParam Long eventId) {
        return eventService.signIn(eventId);
    }

    // 8. 报名列表（发布者查看）
    @GetMapping("/applies/{eventId}")
//    @PreAuthorize("hasAnyRole('ADMIN','CRAFTSMAN')")
    public Result applies(@PathVariable Long eventId) {
        return eventService.getApplyList(eventId);
    }

    @PostMapping("/comment")
    public Result comment(@RequestBody EventCommentAddDTO dto) {
        log.info("用户评论：{}",dto);
        return eventService.createComment(dto);
    }

}
