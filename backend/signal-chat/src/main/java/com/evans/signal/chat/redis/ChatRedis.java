package com.evans.signal.chat.redis;

import com.evans.signal.chat.dto.ChatMessageDto;
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
public class ChatRedis implements MessageListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessageDto dto = objectMapper.readValue(message.getBody(), ChatMessageDto.class);
            String destination = dto.getType() == ChatMessageDto.MessageType.DM
                    ? "/topic/dm." + dto.getRoomId()
                    : "/topic/channel." + dto.getRoomId();
            messagingTemplate.convertAndSend(destination, dto);
        } catch (Exception e) {
            log.error("Redis 메시지 역직렬화 실패: {}", e.getMessage());
        }
    }

}