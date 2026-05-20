package com.evans.signal.chat.session;

import com.evans.signal.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompSessionInterceptor implements ChannelInterceptor {

    static final String SESSION_USER_ID_ATTR = "userId";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserSessionService userSessionService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) return message;

        if (accessor.getCommand() == StompCommand.CONNECT) {
            handleConnect(accessor);
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            log.warn("WS CONNECT rejected: invalid or missing JWT, sessionId={}", accessor.getSessionId());
            return;
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String sessionId = accessor.getSessionId();

        // 세션 속성에 userId 저장 → PresenceEventListener에서 꺼내 씀
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put(SESSION_USER_ID_ATTR, userId);
        }

        userSessionService.connect(userId, sessionId);
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String bearer = accessor.getFirstNativeHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
