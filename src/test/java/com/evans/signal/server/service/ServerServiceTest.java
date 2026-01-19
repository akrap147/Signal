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

    @Mock
    private com.evans.signal.channel.service.port.CategoryRepository categoryRepository;

    @Mock
    private com.evans.signal.channel.service.port.ChannelRepository channelRepository;

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
                            .id(123L) 
                            .serverId(member.getServerId())
                            .userId(member.getUserId())
                            .role(member.getRole())
                            .build(); 
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

    @Test
    @DisplayName("서버 상세 조회 성공 - 카테고리/채널 그룹핑 확인")
    void getServerDetails_Success() {
        // given
        Long serverId = 1L;
        
        // 1. Mock Server
        Server mockServer = Server.builder()
                .id(serverId)
                .name("Detail Server")
                .ownerId(10L)
                .inviteCode("CODE")
                .build();
        
        // 2. Mock Categories
        com.evans.signal.channel.domain.Category cat1 = com.evans.signal.channel.domain.Category.builder()
                .id(10L).serverId(serverId).name("General").displayOrder(0).build();
        com.evans.signal.channel.domain.Category cat2 = com.evans.signal.channel.domain.Category.builder()
                .id(11L).serverId(serverId).name("Game").displayOrder(1).build();

        // 3. Mock Channels
        com.evans.signal.channel.domain.Channel ch1 = com.evans.signal.channel.domain.Channel.builder()
                .id(100L).serverId(serverId).categoryId(10L).name("chat").type("TEXT").displayOrder(0).build();
        com.evans.signal.channel.domain.Channel ch2 = com.evans.signal.channel.domain.Channel.builder()
                .id(101L).serverId(serverId).categoryId(11L).name("voice-room").type("VOICE").displayOrder(0).build();

        given(serverRepository.findById(serverId)).willReturn(Optional.of(mockServer));
        given(categoryRepository.findAllByServerId(serverId)).willReturn(java.util.List.of(cat1, cat2));
        given(channelRepository.findAllByServerId(serverId)).willReturn(java.util.List.of(ch1, ch2));

        // when
        com.evans.signal.server.dto.response.ServerDetailResponse response = serverService.getServerDetails(serverId);

        // then
        assertThat(response.getId()).isEqualTo(serverId);
        assertThat(response.getName()).isEqualTo("Detail Server");
        assertThat(response.getCategories()).hasSize(2);
        
        // Category 1 검증
        assertThat(response.getCategories().get(0).getName()).isEqualTo("General");
        assertThat(response.getCategories().get(0).getChannels()).hasSize(1);
        assertThat(response.getCategories().get(0).getChannels().get(0).getName()).isEqualTo("chat");

        // Category 2 검증
        assertThat(response.getCategories().get(1).getName()).isEqualTo("Game");
        assertThat(response.getCategories().get(1).getChannels()).hasSize(1);
        assertThat(response.getCategories().get(1).getChannels().get(0).getName()).isEqualTo("voice-room");
    }
}
