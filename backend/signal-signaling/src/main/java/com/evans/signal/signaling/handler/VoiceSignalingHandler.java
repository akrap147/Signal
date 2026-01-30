package com.evans.signal.signaling.handler;

import com.evans.signal.signaling.dto.SignalingMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.evans.signal.signaling.config.RabbitMqConfig.SIGNALING_EXCHANGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceSignalingHandler extends TextWebSocketHandler {
    // Voice 및 화상인데 TextWebSocketHandler로 하는이유는 단지 Json만 왔다갔다 하면 되기 때문
    // 실제 Voice데이터가 아닌 누가 누구랑 연결이 되어있는지등 주소록 같은 Handler임

    // RabbitMQ로 던지기 위해서.
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    // Room ID -> Set of WebSocketSessions
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    // Session ID -> Transport ID (to clean up media resources on disconnect)
    private final Map<String, String> sessionTransportMap = new ConcurrentHashMap<>();


    // 세션 제거 과정
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = sessionRoomMap.remove(session.getId());
        String transportId = sessionTransportMap.remove(session.getId());

        // 1. 미디어 서버에 리소스 정리 요청 (Transport 종료)
        if (transportId != null) {
            Map<String, Object> closeRequest = Map.of("transportId", transportId);
            rabbitTemplate.convertAndSend(SIGNALING_EXCHANGE, "signal.media.closeTransport", closeRequest);
            log.info("Requested Media Server to close transport: {}", transportId);
        }

        // 2. 룸 세션 정리
        if (roomId != null) {
            Set<WebSocketSession> roomSessions = rooms.get(roomId);
            if (roomSessions != null) {
                roomSessions.remove(session);
                if (roomSessions.isEmpty()) {
                    rooms.remove(roomId);
                }
            }
            log.info("Session {} removed from room {}", session.getId(), roomId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();
        // Json으로 날라온 글을 읽어라
        SignalingMessage signalingMessage = objectMapper.readValue(payload, SignalingMessage.class);

        log.info("Received signaling message: {}", signalingMessage.getType());

        String routingKey = "";
        String roomId = signalingMessage.getRoomId();

        // 관리 로직: Join 시 세션 저장
        if ("join".equals(signalingMessage.getType())) {
            rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
            sessionRoomMap.put(session.getId(), roomId);
            log.info("Session {} joined room {}", session.getId(), roomId);
        }

        switch (signalingMessage.getType()) {
            case "join":
                routingKey = "signal.media.createRouter";
                break;
            case "createTransport":
                routingKey = "signal.media.createTransport";
                break;
            case "connectTransport":
                routingKey = "signal.media.connectTransport";
                break;
            case "produce":
                routingKey = "signal.media.produce";
                break;
            case "consume":
                routingKey = "signal.media.consume";
                break;
            case "resume":
                routingKey = "signal.media.resume";
                break;
            default:
                log.warn("Unknown message type: {}", signalingMessage.getType());
                return;
        }

        // RabbitMQ RPC 호출
        Object response = rabbitTemplate.convertSendAndReceive(
                SIGNALING_EXCHANGE,
                routingKey,
                signalingMessage.getData() != null ? signalingMessage.getData() : Map.of("roomId", roomId)
        );

        if (response != null) {
            String jsonResponse = objectMapper.writeValueAsString(response);
            
            // Transport 생성 시, ID 저장 (나갈 때 지우기 위해)
            if ("createTransport".equals(signalingMessage.getType())) {
                Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);
                if (Boolean.TRUE.equals(responseMap.get("success"))) {
                    String transportId = (String) responseMap.get("id");
                    sessionTransportMap.put(session.getId(), transportId);
                    log.info("Mapped Session {} to Transport {}", session.getId(), transportId);
                }
            }

            session.sendMessage(new TextMessage(jsonResponse));

            // Produce 성공 시, 다른 사람들에게 알림 (Broadcasting)
            if ("produce".equals(signalingMessage.getType())) {
                broadcastNewProducer(session, roomId, response);
            }

        } else {
            log.error("No response from Media Server for type: {}", signalingMessage.getType());
            session.sendMessage(new TextMessage("{\"success\": false, \"error\": \"No response from Media Server\"}"));
        }
    }

    private void broadcastNewProducer(WebSocketSession currentSession, String roomId, Object response) {
        try {
            // response는 LinkedHashMap 등일 수 있음. JSON 변환 후 파싱하거나 Map으로 캐스팅
            Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);
            String producerId = (String) responseMap.get("id");

            if (producerId != null) {
                Map<String, Object> notification = Map.of(
                        "type", "newProducer",
                        "producerId", producerId,
                        "roomId", roomId
                );
                String notificationJson = objectMapper.writeValueAsString(notification);

                Set<WebSocketSession> roomSessions = rooms.get(roomId);
                if (roomSessions != null) {
                    for (WebSocketSession s : roomSessions) {
                        // 나 자신에게는 보내지 않음
                        if (s.isOpen() && !s.getId().equals(currentSession.getId())) {
                            s.sendMessage(new TextMessage(notificationJson));
                        }
                    }
                }
                log.info("Broadcasted newProducer {} to room {}", producerId, roomId);
            }
        } catch (Exception e) {
            log.error("Failed to broadcast newProducer", e);
        }
    }
}
