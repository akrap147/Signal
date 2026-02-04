package com.evans.signal.server.service;

import com.evans.signal.server.service.InviteService;
import com.evans.signal.server.exception.ServerErrorCode;
import com.evans.signal.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisInviteServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisInviteService redisInviteService;

    @BeforeEach
    void setUp() {
        redisInviteService = new RedisInviteService(redisTemplate);
    }

    @Test
    @DisplayName("초대 코드 생성 성공: 기존 코드가 없을 때 새로 생성한다.")
    void createInvite_success_new() {
        // given
        Long serverId = 1L;
        Long userId = 100L;
        Long ttl = 3600L;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn(null); // 기존 코드 없음

        // when
        String inviteCode = redisInviteService.createInvite(serverId, userId, ttl);

        // then
        assertThat(inviteCode).isNotNull().hasSize(8);
        // 잘 들어가 있는지 확인한다.
        verify(valueOperations).set(eq("server:invite:" + inviteCode), eq(String.valueOf(serverId)), eq(ttl), eq(TimeUnit.SECONDS));
        verify(valueOperations).set(eq("server:active_invite:" + serverId + ":" + userId), eq(inviteCode), eq(ttl), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("초대 코드 생성 성공: 기존 코드가 있을 때 기존 코드를 반환한다.")
    void createInvite_success_existing() {
        // given
        Long serverId = 1L;
        Long userId = 100L;
        Long ttl = 3600L;
        String existingCode = "abcdefgh";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("server:active_invite:" + serverId + ":" + userId)).willReturn(existingCode);

        // when
        String inviteCode = redisInviteService.createInvite(serverId, userId, ttl);

        // then
        assertThat(inviteCode).isEqualTo(existingCode);
    }

    @Test
    @DisplayName("초대 코드로 서버 ID 조회 성공")
    void getServerIdByInviteCode_success() {
        // given
        String inviteCode = "abcdefgh";
        Long serverId = 1L;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("server:invite:" + inviteCode)).willReturn(String.valueOf(serverId));

        // when
        Long result = redisInviteService.getServerIdByInviteCode(inviteCode);

        // then
        assertThat(result).isEqualTo(serverId);
    }

    @Test
    @DisplayName("초대 코드로 서버 ID 조회 실패: 유효하지 않거나 만료된 코드")
    void getServerIdByInviteCode_fail_invalid() {
        // given
        String inviteCode = "invalid";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("server:invite:" + inviteCode)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> redisInviteService.getServerIdByInviteCode(inviteCode))
                .isInstanceOf(CustomException.class)
                .hasMessage(ServerErrorCode.INVALID_INVITE_CODE.getMessage());
    }
}
