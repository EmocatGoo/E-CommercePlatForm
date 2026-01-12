package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.CreateSessionDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.ChatSessionVO;
import com.yyblcc.ecommerceplatforms.domain.po.ChatMessage;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.ChatMessageService;
import com.yyblcc.ecommerceplatforms.service.ChatSessionService;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

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

    @GetMapping(value = "/modelInStream",produces = "text/html;charset=utf-8")
    public Flux<String> fluxChat(@RequestParam String prompt) {
        log.info("收到流式聊天请求: {}", prompt);

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        String systemPrompt = loadSystemPromptFromTxt("system_prompt.txt");
        streamingChatModel.chat(
                Arrays.asList(
                        SystemMessage.from(systemPrompt),
                        UserMessage.from(prompt)
                ),
                new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String s) {
                log.info("收到部分响应: {}",s);
                sink.tryEmitNext(s);
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                log.info("流式响应完成");
                sink.tryEmitComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("流式响应发生错误", throwable);
                sink.tryEmitError(throwable);
            }
        });

        return sink.asFlux();
    }

    @GetMapping(value = "/modelInStream-notLogin",produces = "text/html;charset=utf-8")
    public Flux<String> fluxChatForNotLogin(@RequestParam String prompt) {
        log.info("收到流式聊天请求: {}", prompt);

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        String systemPrompt = loadSystemPromptFromTxt("system-prompt_notlogin.txt");
        streamingChatModel.chat(
                Arrays.asList(
                        SystemMessage.from(systemPrompt),
                        UserMessage.from(prompt)
                ),
                new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String s) {
                        log.info("收到部分响应: {}",s);
                        sink.tryEmitNext(s);
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse chatResponse) {
                        log.info("流式响应完成");
                        sink.tryEmitComplete();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.error("流式响应发生错误", throwable);
                        sink.tryEmitError(throwable);
                    }
                });

        return sink.asFlux();
    }
    private String loadSystemPromptFromTxt(String fileName){
        try{
            ClassPathResource resource = new ClassPathResource(fileName);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("加载系统提示失败", e);
            return "你是一个电商客服助手，专门处理用户在电商平台的咨询。\n" +
                    "请遵循以下规则：\n" +
                    "1. 回答要简洁明了，符合电商客服场景\n" +
                    "2. 如涉及商品推荐，要基于用户需求进行推荐\n" +
                    "3. 对于价格、物流、售后等问题，提供专业解答\n" +
                    "4. 不要回答与电商无关的问题";
        }
    }

}
