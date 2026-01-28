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
        rabbitTemplate.setReplyAddress(SIGNALING_REPLY_QUEUE);
        rabbitTemplate.setReplyTimeout(6000); // 6초 타임아웃
        return rabbitTemplate;
    }

    @Bean
    public org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer replyListenerContainer(ConnectionFactory connectionFactory, RabbitTemplate rabbitTemplate) {
        org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer container = new org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(signalingReplyQueue());
        container.setMessageListener(rabbitTemplate); // RabbitTemplate이 스스로 리스너가 되어 응답 처리
        return container;
    }
}
