package com.evans.signal.chat.controller;

import com.evans.signal.chat.config.RabbitMqConfig;
import com.evans.signal.chat.dto.ChatMessageDto;
import com.evans.signal.chat.infrastructure.ChatMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * 메세지 전송 엔드포인트
     * 클라이언트 전송 경로: /pub/chat/message
     */
    // 처음에는 보내주는걸로하자
    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageDto message) {

        messagingTemplate.convertAndSend(
                "/topic/channel." + message.getRoomId(), message
        );
    }
}
