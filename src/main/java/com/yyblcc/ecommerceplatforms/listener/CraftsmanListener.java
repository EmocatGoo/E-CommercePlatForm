package com.yyblcc.ecommerceplatforms.listener;

import com.yyblcc.ecommerceplatforms.domain.message.CraftsmanMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.checkerframework.checker.index.qual.SameLen;
import org.springframework.stereotype.Service;

@RocketMQMessageListener(
        topic = "craftsman-topic",
        consumerGroup = "craftsman-group",
        consumeMode = ConsumeMode.CONCURRENTLY
)
@Service
@RequiredArgsConstructor
@Slf4j
public class CraftsmanListener implements RocketMQListener<CraftsmanMessage> {
    @Override
    public void onMessage(CraftsmanMessage message) {
        Integer actionCode = message.getActionCode();
        switch (actionCode){
            case 1:
                log.info("craftsman-listener: 1");
                break;
            case 2:
                log.info("craftsman-listener: 2");
                break;
            case 3:
                log.info("craftsman-listener: 3");
                break;
            default:
                log.info("craftsman-listener: default");
                break;
        }
    }
}
