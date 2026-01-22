package com.evans.signal.chat.controller;

import com.evans.signal.chat.config.RabbitMqConfig;
import com.evans.signal.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * 메세지 전송 엔드포인트
     * 클라이언트 전송 경로: /pub/chat/message
     */
    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageDto message) {
        // 1. 번호표 뽑기
        Long seqId = redisTemplate.opsForValue().increment("room:" + message.getRoomId() + ":seq");
        ChatMessageDto finalMessage = message.toBuilder().seqId(seqId).build();

        // 2. 실시간 전송 (현재 서버 접속자 대상)
        messagingTemplate.convertAndSend("/topic/channel." + message.getRoomId(), finalMessage);

        // 3. DB 저장은 나중에 천천히 (RabbitMQ행)
        rabbitTemplate.convertAndSend(RabbitMqConfig.DB_EXCHANGE_NAME, RabbitMqConfig.DB_ROUTING_KEY, finalMessage);
    }
}
