package com.evans.signal.server.service;

import com.evans.signal.channel.domain.Category;
import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.service.port.CategoryRepository;
import com.evans.signal.channel.service.port.ChannelRepository;
import com.evans.signal.server.domain.Member;
import com.evans.signal.server.domain.Server;
import com.evans.signal.server.dto.response.MemberResponse;
import com.evans.signal.server.dto.response.ServerDetailResponse;
import com.evans.signal.server.service.port.MemberRepository;
import com.evans.signal.server.service.port.ServerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {

    @Mock private ServerRepository serverRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ChannelRepository channelRepository;
    @InjectMocks private ServerService serverService;

    @Nested
    @DisplayName("서버 가입 테스트")
    class JoinTest {
        @Test
        @DisplayName("초대 코드로 서버 가입 성공")
        void joinServer_Success() {
            String inviteCode = "valid-code";
            Long userId = 100L;
            Server mockServer = Server.builder().id(1L).name("Test Server").inviteCode(inviteCode).build();

            given(serverRepository.findByInviteCode(inviteCode)).willReturn(Optional.of(mockServer));
            given(memberRepository.save(any(Member.class)))
                    .willAnswer(inv -> Member.builder().id(123L).build());

            Long result = serverService.joinServer(inviteCode, userId);

            assertThat(result).isEqualTo(123L);
        }

        @Test
        @DisplayName("유효하지 않은 초대 코드로 가입 실패")
        void joinServer_Fail_InvalidCode() {
            String invalidCode = "invalid-code";
            Long userId = 100L;

            given(serverRepository.findByInviteCode(invalidCode)).willReturn(Optional.empty());

            assertThatThrownBy(() -> serverService.joinServer(invalidCode, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid invite code");
        }
    }

    @Nested
    @DisplayName("서버 조회 테스트")
    class ReadTest {
        @Test
        @DisplayName("서버 상세 조회 성공")
        void getServerDetails_Success() {
            Long serverId = 1L;
            Server mockServer = Server.builder().id(serverId).name("Detail Server").ownerId(10L).build();
            Category cat1 = Category.builder().id(10L).serverId(serverId).name("General").displayOrder(0).build();
            Category cat2 = Category.builder().id(11L).serverId(serverId).name("Game").displayOrder(1).build();
            Channel ch1 = Channel.builder().id(100L).serverId(serverId).categoryId(10L).name("chat").type("TEXT").displayOrder(0).build();
            Channel ch2 = Channel.builder().id(101L).serverId(serverId).categoryId(11L).name("voice-room").type("VOICE").displayOrder(0).build();

            given(serverRepository.findById(serverId)).willReturn(Optional.of(mockServer));
            given(categoryRepository.findAllByServerId(serverId)).willReturn(List.of(cat1, cat2));
            given(channelRepository.findAllByServerId(serverId)).willReturn(List.of(ch1, ch2));

            ServerDetailResponse response = serverService.getServerDetails(serverId);

            assertThat(response.getCategories()).hasSize(2);
            assertThat(response.getCategories().get(0).getName()).isEqualTo("General");
            assertThat(response.getCategories().get(0).getChannels()).hasSize(1);
        }

        @Test
        @DisplayName("서버 멤버 목록 조회 성공")
        void getServerMembers_Success() {
            Long serverId = 1L;
            Member member1 = Member.builder().id(10L).serverId(serverId).userId(100L).role("OWNER").build();
            Member member2 = Member.builder().id(11L).serverId(serverId).userId(101L).role("MEMBER").build();

            given(memberRepository.findAllByServerId(serverId)).willReturn(List.of(member1, member2));

            List<MemberResponse> responses = serverService.getServerMembers(serverId);

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getRole()).isEqualTo("OWNER");
        }
    }

    @Nested
    @DisplayName("서버 관리 테스트")
    class ManageTest {
        @Test
        @DisplayName("서버 나가기 성공")
        void leaveServer_Success() {
            Long serverId = 1L;
            Long userId = 100L;
            Server server = Server.builder().id(serverId).ownerId(99L).build();

            given(serverRepository.findById(serverId)).willReturn(Optional.of(server));

            serverService.leaveServer(serverId, userId);

            verify(memberRepository).deleteByServerIdAndUserId(serverId, userId);
        }

        @Test
        @DisplayName("오너는 서버를 나갈 수 없음")
        void leaveServer_Fail_Owner() {
            Long serverId = 1L;
            Long userId = 100L;
            Server server = Server.builder().id(serverId).ownerId(userId).build();

            given(serverRepository.findById(serverId)).willReturn(Optional.of(server));

            assertThatThrownBy(() -> serverService.leaveServer(serverId, userId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("서버 삭제 성공 (오너)")
        void deleteServer_Success() {
            Long serverId = 1L;
            Long userId = 100L;
            Server server = Server.builder().id(serverId).ownerId(userId).build();

            given(serverRepository.findById(serverId)).willReturn(Optional.of(server));

            serverService.deleteServer(serverId, userId);

            verify(serverRepository).deleteById(serverId);
        }

        @Test
        @DisplayName("서버 삭제 실패 (오너 아님)")
        void deleteServer_Fail_NotOwner() {
            Long serverId = 1L;
            Long userId = 100L;
            Server server = Server.builder().id(serverId).ownerId(99L).build();

            given(serverRepository.findById(serverId)).willReturn(Optional.of(server));

            assertThatThrownBy(() -> serverService.deleteServer(serverId, userId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("멤버 추방 성공")
        void kickMember_Success() {
            Long serverId = 1L;
            Long ownerId = 100L;
            Long targetId = 101L;
            Server server = Server.builder().id(serverId).ownerId(ownerId).build();

            given(serverRepository.findById(serverId)).willReturn(Optional.of(server));

            serverService.kickMember(serverId, targetId, ownerId);

            verify(memberRepository).deleteByServerIdAndUserId(serverId, targetId);
        }
    }
}
