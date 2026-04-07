package com.evans.signal.chat.controller;


import com.evans.signal.auth.service.ChatService;
import com.evans.signal.chat.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{channelId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChannelMessage(
            @PathVariable Long channelId) {
        return ResponseEntity.ok(chatService.getChannelMessages(channelId));

    }

}