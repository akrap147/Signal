package com.evans.signal.chat.redis;

import com.evans.signal.chat.dto.CanvasEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CanvasRedis implements MessageListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            CanvasEventDto event = objectMapper.readValue(message.getBody(), CanvasEventDto.class);
            messagingTemplate.convertAndSend("/topic/canvas." + event.getRoomId(), event);
        } catch (Exception e) {
            log.error("Canvas 메시지 역직렬화 실패: {}", e.getMessage());
        }
    }
}
