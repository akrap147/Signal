package com.evans.signal.presence;

import com.evans.signal.redis.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final StringRedisTemplate redisTemplate;

    public void setOnline(Long userId) {
        redisTemplate.opsForValue().set(RedisKeys.presenceOnline(userId), "online");
    }

    public void setOffline(Long userId) {
        redisTemplate.delete(RedisKeys.presenceOnline(userId));
        redisTemplate.delete(RedisKeys.presenceRoom(userId));
    }

    public void enterRoom(Long userId, Long roomId) {
        redisTemplate.opsForValue().set(RedisKeys.presenceRoom(userId), String.valueOf(roomId));
    }

    public void leaveRoom(Long userId) {
        redisTemplate.delete(RedisKeys.presenceRoom(userId));
    }

    public boolean isOnline(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeys.presenceOnline(userId)));
    }

    public boolean isInRoom(Long userId, Long roomId) {
        String activeRoom = redisTemplate.opsForValue().get(RedisKeys.presenceRoom(userId));
        return String.valueOf(roomId).equals(activeRoom);
    }
}
