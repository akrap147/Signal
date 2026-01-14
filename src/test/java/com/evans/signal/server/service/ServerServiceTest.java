package com.evans.signal.server.service;

import com.evans.signal.server.domain.Member;
import com.evans.signal.server.domain.Server;
import com.evans.signal.server.service.port.MemberRepository;
import com.evans.signal.server.service.port.ServerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ServerService serverService;

    @Test
    @DisplayName("초대 코드로 서버 가입 성공")
    void joinServer_Success() {
        // given
        String inviteCode = "valid-code";
        Long userId = 100L;
        Long serverId = 1L;

        // Mock Server (가짜 서버 객체 생성)
        Server mockServer = Server.builder()
                .id(serverId)
                .name("Test Server")
                .ownerId(99L)
                .inviteCode(inviteCode)
                .build();

        // Mocking behavior
        given(serverRepository.findByInviteCode(inviteCode))
                .willReturn(Optional.of(mockServer));

        given(memberRepository.save(any(Member.class)))
                .willAnswer(invocation -> {
                    Member member = invocation.getArgument(0);
                    // 저장 후 ID가 123L인 멤버를 반환한다고 가정
                    return Member.builder()
                            .id(123L) // Builder가 있다면
                            .serverId(member.getServerId())
                            .userId(member.getUserId())
                            .role(member.getRole())
                            .build(); 
                    // 주의: Member 도메인에 Builder가 있었나? 확인 필요.
                    // 없으면 Mock 객해 반환.
                });

        // when
        Long joinedMemberId = serverService.joinServer(inviteCode, userId);

        // then
        assertThat(joinedMemberId).isEqualTo(123L);
        verify(serverRepository).findByInviteCode(inviteCode);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("유효하지 않은 초대 코드로 가입 실패")
    void joinServer_Fail_InvalidCode() {
        // given
        String invalidCode = "invalid-code";
        Long userId = 100L;

        given(serverRepository.findByInviteCode(invalidCode))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> serverService.joinServer(invalidCode, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid invite code");
    }
}
