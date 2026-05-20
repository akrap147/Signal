package com.evans.signal.chat.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceEventListener {

    private static final String PRESENCE_TOPIC = "/topic/presence";
    private static final int OFFLINE_GRACE_SECONDS = 6;

    private final UserSessionService userSessionService;
    private final SimpMessageSendingOperations messagingTemplate;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @EventListener
    public void handleConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getSessionAttributes() == null) return;

        Long userId = (Long) accessor.getSessionAttributes().get(StompSessionInterceptor.SESSION_USER_ID_ATTR);
        if (userId == null) return;

        messagingTemplate.convertAndSend(PRESENCE_TOPIC,
                new PresenceStatusDto(userId, PresenceStatusDto.Status.ONLINE));
        log.debug("Presence ONLINE broadcast: userId={}", userId);
    }

    @EventListener
    public void handleDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        Long userId = userSessionService.getUserIdBySession(sessionId);
        boolean wentOffline = userSessionService.disconnect(sessionId);

        if (!wentOffline || userId == null) return;

        // 새로고침 시 즉시 재연결되므로 유예 시간 후에 아직 오프라인인지 확인
        final Long finalUserId = userId;
        scheduler.schedule(() -> {
            if (!userSessionService.isOnline(finalUserId)) {
                messagingTemplate.convertAndSend(PRESENCE_TOPIC,
                        new PresenceStatusDto(finalUserId, PresenceStatusDto.Status.OFFLINE));
                log.debug("Presence OFFLINE broadcast: userId={}", finalUserId);
            }
        }, OFFLINE_GRACE_SECONDS, TimeUnit.SECONDS);
    }
}
