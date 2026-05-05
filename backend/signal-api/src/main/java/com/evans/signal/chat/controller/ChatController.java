package com.evans.signal.chat.controller;

import com.evans.signal.chat.dto.ChatMessageResponse;
import com.evans.signal.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService chatService;

    @GetMapping("/{channelId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChannelMessage(
            @PathVariable Long channelId) {
        return ResponseEntity.ok(chatService.getChannelMessages(channelId));
    }

}