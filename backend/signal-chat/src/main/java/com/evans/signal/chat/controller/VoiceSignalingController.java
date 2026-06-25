package com.evans.signal.chat.controller;

import com.evans.signal.chat.dto.SignalingMessage;
import com.evans.signal.chat.service.VoiceSignalingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class VoiceSignalingController {

    private final VoiceSignalingService voiceSignalingService;

    @MessageMapping("/voice")
    public void handleSignaling(@Payload SignalingMessage message) {
        voiceSignalingService.handle(message);
    }
}
