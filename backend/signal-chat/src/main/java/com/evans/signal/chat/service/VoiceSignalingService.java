package com.evans.signal.chat.service;

import com.evans.signal.chat.dto.SignalingMessage;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceSignalingService {

    private final RestTemplate restTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    @Value("${signal.media.url:http://localhost:3000}")
    private String mediaServerUrl;

    public void handle(SignalingMessage message) {
        String action = message.getAction();
        Long roomId = message.getRoomId();
        Long userId = message.getUserId();
        String personalDest = "/topic/voice." + roomId + "." + userId;
        String roomDest     = "/topic/voice." + roomId;

        try {
            switch (action) {
                case "joinRoom" -> {
                    JsonNode result = post("/router", Map.of("roomId", roomId));
                    send(personalDest, "joinRoom", result);
                }
                case "createTransport" -> {
                    JsonNode result = post("/transport", Map.of("roomId", roomId));
                    send(personalDest, "createTransport", result);
                }
                case "connect" -> {
                    post("/connect", message.getPayload());
                    send(personalDest, "connected", null);
                }
                case "produce" -> {
                    JsonNode result = post("/produce", message.getPayload());
                    send(personalDest, "produced", result);
                    messagingTemplate.convertAndSend(roomDest,
                            Map.of("action", "newProducer", "producerId", result.get("id").asText(), "userId", userId));
                }
                case "consume" -> {
                    JsonNode result = post("/consume", message.getPayload());
                    send(personalDest, "consumed", result);
                }
                case "resume" -> {
                    post("/resume", message.getPayload());
                    send(personalDest, "resumed", null);
                }
                case "leave" -> {
                    post("/close", message.getPayload());
                    messagingTemplate.convertAndSend(roomDest, Map.of("action", "userLeft", "userId", userId));
                }
                default -> log.warn("알 수 없는 action: {}", action);
            }
        } catch (Exception e) {
            log.error("[Voice] action={} 처리 실패: {}", action, e.getMessage());
            send(personalDest, "error", null);
        }
    }

    private JsonNode post(String path, Object body) {
        return restTemplate.postForObject(mediaServerUrl + path, body, JsonNode.class);
    }

    private void send(String destination, String action, JsonNode data) {
        messagingTemplate.convertAndSend(destination, Map.of("action", action, "data", data != null ? data : Map.of()));
    }
}
