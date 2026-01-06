package com.yyblcc.ecommerceplatforms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.po.EventComment;
import com.yyblcc.ecommerceplatforms.mapper.EventCommentMapper;
import com.yyblcc.ecommerceplatforms.service.EventCommentService;
import org.springframework.stereotype.Service;

@Service
public class EventCommentServiceImplement extends ServiceImpl<EventCommentMapper, EventComment> implements EventCommentService {
}
