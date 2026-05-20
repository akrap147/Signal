package com.evans.signal.chat.signaling.handler;

import com.evans.signal.chat.session.UserSessionService;
import com.evans.signal.chat.signaling.dto.SignalingMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.evans.signal.chat.config.RabbitMqConfig.SIGNALING_EXCHANGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceSignalingHandler extends TextWebSocketHandler implements MessageListener {

    @Qualifier("signalingRabbitTemplate")
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final UserSessionService userSessionService;

    private static final String INSTANCE_ID    = UUID.randomUUID().toString();
    private static final String CHANNEL_PREFIX = "voice:room:";

    // WebSocket handles must stay in-memory (cannot be stored in Redis)
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(this, new PatternTopic(CHANNEL_PREFIX + "*"));
        log.info("[Signaling] Subscribed to Redis channel: {}*", CHANNEL_PREFIX);
    }

    /** Receives room-event messages published by OTHER instances via Redis Pub/Sub. */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            Map<String, Object> event = objectMapper.readValue(message.getBody(), Map.class);
            if (INSTANCE_ID.equals(event.get("instanceId"))) return;

            String channel = new String(message.getChannel());
            String roomId  = channel.substring(CHANNEL_PREFIX.length());

            Map<String, Object> clientEvent = new HashMap<>(event);
            clientEvent.remove("instanceId");
            String notification = objectMapper.writeValueAsString(clientEvent);

            sendToLocalRoom(null, roomId, notification);
        } catch (Exception e) {
            log.error("[Signaling] Failed to handle Redis message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId   = session.getId();
        String roomId      = userSessionService.voiceGetRoom(sessionId);
        String transportId = userSessionService.voiceGetTransport(sessionId);
        String userId      = userSessionService.voiceGetUserId(sessionId);
        Set<String> producerIds = userSessionService.voiceGetProducers(sessionId);

        userSessionService.voiceCleanup(sessionId);

        if (transportId != null) {
            rabbitTemplate.convertAndSend(SIGNALING_EXCHANGE, "signal.media.closeTransport",
                    Map.of("transportId", transportId));
        }

        if (roomId != null) {
            for (String producerId : producerIds) {
                broadcastEvent(session, roomId, Map.of("type", "producerClosed", "producerId", producerId, "roomId", roomId));
            }
            Map<String, Object> leftEvent = new HashMap<>();
            leftEvent.put("type", "userLeft");
            leftEvent.put("sessionId", sessionId);
            leftEvent.put("roomId", roomId);
            if (userId != null) leftEvent.put("userId", userId);
            broadcastEvent(session, roomId, leftEvent);
            Set<WebSocketSession> roomSessions = rooms.get(roomId);
            if (roomSessions != null) {
                roomSessions.remove(session);
                if (roomSessions.isEmpty()) rooms.remove(roomId);
            }
        }
        log.info("[Signaling] Session {} (userId={}) disconnected from room {}", sessionId, userId, roomId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SignalingMessage msg = objectMapper.readValue(message.getPayload(), SignalingMessage.class);
        log.info("[Signaling] Received: type={}", msg.getType());

        String roomId = msg.getRoomId();

        if ("join".equals(msg.getType())) {
            rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
            String joiningUserId   = msg.getData() != null ? (String) msg.getData().get("userId")   : null;
            String joiningUsername = msg.getData() != null ? (String) msg.getData().get("username") : null;
            userSessionService.voiceJoin(session.getId(), roomId, joiningUserId, joiningUsername);

            // Always notify existing participants — sessionId is the reliable identifier
            Map<String, Object> joinedEvent = new HashMap<>();
            joinedEvent.put("type", "userJoined");
            joinedEvent.put("sessionId", session.getId());
            joinedEvent.put("roomId", roomId);
            if (joiningUserId != null)   joinedEvent.put("userId", joiningUserId);
            if (joiningUsername != null) joinedEvent.put("username", joiningUsername);
            broadcastEvent(session, roomId, joinedEvent);
        }

        // closeProducer: fire-and-forget to media server, broadcast immediately without waiting
        if ("closeProducer".equals(msg.getType())) {
            if (msg.getData() != null) {
                String producerId = (String) msg.getData().get("producerId");
                if (producerId != null) {
                    rabbitTemplate.convertAndSend(SIGNALING_EXCHANGE, "signal.media.closeProducer", msg.getData());
                    userSessionService.voiceRemoveProducer(session.getId(), producerId);
                    broadcastEvent(session, roomId, Map.of("type", "producerClosed", "producerId", producerId, "roomId", roomId));
                    log.info("[Signaling] closeProducer broadcast: producerId={}", producerId);
                }
            }
            return;
        }

        String routingKey = switch (msg.getType()) {
            case "join"             -> "signal.media.createRouter";
            case "createTransport"  -> "signal.media.createTransport";
            case "connectTransport" -> "signal.media.connectTransport";
            case "produce"          -> "signal.media.produce";
            case "consume"          -> "signal.media.consume";
            case "resume"           -> "signal.media.resume";
            default -> {
                log.warn("[Signaling] Unknown type: {}", msg.getType());
                yield null;
            }
        };

        if (routingKey == null) return;

        Object payload = msg.getData() != null ? msg.getData() : Map.of("roomId", roomId);
        Object response = rabbitTemplate.convertSendAndReceive(SIGNALING_EXCHANGE, routingKey, payload);

        if (response == null) {
            log.error("[Signaling] No response from media server for type: {}", msg.getType());
            session.sendMessage(new TextMessage("{\"success\":false,\"error\":\"No response from Media Server\"}"));
            return;
        }

        Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);

        if ("createTransport".equals(msg.getType()) && Boolean.TRUE.equals(responseMap.get("success"))) {
            userSessionService.voiceSetTransport(session.getId(), (String) responseMap.get("id"));
        }

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        // Late joiner: send userJoined + newProducer for each existing participant
        if ("join".equals(msg.getType())) {
            Set<WebSocketSession> roomSessions = rooms.get(roomId);
            if (roomSessions != null) {
                for (WebSocketSession existingSession : roomSessions) {
                    if (existingSession.getId().equals(session.getId())) continue;
                    String existingUserId    = userSessionService.voiceGetUserId(existingSession.getId());
                    String existingUsername  = userSessionService.voiceGetUsername(existingSession.getId());

                    // Inform late joiner about this existing participant
                    Map<String, Object> joinedEv = new HashMap<>();
                    joinedEv.put("type", "userJoined");
                    joinedEv.put("sessionId", existingSession.getId());
                    joinedEv.put("roomId", roomId);
                    if (existingUserId != null)   joinedEv.put("userId", existingUserId);
                    if (existingUsername != null) joinedEv.put("username", existingUsername);
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(joinedEv)));

                    // Inform late joiner about each producer this participant has
                    for (String producerId : userSessionService.voiceGetProducers(existingSession.getId())) {
                        Map<String, Object> producerEv = new HashMap<>();
                        producerEv.put("type", "newProducer");
                        producerEv.put("producerId", producerId);
                        producerEv.put("sessionId", existingSession.getId());
                        producerEv.put("roomId", roomId);
                        if (existingUserId != null)   producerEv.put("userId", existingUserId);
                        if (existingUsername != null) producerEv.put("username", existingUsername);
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(producerEv)));
                        log.info("[Signaling] Late joiner notified: producer={} sessionId={}", producerId, existingSession.getId());
                    }
                }
            }
        }

        if ("produce".equals(msg.getType())) {
            String producerId = (String) responseMap.get("id");
            if (producerId != null) {
                userSessionService.voiceAddProducer(session.getId(), producerId);
                String producerUserId   = userSessionService.voiceGetUserId(session.getId());
                String producerUsername = userSessionService.voiceGetUsername(session.getId());
                String kind = msg.getData() != null ? (String) msg.getData().get("kind") : null;
                Map<String, Object> producerEvent = new HashMap<>();
                producerEvent.put("type", "newProducer");
                producerEvent.put("producerId", producerId);
                producerEvent.put("sessionId", session.getId());
                producerEvent.put("roomId", roomId);
                if (kind != null)           producerEvent.put("kind", kind);
                if (producerUserId != null)   producerEvent.put("userId", producerUserId);
                if (producerUsername != null) producerEvent.put("username", producerUsername);
                broadcastEvent(session, roomId, producerEvent);
            }
        }
    }

    /**
     * Broadcasts an event to the room.
     * 1. Sends directly to local sessions on this instance (excluding sender).
     * 2. Publishes to Redis so other instances can relay to their local sessions.
     */
    private void broadcastEvent(WebSocketSession sender, String roomId, Map<String, Object> event) {
        try {
            String notification = objectMapper.writeValueAsString(event);
            sendToLocalRoom(sender, roomId, notification);

            Map<String, Object> redisEvent = new HashMap<>(event);
            redisEvent.put("instanceId", INSTANCE_ID);
            redisTemplate.convertAndSend(CHANNEL_PREFIX + roomId, objectMapper.writeValueAsString(redisEvent));
        } catch (Exception e) {
            log.error("[Signaling] Failed to broadcast event to room {}", roomId, e);
        }
    }

    /** Sends a pre-serialized notification to all open local sessions in a room, optionally excluding one. */
    private void sendToLocalRoom(WebSocketSession excluded, String roomId, String notification) {
        Set<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions == null) return;
        for (WebSocketSession s : roomSessions) {
            if (s.isOpen() && (excluded == null || !s.getId().equals(excluded.getId()))) {
                try { s.sendMessage(new TextMessage(notification)); }
                catch (Exception e) { log.error("[Signaling] Failed to send to session {}", s.getId(), e); }
            }
        }
    }
}
