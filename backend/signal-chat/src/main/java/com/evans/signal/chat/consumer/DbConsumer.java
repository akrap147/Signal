package com.evans.signal.chat.consumer;

import com.evans.signal.chat.config.RabbitMqConfig;
import com.evans.signal.chat.domain.ChatMessage;
import com.evans.signal.chat.dto.ChatMessageDto;
import com.evans.signal.chat.infrastructure.ChatMessageJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DbConsumer {

    private final ChatMessageJpaRepository chatMessageRepository;
    private final ObjectMapper objectMapper;

    /**
     * DB 저장 전담 Consumer.
     * 'db.queue'에서 메시지를 꺼내 DB에 저장함.
     * 느려도 상관없음 (비동기 처리)
     */
    public void saveToDb(ChatMessageDto message) {
        // 이미 MessageConverter가 객체 변환을 끝냈으므로 로직에만 집중
        chatMessageRepository.save(ChatMessage.builder()
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .seqId(message.getSeqId())
                .build());

        log.debug("DB saved: Room {} from User {}", message.getRoomId(), message.getSenderId());
    }
}
