package com.yyblcc.ecommerceplatforms.service;

import cn.hutool.db.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventApplyDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.EventDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.EventVO;
import com.yyblcc.ecommerceplatforms.domain.po.Event;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.EventQuery;
import jakarta.validation.Valid;

public interface EventService extends IService<Event> {
    Result saveEvent(EventDTO dto);

    Result updateEvent(EventDTO dto);

    Result getDetail(Long id);

    Result pageList(EventQuery eventQuery);

    Result apply( EventApplyDTO dto, Long userId);

    Result signIn(Long eventId);

    Result getApplyList(Long eventId);
}
