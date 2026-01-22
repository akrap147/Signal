package com.evans.signal.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    // 첫 요청 websocket을 연결 하기위한 endPoint 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*");

    }

    // Message Broker를 RabbitMQ로 사용하기 위한 configuration
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 기존 내장 브로커(SimpleBroker) 대신 외부 브로커(RabbitMQ) 사용
        // RabbitMQ STOMP는 /topic (pub/sub), /queue (p2p), /exchange (routing) 등을 지원함
        registry.enableStompBrokerRelay("/topic", "/queue", "/exchange", "/amq/queue")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest")
                .setSystemLogin("guest")
                .setSystemPasscode("guest");

        registry.setApplicationDestinationPrefixes("/pub");
    }
}
