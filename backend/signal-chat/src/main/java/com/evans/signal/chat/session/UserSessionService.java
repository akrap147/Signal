package com.evans.signal.chat.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionService {

    // ── STOMP presence ──────────────────────────────────────────────────────
    private static final String SESSION_USER_KEY  = "session:";         // session:{id} -> userId
    private static final String USER_SESSIONS_KEY = "user:sessions:";   // user:sessions:{userId} -> Set<sessionId>
    private static final String ONLINE_USERS_KEY  = "online:users";     // Set<userId>

    // ── Voice signaling ──────────────────────────────────────────────────────
    private static final String VOICE_ROOM_KEY      = "voice:session:%s:room";       // -> roomId
    private static final String VOICE_TRANSPORT_KEY = "voice:session:%s:transport";  // -> transportId
    private static final String VOICE_PRODUCERS_KEY = "voice:session:%s:producers";  // -> Set<producerId>
    private static final String VOICE_USER_KEY      = "voice:session:%s:userId";     // -> userId
    private static final String VOICE_USERNAME_KEY  = "voice:session:%s:username";   // -> username

    private static final Duration SESSION_TTL = Duration.ofHours(2);
    private static final Duration VOICE_TTL   = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    // ── STOMP presence ───────────────────────────────────────────────────────

    public void connect(Long userId, String sessionId) {
        String userIdStr = String.valueOf(userId);
        redisTemplate.opsForValue().set(SESSION_USER_KEY + sessionId, userIdStr, SESSION_TTL);
        redisTemplate.opsForSet().add(USER_SESSIONS_KEY + userIdStr, sessionId);
        redisTemplate.expire(USER_SESSIONS_KEY + userIdStr, SESSION_TTL);
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userIdStr);
        log.debug("STOMP connect: userId={}, sessionId={}", userId, sessionId);
    }

    /** Removes the session. Returns true if this was the user's last session (went offline). */
    public boolean disconnect(String sessionId) {
        String userIdStr = redisTemplate.opsForValue().get(SESSION_USER_KEY + sessionId);
        if (userIdStr == null) return false;

        redisTemplate.delete(SESSION_USER_KEY + sessionId);
        redisTemplate.opsForSet().remove(USER_SESSIONS_KEY + userIdStr, sessionId);

        Long remaining = redisTemplate.opsForSet().size(USER_SESSIONS_KEY + userIdStr);
        boolean wentOffline = remaining == null || remaining == 0;
        if (wentOffline) redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userIdStr);

        log.debug("STOMP disconnect: userId={}, sessionId={}, wentOffline={}", userIdStr, sessionId, wentOffline);
        return wentOffline;
    }

    public Long getUserIdBySession(String sessionId) {
        String userIdStr = redisTemplate.opsForValue().get(SESSION_USER_KEY + sessionId);
        return userIdStr != null ? Long.parseLong(userIdStr) : null;
    }

    /** 주어진 userId 목록 중 실제 온라인인 것만 반환 */
    public Set<String> getOnlineAmong(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptySet();
        return userIds.stream()
                .map(String::valueOf)
                .filter(id -> Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, id)))
                .collect(Collectors.toSet());
    }

    public boolean isOnline(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, String.valueOf(userId)));
    }

    // ── Voice signaling ───────────────────────────────────────────────────────

    public void voiceJoin(String sessionId, String roomId, String userId, String username) {
        redisTemplate.opsForValue().set(String.format(VOICE_ROOM_KEY, sessionId), roomId, VOICE_TTL);
        if (userId != null) {
            redisTemplate.opsForValue().set(String.format(VOICE_USER_KEY, sessionId), userId, VOICE_TTL);
        }
        if (username != null) {
            redisTemplate.opsForValue().set(String.format(VOICE_USERNAME_KEY, sessionId), username, VOICE_TTL);
        }
        log.debug("Voice join: sessionId={}, roomId={}, userId={}", sessionId, roomId, userId);
    }

    public String voiceGetUserId(String sessionId) {
        return redisTemplate.opsForValue().get(String.format(VOICE_USER_KEY, sessionId));
    }

    public String voiceGetUsername(String sessionId) {
        return redisTemplate.opsForValue().get(String.format(VOICE_USERNAME_KEY, sessionId));
    }

    public void voiceSetTransport(String sessionId, String transportId) {
        redisTemplate.opsForValue().set(String.format(VOICE_TRANSPORT_KEY, sessionId), transportId, VOICE_TTL);
    }

    public void voiceAddProducer(String sessionId, String producerId) {
        String key = String.format(VOICE_PRODUCERS_KEY, sessionId);
        redisTemplate.opsForSet().add(key, producerId);
        redisTemplate.expire(key, VOICE_TTL);
    }

    public void voiceRemoveProducer(String sessionId, String producerId) {
        redisTemplate.opsForSet().remove(String.format(VOICE_PRODUCERS_KEY, sessionId), producerId);
    }

    public String voiceGetRoom(String sessionId) {
        return redisTemplate.opsForValue().get(String.format(VOICE_ROOM_KEY, sessionId));
    }

    public String voiceGetTransport(String sessionId) {
        return redisTemplate.opsForValue().get(String.format(VOICE_TRANSPORT_KEY, sessionId));
    }

    public Set<String> voiceGetProducers(String sessionId) {
        Set<String> producers = redisTemplate.opsForSet().members(String.format(VOICE_PRODUCERS_KEY, sessionId));
        return producers != null ? producers : Collections.emptySet();
    }

    public void voiceCleanup(String sessionId) {
        redisTemplate.delete(String.format(VOICE_ROOM_KEY, sessionId));
        redisTemplate.delete(String.format(VOICE_TRANSPORT_KEY, sessionId));
        redisTemplate.delete(String.format(VOICE_PRODUCERS_KEY, sessionId));
        redisTemplate.delete(String.format(VOICE_USER_KEY, sessionId));
        redisTemplate.delete(String.format(VOICE_USERNAME_KEY, sessionId));
        log.debug("Voice cleanup: sessionId={}", sessionId);
    }
}
