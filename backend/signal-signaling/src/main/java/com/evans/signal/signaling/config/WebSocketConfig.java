package com.evans.signal.signaling.config;

import com.evans.signal.signaling.handler.VoiceSignalingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final VoiceSignalingHandler voiceSignalingHandler;

    // WebSocket end point 지정
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceSignalingHandler, "/ws/signaling")
                .setAllowedOrigins("*");
    }
}
