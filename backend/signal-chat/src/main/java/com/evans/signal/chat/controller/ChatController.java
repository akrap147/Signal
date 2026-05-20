package com.evans.signal.chat.controller;

import com.evans.signal.chat.config.RabbitMqConfig;
import com.evans.signal.chat.dto.ChatMessageDto;
import com.evans.signal.chat.session.UserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;
    private final UserSessionService userSessionService;

    /**
     * 메세지 전송 엔드포인트
     * 클라이언트 전송 경로: /pub/chat/message
     */
    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        // 세션으로 실제 userId 검증 — 클라이언트가 보낸 senderId를 신뢰하지 않음
        String sessionId = headerAccessor.getSessionId();
        Long verifiedUserId = userSessionService.getUserIdBySession(sessionId);
        if (verifiedUserId == null) {
            log.warn("Rejected message from unregistered session: {}", sessionId);
            return;
        }

        // 1. 번호표 뽑기
        Long seqId = redisTemplate.opsForValue().increment("room:" + message.getRoomId() + ":seq");
        ChatMessageDto finalMessage = message.toBuilder()
                .senderId(verifiedUserId)  // 클라이언트 값 덮어씌우기
                .seqId(seqId)
                .build();

        // 2. 실시간 전송
        messagingTemplate.convertAndSend("/topic/channel." + message.getRoomId(), finalMessage);

        // 3. DB 저장 (RabbitMQ)
        rabbitTemplate.convertAndSend(RabbitMqConfig.DB_EXCHANGE_NAME, RabbitMqConfig.DB_ROUTING_KEY, finalMessage);
    }
}
