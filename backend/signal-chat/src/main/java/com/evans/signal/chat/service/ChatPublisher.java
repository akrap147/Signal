package com.evans.signal.chat.service;

import com.evans.signal.chat.config.RabbitMqConfig;
import com.evans.signal.chat.dto.ChatMessageDto;
import com.evans.signal.redis.RedisKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatPublisher {

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publish(ChatMessageDto message) {
        publishToRedis(message);
        publishToDb(message);
    }

    private void publishToRedis(ChatMessageDto message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(RedisKeys.chatChannel(message.getRoomId()), json);
        } catch (JsonProcessingException e) {
            log.error("Redis publish 실패: {}", e.getMessage());
        }
    }

    private void publishToDb(ChatMessageDto message) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.DB_EXCHANGE_NAME,
                RabbitMqConfig.DB_ROUTING_KEY,
                message
        );
    }
}
