package com.yyblcc.ecommerceplatforms.config;

import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


public class RocketMQConfig {
    //购物车项消息Producer
    @Bean("cartItemProducer")
    public TransactionMQProducer cartItemProducer(){
        TransactionMQProducer producer = new TransactionMQProducer("cartItem-producer-group");
        producer.setNamesrvAddr("127.0.0.1:9876");
        return producer;
    }
    @Bean("cartItemRocketMQTemplate")
    public RocketMQTemplate postFavoriteTemplate(@Qualifier("cartItemProducer") TransactionMQProducer producer) {
        RocketMQTemplate template = new RocketMQTemplate();
        template.setProducer(producer);
        return template;
    }
}
