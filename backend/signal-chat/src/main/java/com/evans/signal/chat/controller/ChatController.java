package com.evans.signal.chat.controller;

import com.evans.signal.chat.dto.ChatMessageDto;
import com.evans.signal.chat.service.ChatPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatPublisher chatPublisher;

    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageDto message) {
        chatPublisher.publish(message);
    }
}
