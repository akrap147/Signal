package com.evans.signal.chat.listener;

import com.evans.signal.presence.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        // TODO: JWT 연결 후 userId 추출하여 presenceService.setOnline(userId) 호출
        log.debug("WebSocket 연결: sessionId={}", getSessionId(event.getMessage().getHeaders()));
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        // TODO: JWT 연결 후 userId 추출하여 presenceService.setOffline(userId) 호출
        log.debug("WebSocket 종료: sessionId={}", event.getSessionId());
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (destination == null) return;

        Long roomId = parseRoomId(destination);
        if (roomId == null) return;

        // TODO: JWT 연결 후 userId 추출하여 presenceService.enterRoom(userId, roomId) 호출
        log.debug("방 입장: destination={}", destination);
    }

    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        // TODO: JWT 연결 후 userId 추출하여 presenceService.leaveRoom(userId) 호출
        log.debug("방 퇴장: sessionId={}", StompHeaderAccessor.wrap(event.getMessage()).getSessionId());
    }

    private Long parseRoomId(String destination) {
        try {
            if (destination.startsWith("/topic/channel.")) {
                return Long.parseLong(destination.replace("/topic/channel.", ""));
            }
            if (destination.startsWith("/topic/dm.")) {
                return Long.parseLong(destination.replace("/topic/dm.", ""));
            }
        } catch (NumberFormatException e) {
            log.warn("roomId 파싱 실패: {}", destination);
        }
        return null;
    }

    private String getSessionId(java.util.Map<String, Object> headers) {
        Object sessionId = headers.get("simpSessionId");
        return sessionId != null ? sessionId.toString() : "unknown";
    }
}
