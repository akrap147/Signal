package com.evans.signal.server.service;

import com.evans.signal.channel.domain.Category;
import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.service.port.CategoryRepository;
import com.evans.signal.channel.service.port.ChannelRepository;
import com.evans.signal.global.exception.CustomException;
import com.evans.signal.server.domain.Member;
import com.evans.signal.server.domain.Role;
import com.evans.signal.server.domain.Server;
import com.evans.signal.server.dto.ServerCreateDto;
import com.evans.signal.server.dto.response.SimpleServerResponse;
import com.evans.signal.server.exception.ServerErrorCode;
import com.evans.signal.server.service.port.MemberRepository;
import com.evans.signal.server.service.port.ServerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {

    @InjectMocks
    private ServerService serverService;

    @Mock private ServerRepository serverRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ChannelRepository channelRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private InviteService inviteService;

    @Test
    @DisplayName("서버 생성 성공: 서버, 멤버, 기본 카테고리/채널이 생성된다.")
    void createServer_success() {
        // given
        Long userId = 1L;
        ServerCreateDto dto = new ServerCreateDto("Test Server");

        // Mocks
        Server server = Server.builder().id(10L).name("Test Server").ownerId(userId).build();
        given(serverRepository.save(any(Server.class))).willReturn(server);
        given(categoryRepository.save(any(Category.class))).willReturn(Category.builder().id(100L).build()); // Mocking saves

        // when
        Long serverId = serverService.createServer(dto, userId);

        // then
        assertThat(serverId).isEqualTo(10L);
        verify(serverRepository).save(any(Server.class));
        verify(memberRepository).save(any(Member.class));
        verify(categoryRepository, times(3)).save(any(Category.class)); // 3 times actually
        verify(channelRepository, times(3)).save(any(Channel.class));   // 3 times actually
    }

    @Test
    @DisplayName("서버 가입 성공: 초대 코드로 서버 ID를 찾고 멤버를 저장한다.")
    void joinServer_success() {
        // given
        String inviteCode = "validCode";
        Long userId = 2L;
        Long serverId = 10L;

        given(inviteService.getServerIdByInviteCode(inviteCode)).willReturn(serverId);
        given(serverRepository.findById(serverId)).willReturn(Optional.of(Server.builder().id(serverId).build()));

        // when
        Long resultServerId = serverService.joinServer(inviteCode, userId);

        // then
        assertThat(resultServerId).isEqualTo(serverId);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("초대 코드 생성 성공: 방장이면 초대 코드를 반환한다.")
    void createInviteCode_success() {
        // given
        Long serverId = 10L;
        Long ownerId = 1L;
        Server server = Server.builder().id(serverId).ownerId(ownerId).build();

        given(serverRepository.findById(serverId)).willReturn(Optional.of(server));
        given(inviteService.createInvite(anyLong(), anyLong(), anyLong())).willReturn("newCode");

        // when
        String code = serverService.createInviteCode(serverId, ownerId);

        // then
        assertThat(code).isEqualTo("newCode");
    }

    @Test
    @DisplayName("초대 코드 생성 실패: 방장이 아니면 예외가 발생한다.")
    void createInviteCode_fail_notOwner() {
        // given
        Long serverId = 10L;
        Long userId = 2L; // Not owner
        Server server = Server.builder().id(serverId).ownerId(1L).build();

        given(serverRepository.findById(serverId)).willReturn(Optional.of(server));

        // when & then
        assertThatThrownBy(() -> serverService.createInviteCode(serverId, userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ServerErrorCode.NOT_OWNER.getMessage());
    }

    @Test
    @DisplayName("내 서버 목록 조회")
    void findAllMyServers_success() {
        // given
        Long userId = 1L;
        Member member = Member.builder().serverId(10L).userId(userId).build();
        Server server = Server.builder().id(10L).name("Test Server").build();

        given(memberRepository.findAllByUserId(userId)).willReturn(List.of(member));
        given(serverRepository.findAllById(List.of(10L))).willReturn(List.of(server));

        // when
        List<SimpleServerResponse> responses = serverService.findAllMyServers(userId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(10L);
    }
}
