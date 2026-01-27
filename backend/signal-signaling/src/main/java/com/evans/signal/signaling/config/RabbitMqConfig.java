package com.evans.signal.signaling.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String SIGNALING_EXCHANGE = "signal.signaling.exchange";
    public static final String MEDIA_REQUEST_QUEUE = "signal.media.request.queue";
    public static final String SIGNALING_REPLY_QUEUE = "signal.signaling.reply.queue";

    @Bean
    public TopicExchange signalingExchange() {
        return new TopicExchange(SIGNALING_EXCHANGE);
    }

    @Bean
    public Queue mediaRequestQueue() {
        return new Queue(MEDIA_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue signalingReplyQueue() {
        return new Queue(SIGNALING_REPLY_QUEUE, true);
    }

    @Bean
    public Binding mediaRequestBinding(Queue mediaRequestQueue, TopicExchange signalingExchange) {
        return BindingBuilder.bind(mediaRequestQueue).to(signalingExchange).with("signal.media.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setReplyAddress(SIGNALING_REPLY_QUEUE); // RPC 응답 큐 설정
        return rabbitTemplate;
    }
}
