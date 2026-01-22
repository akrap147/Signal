package com.evans.signal.chat.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitMqConfig {

    public static final String DB_EXCHANGE_NAME = "chat.db.exchange";
    public static final String DB_QUEUE_NAME = "chat.db.queue";
    public static final String DB_ROUTING_KEY = "chat.db.record";

    @Bean
    public Queue dbQueue() {
        return new Queue(DB_QUEUE_NAME, true);
    }

    // 1. 여기서 DirectExchange로 등록했습니다.
    @Bean
    public DirectExchange dbExchange() {
        return new DirectExchange(DB_EXCHANGE_NAME);
    }

    // 2. 파라미터 타입을 DirectExchange로 맞춰주세요! (TopicExchange 아님)
    @Bean
    public Binding dbBinding(Queue dbQueue, DirectExchange dbExchange) {
        return BindingBuilder.bind(dbQueue).to(dbExchange).with(DB_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
