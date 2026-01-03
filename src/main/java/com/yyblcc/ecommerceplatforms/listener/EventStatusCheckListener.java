package com.yyblcc.ecommerceplatforms.listener;

import com.yyblcc.ecommerceplatforms.domain.po.Event;
import com.yyblcc.ecommerceplatforms.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RocketMQMessageListener(
        topic = "event-status-check-topic",
        consumerGroup = "event-status-check-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY
)
@RequiredArgsConstructor
public class EventStatusCheckListener implements RocketMQListener<String> {
    private final EventMapper eventMapper;

    @Override
    public void onMessage(String s) {
        List<Event> events = eventMapper.selectList(null);
        events.forEach(event -> {
            if (event.getEndTime().isBefore(LocalDateTime.now()) && !event.getStatus().equals(3)) {
                event.setStatus(3);
                event.setUpdateTime(LocalDateTime.now());
                eventMapper.updateById(event);
            } else if (event.getStartTime().isBefore(LocalDateTime.now())
                    && (event.getStatus().equals(0) || event.getStatus().equals(1))
                    && event.getEndTime().isAfter(LocalDateTime.now())) {
                event.setStatus(2);
                event.setUpdateTime(LocalDateTime.now());
                eventMapper.updateById(event);
            }
        });
    }
}
