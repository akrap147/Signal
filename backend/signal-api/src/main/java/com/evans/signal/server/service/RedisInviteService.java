package com.evans.signal.server.service;

import com.evans.signal.global.exception.CustomException;
import com.evans.signal.server.exception.ServerErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisInviteService implements InviteService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String INVITE_KEY_PREFIX = "server:invite:";
    private static final String ACTIVE_INVITE_KEY_PREFIX = "server:active_invite:";

    @Override
    public String createInvite(Long serverId, Long userId, Long ttlSeconds) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // 1. 해당 유저가 해당 서버에 대해 만든 유효한 초대 코드가 있는지 확인
        String activeInviteKey = ACTIVE_INVITE_KEY_PREFIX + serverId + ":" + userId;
        String existingCode = ops.get(activeInviteKey);

        if (existingCode != null) {
            return existingCode;
        }

        // 2. 없으면 새로 생성
        String inviteCode = UUID.randomUUID().toString().substring(0, 8); // 8자리 랜덤 문자열
        String inviteKey = INVITE_KEY_PREFIX + inviteCode;

        // 3. Redis 저장 (초대 코드 조회용 + 중복 생성 방지용)
        ops.set(inviteKey, String.valueOf(serverId), ttlSeconds, TimeUnit.SECONDS);
        ops.set(activeInviteKey, inviteCode, ttlSeconds, TimeUnit.SECONDS);

        return inviteCode;
    }

    @Override
    public Long getServerIdByInviteCode(String inviteCode) {
        String key = INVITE_KEY_PREFIX + inviteCode;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String serverIdStr = ops.get(key);

        if (serverIdStr == null) {
            throw new CustomException(ServerErrorCode.INVALID_INVITE_CODE);
        }

        return Long.parseLong(serverIdStr);
    }
}
