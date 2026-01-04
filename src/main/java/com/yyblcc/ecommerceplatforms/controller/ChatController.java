package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.CreateSessionDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.ChatSessionVO;
import com.yyblcc.ecommerceplatforms.domain.po.ChatMessage;
import com.yyblcc.ecommerceplatforms.domain.po.ChatSession;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.ChatMessageService;
import com.yyblcc.ecommerceplatforms.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;

    @GetMapping("/messages")
    public List<ChatMessage> listMessages(@RequestParam String sessionId) {
        return chatMessageService.listBySession(sessionId);
    }
    @GetMapping("/messages/getUserUnread")
    public Result<Long> countUnreadByUserId(@RequestParam Long userId) {
        return chatSessionService.countUnreadByUserId(userId);
    }

    @GetMapping("/messages/getCraftsmanUnread")
    public Result<Long> countUnreadByCraftsmanId(@RequestParam Long craftsmanId) {
        return chatSessionService.countUnreadByCraftsmanId(craftsmanId);
    }

    @GetMapping("/sessions")
    public List<ChatSessionVO> listSessions(@RequestParam Integer userType, @RequestParam Long userId) {
        if (userType == 1) {
            return chatSessionService.getUserSessions(userId);
        } else {
            return chatSessionService.getCraftsmanSessions(userId);
        }
    }

    @PostMapping("/session")
    public ChatSessionVO createSession(@RequestBody CreateSessionDTO dto) {
        return chatSessionService.createOrGetSession(dto.getUserId(), dto.getCraftsmanId());
    }

    @PutMapping("/messages/read")
    public void readMessage(@RequestParam String sessionId, @RequestParam Integer userType) {
        chatSessionService.clearUnreadCount(sessionId, userType);
    }
}
