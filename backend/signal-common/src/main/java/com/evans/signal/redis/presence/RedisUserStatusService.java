package com.evans.signal.redis.presence;


import com.evans.signal.redis.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisUserStatusService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long TTL_SECONDS =60;

    // 쓰기
    public void setOnline(Long userId) {
        redisTemplate.opsForValue()
                .set(RedisKeyConstants.presence(userId), "online",
                        TTL_SECONDS, TimeUnit.SECONDS);
    }

    public void setOffline(Long userId){
        redisTemplate.delete(RedisKeyConstants.presence(userId));
    }

    public void refresh(Long userId){
        redisTemplate.expire(RedisKeyConstants.presence(userId),
                TTL_SECONDS, TimeUnit.SECONDS);
    }

    // 읽기
    // 한개 조화
    public boolean isOnline(Long userId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(RedisKeyConstants.presence(userId))
        );    }

    // 여러개 조회.
    public Map<Long, Boolean> getPresenceMap(List<Long> userIds) {
        List<String> keys = userIds.stream()
                .map(RedisKeyConstants::presence)
                .toList();

        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, Boolean> result = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            result.put(userIds.get(i), values.get(i) != null);
        }
        return result;
    }


}
