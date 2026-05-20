package com.evans.signal.chat.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RabbitMqConfig {

    // ── Chat (DB write) ──────────────────────────────────────
    public static final String DB_EXCHANGE_NAME = "chat.db.exchange";
    public static final String DB_QUEUE_NAME    = "chat.db.queue";
    public static final String DB_ROUTING_KEY   = "chat.db.record";

    // ── Signaling (WebRTC ↔ signal-media RPC) ────────────────
    public static final String SIGNALING_EXCHANGE    = "signal.signaling.exchange";
    public static final String MEDIA_REQUEST_QUEUE   = "signal.media.request.queue";
    public static final String SIGNALING_REPLY_QUEUE = "signal.signaling.reply.queue";

    // ── 공통 ────────────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ── Chat beans ───────────────────────────────────────────
    @Bean
    public Queue dbQueue() {
        return new Queue(DB_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange dbExchange() {
        return new DirectExchange(DB_EXCHANGE_NAME);
    }

    @Bean
    public Binding dbBinding(Queue dbQueue, DirectExchange dbExchange) {
        return BindingBuilder.bind(dbQueue).to(dbExchange).with(DB_ROUTING_KEY);
    }

    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ── Signaling beans ──────────────────────────────────────
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

    @Bean("signalingRabbitTemplate")
    public RabbitTemplate signalingRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setReplyAddress(SIGNALING_REPLY_QUEUE);
        template.setReplyTimeout(6000);
        return template;
    }

    @Bean
    public SimpleMessageListenerContainer replyListenerContainer(
            ConnectionFactory connectionFactory,
            @org.springframework.beans.factory.annotation.Qualifier("signalingRabbitTemplate")
            RabbitTemplate signalingRabbitTemplate) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(signalingReplyQueue());
        container.setMessageListener(signalingRabbitTemplate);
        return container;
    }

    // ── Redis Pub/Sub ────────────────────────────────────────────────────────
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }
}
