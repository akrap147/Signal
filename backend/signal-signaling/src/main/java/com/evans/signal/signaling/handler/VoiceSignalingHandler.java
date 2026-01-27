package com.evans.signal.signaling.handler;

import com.evans.signal.signaling.dto.SignalingMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

import static com.evans.signal.signaling.config.RabbitMqConfig.SIGNALING_EXCHANGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceSignalingHandler extends TextWebSocketHandler {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        SignalingMessage signalingMessage = objectMapper.readValue(payload, SignalingMessage.class);

        log.info("Received signaling message: {}", signalingMessage.getType());

        String routingKey = "";
        switch (signalingMessage.getType()) {
            case "join":
                routingKey = "signal.media.createRouter";
                break;
            case "createTransport":
                routingKey = "signal.media.createTransport";
                break;
            default:
                log.warn("Unknown message type: {}", signalingMessage.getType());
                return;
        }

        // RabbitMQ RPC 호출
        Object response = rabbitTemplate.convertSendAndReceive(
                SIGNALING_EXCHANGE,
                routingKey,
                signalingMessage.getData() != null ? signalingMessage.getData() : Map.of("roomId", signalingMessage.getRoomId())
        );

        if (response != null) {
            String jsonResponse = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonResponse));
        } else {
            log.error("No response from Media Server for type: {}", signalingMessage.getType());
            session.sendMessage(new TextMessage("{\"success\": false, \"error\": \"No response from Media Server\"}"));
        }
    }
}
