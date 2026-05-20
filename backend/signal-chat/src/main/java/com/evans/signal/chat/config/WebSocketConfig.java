package com.evans.signal.chat.config;

import com.evans.signal.chat.session.StompSessionInterceptor;
import com.evans.signal.chat.signaling.handler.VoiceSignalingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    private final StompSessionInterceptor stompSessionInterceptor;
    private final VoiceSignalingHandler voiceSignalingHandler;

    @Value("${chat.rabbitmq.enabled:true}")
    private boolean useRabbitMq;

    @Value("${spring.rabbitmq.host}")
    private String relayHost;

    @Value("${spring.rabbitmq.stomp.port:61613}")
    private int relayPort;

    @Value("${spring.rabbitmq.username}")
    private String login;

    @Value("${spring.rabbitmq.password}")
    private String passcode;

    // ── STOMP (채팅) ──────────────────────────────────────────────────────────

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        if (useRabbitMq) {
            registry.enableStompBrokerRelay("/topic", "/queue", "/exchange", "/amq/queue")
                    .setRelayHost(relayHost)
                    .setRelayPort(relayPort)
                    .setClientLogin(login)
                    .setClientPasscode(passcode)
                    .setSystemLogin(login)
                    .setSystemPasscode(passcode)
                    .setSystemHeartbeatSendInterval(10000)
                    .setSystemHeartbeatReceiveInterval(10000);
        } else {
            registry.enableSimpleBroker("/topic", "/queue");
        }
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompSessionInterceptor);
    }

    // ── Plain WebSocket (시그널링) ─────────────────────────────────────────────

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceSignalingHandler, "/ws/signaling")
                .setAllowedOrigins("*");
    }
}
