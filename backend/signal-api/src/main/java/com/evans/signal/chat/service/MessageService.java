package com.evans.signal.chat.service;

import com.evans.signal.chat.dto.ChatMessageResponse;
import com.evans.signal.chat.infrastructure.ChatMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChatMessageJpaRepository chatMessageJpaRepository;

    public List<ChatMessageResponse> getChannelMessages(Long channelId) {
        return chatMessageJpaRepository.findAllByRoomIdOrderByIdAsc(channelId)
                .stream()
                .map(message -> ChatMessageResponse.builder()
                        .id(message.getId())
                        .roomId(message.getRoomId())
                        .senderId(message.getSenderId())
                        .content(message.getContent())
                        .senderName(message.getSenderName())
                        .createdAt(message.getCreatedAt())
                        .build())
                .toList();
    }
}
