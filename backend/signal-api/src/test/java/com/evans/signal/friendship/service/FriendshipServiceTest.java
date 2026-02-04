package com.evans.signal.friendship.service;

import com.evans.signal.friendship.domain.Friendship;
import com.evans.signal.friendship.domain.FriendshipStatus;
import com.evans.signal.friendship.dto.FriendshipResponseDto;
import com.evans.signal.friendship.exception.FriendshipErrorCode;
import com.evans.signal.friendship.service.port.FriendshipRepository;
import com.evans.signal.global.exception.CustomException;
import com.evans.signal.user.domain.User;
import com.evans.signal.user.exception.UserErrorCode;
import com.evans.signal.user.service.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @InjectMocks
    private FriendshipService friendshipService;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("친구 요청 보내기 성공")
    void sendFriendRequest_success() {
        // given
        Long userId = 1L;
        Long friendId = 2L;
        User friend = User.builder()
                .id(friendId)
                .email("friend@example.com")
                .username("friend")
                .build();

        Friendship savedFriendship = Friendship.builder()
                .id(1L)
                .userId(userId)
                .friendId(friendId)
                .status(FriendshipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(friendId)).willReturn(Optional.of(friend));
        given(friendshipRepository.existsFriendshipBetween(userId, friendId)).willReturn(false);
        given(friendshipRepository.save(any(Friendship.class))).willReturn(savedFriendship);

        // when
        FriendshipResponseDto response = friendshipService.sendFriendRequest(userId, friendId);

        // then
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getFriendId()).isEqualTo(friendId);
        assertThat(response.getStatus()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(response.getFriendInfo()).isNotNull();
        assertThat(response.getFriendInfo().getUsername()).isEqualTo("friend");
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    @DisplayName("친구 요청 보내기 실패 - 사용자 없음")
    void sendFriendRequest_fail_user_not_found() {
        // given
        Long userId = 1L;
        Long friendId = 999L;
        given(userRepository.findById(friendId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> friendshipService.sendFriendRequest(userId, friendId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("친구 요청 보내기 실패 - 이미 친구 관계 존재")
    void sendFriendRequest_fail_already_exists() {
        // given
        Long userId = 1L;
        Long friendId = 2L;
        User friend = User.builder().id(friendId).build();

        given(userRepository.findById(friendId)).willReturn(Optional.of(friend));
        given(friendshipRepository.existsFriendshipBetween(userId, friendId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> friendshipService.sendFriendRequest(userId, friendId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", FriendshipErrorCode.FRIENDSHIP_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("친구 요청 수락 성공")
    void acceptFriendRequest_success() {
        // given
        Long userId = 2L; // 수락하는 사람 (B)
        Long requesterId = 1L; // 요청한 사람 (A)

        Friendship pendingFriendship = Friendship.builder()
                .id(1L)
                .userId(requesterId)
                .friendId(userId)
                .status(FriendshipStatus.PENDING)
                .build();

        given(friendshipRepository.findByUserIdAndFriendId(requesterId, userId))
                .willReturn(Optional.of(pendingFriendship));

        // when
        friendshipService.acceptFriendRequest(userId, requesterId);

        // then
        verify(friendshipRepository, times(2)).save(any(Friendship.class)); // 원본 + 역방향
        assertThat(pendingFriendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    @DisplayName("친구 요청 수락 실패 - 요청 없음")
    void acceptFriendRequest_fail_not_found() {
        // given
        Long userId = 2L;
        Long requesterId = 1L;

        given(friendshipRepository.findByUserIdAndFriendId(requesterId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> friendshipService.acceptFriendRequest(userId, requesterId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", FriendshipErrorCode.FRIENDSHIP_NOT_FOUND);
    }

    @Test
    @DisplayName("친구 삭제 성공")
    void removeFriendship_success() {
        // given
        Long userId = 1L;
        Long friendId = 2L;

        Friendship friendship1 = Friendship.builder()
                .userId(userId)
                .friendId(friendId)
                .build();

        Friendship friendship2 = Friendship.builder()
                .userId(friendId)
                .friendId(userId)
                .build();

        given(friendshipRepository.findByUserIdAndFriendId(userId, friendId))
                .willReturn(Optional.of(friendship1));
        given(friendshipRepository.findByUserIdAndFriendId(friendId, userId))
                .willReturn(Optional.of(friendship2));

        // when
        friendshipService.removeFriendship(userId, friendId);

        // then
        verify(friendshipRepository, times(2)).delete(any(Friendship.class));
    }

    @Test
    @DisplayName("내 친구 목록 조회 성공")
    void getMyFriends_success() {
        // given
        Long userId = 1L;
        User friend1 = User.builder().id(2L).username("friend1").email("f1@test.com").build();
        User friend2 = User.builder().id(3L).username("friend2").email("f2@test.com").build();

        Friendship friendship1 = Friendship.builder()
                .id(1L)
                .userId(userId)
                .friendId(2L)
                .status(FriendshipStatus.ACCEPTED)
                .build();

        Friendship friendship2 = Friendship.builder()
                .id(2L)
                .userId(userId)
                .friendId(3L)
                .status(FriendshipStatus.ACCEPTED)
                .build();

        given(friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED))
                .willReturn(Arrays.asList(friendship1, friendship2));
        given(userRepository.findById(2L)).willReturn(Optional.of(friend1));
        given(userRepository.findById(3L)).willReturn(Optional.of(friend2));

        // when
        List<FriendshipResponseDto> friends = friendshipService.getMyFriends(userId);

        // then
        assertThat(friends).hasSize(2);
        assertThat(friends.get(0).getFriendInfo().getUsername()).isEqualTo("friend1");
        assertThat(friends.get(1).getFriendInfo().getUsername()).isEqualTo("friend2");
    }

    @Test
    @DisplayName("내가 보낸 친구 요청 목록 조회 성공")
    void getMySentRequests_success() {
        // given
        Long userId = 1L;
        User friend = User.builder().id(2L).username("friend").email("f@test.com").build();

        Friendship friendship = Friendship.builder()
                .id(1L)
                .userId(userId)
                .friendId(2L)
                .status(FriendshipStatus.PENDING)
                .build();

        given(friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.PENDING))
                .willReturn(Arrays.asList(friendship));
        given(userRepository.findById(2L)).willReturn(Optional.of(friend));

        // when
        List<FriendshipResponseDto> requests = friendshipService.getMySentRequests(userId);

        // then
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getStatus()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(requests.get(0).getFriendInfo().getUsername()).isEqualTo("friend");
    }

    @Test
    @DisplayName("내가 받은 친구 요청 목록 조회 성공")
    void getMyReceivedRequests_success() {
        // given
        Long userId = 2L;
        User requester = User.builder().id(1L).username("requester").email("r@test.com").build();

        Friendship friendship = Friendship.builder()
                .id(1L)
                .userId(1L)
                .friendId(userId)
                .status(FriendshipStatus.PENDING)
                .build();

        given(friendshipRepository.findByFriendIdAndStatus(userId, FriendshipStatus.PENDING))
                .willReturn(Arrays.asList(friendship));
        given(userRepository.findById(1L)).willReturn(Optional.of(requester));

        // when
        List<FriendshipResponseDto> requests = friendshipService.getMyReceivedRequests(userId);

        // then
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getStatus()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(requests.get(0).getFriendInfo().getUsername()).isEqualTo("requester");
    }

    @Test
    @DisplayName("내 친구 목록 조회 - 빈 목록")
    void getMyFriends_empty() {
        // given
        Long userId = 1L;
        given(friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED))
                .willReturn(Arrays.asList());

        // when
        List<FriendshipResponseDto> friends = friendshipService.getMyFriends(userId);

        // then
        assertThat(friends).isEmpty();
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("내 친구 목록 조회 - 친구 정보 포함 검증")
    void getMyFriends_with_friend_info() {
        // given
        Long userId = 1L;
        User friend = User.builder()
                .id(2L)
                .email("friend@test.com")
                .username("friendName")
                .profileImage("profile.jpg")
                .build();

        Friendship friendship = Friendship.builder()
                .id(1L)
                .userId(userId)
                .friendId(2L)
                .status(FriendshipStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED))
                .willReturn(Arrays.asList(friendship));
        given(userRepository.findById(2L)).willReturn(Optional.of(friend));

        // when
        List<FriendshipResponseDto> friends = friendshipService.getMyFriends(userId);

        // then
        assertThat(friends).hasSize(1);
        FriendshipResponseDto response = friends.get(0);
        assertThat(response.getFriendInfo()).isNotNull();
        assertThat(response.getFriendInfo().getId()).isEqualTo(2L);
        assertThat(response.getFriendInfo().getEmail()).isEqualTo("friend@test.com");
        assertThat(response.getFriendInfo().getUsername()).isEqualTo("friendName");
        assertThat(response.getFriendInfo().getProfileImage()).isEqualTo("profile.jpg");
    }

    @Test
    @DisplayName("내가 보낸 친구 요청 목록 조회 - 빈 목록")
    void getMySentRequests_empty() {
        // given
        Long userId = 1L;
        given(friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.PENDING))
                .willReturn(Arrays.asList());

        // when
        List<FriendshipResponseDto> requests = friendshipService.getMySentRequests(userId);

        // then
        assertThat(requests).isEmpty();
    }

    @Test
    @DisplayName("내가 보낸 친구 요청 목록 조회 - 여러 요청")
    void getMySentRequests_multiple() {
        // given
        Long userId = 1L;
        User friend1 = User.builder().id(2L).username("friend1").build();
        User friend2 = User.builder().id(3L).username("friend2").build();
        User friend3 = User.builder().id(4L).username("friend3").build();

        Friendship req1 = Friendship.builder().id(1L).userId(userId).friendId(2L).status(FriendshipStatus.PENDING).build();
        Friendship req2 = Friendship.builder().id(2L).userId(userId).friendId(3L).status(FriendshipStatus.PENDING).build();
        Friendship req3 = Friendship.builder().id(3L).userId(userId).friendId(4L).status(FriendshipStatus.PENDING).build();

        given(friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.PENDING))
                .willReturn(Arrays.asList(req1, req2, req3));
        given(userRepository.findById(2L)).willReturn(Optional.of(friend1));
        given(userRepository.findById(3L)).willReturn(Optional.of(friend2));
        given(userRepository.findById(4L)).willReturn(Optional.of(friend3));

        // when
        List<FriendshipResponseDto> requests = friendshipService.getMySentRequests(userId);

        // then
        assertThat(requests).hasSize(3);
        assertThat(requests).allMatch(r -> r.getStatus() == FriendshipStatus.PENDING);
        assertThat(requests).allMatch(r -> r.getUserId().equals(userId));
    }

    @Test
    @DisplayName("내가 받은 친구 요청 목록 조회 - 빈 목록")
    void getMyReceivedRequests_empty() {
        // given
        Long userId = 2L;
        given(friendshipRepository.findByFriendIdAndStatus(userId, FriendshipStatus.PENDING))
                .willReturn(Arrays.asList());

        // when
        List<FriendshipResponseDto> requests = friendshipService.getMyReceivedRequests(userId);

        // then
        assertThat(requests).isEmpty();
    }

    @Test
    @DisplayName("내가 받은 친구 요청 목록 조회 - 여러 요청")
    void getMyReceivedRequests_multiple() {
        // given
        Long userId = 5L;
        User requester1 = User.builder().id(1L).username("requester1").build();
        User requester2 = User.builder().id(2L).username("requester2").build();

        Friendship req1 = Friendship.builder().id(1L).userId(1L).friendId(userId).status(FriendshipStatus.PENDING).build();
        Friendship req2 = Friendship.builder().id(2L).userId(2L).friendId(userId).status(FriendshipStatus.PENDING).build();

        given(friendshipRepository.findByFriendIdAndStatus(userId, FriendshipStatus.PENDING))
                .willReturn(Arrays.asList(req1, req2));
        given(userRepository.findById(1L)).willReturn(Optional.of(requester1));
        given(userRepository.findById(2L)).willReturn(Optional.of(requester2));

        // when
        List<FriendshipResponseDto> requests = friendshipService.getMyReceivedRequests(userId);

        // then
        assertThat(requests).hasSize(2);
        assertThat(requests).allMatch(r -> r.getStatus() == FriendshipStatus.PENDING);
        assertThat(requests).allMatch(r -> r.getFriendId().equals(userId));
        assertThat(requests.get(0).getFriendInfo().getUsername()).isEqualTo("requester1");
        assertThat(requests.get(1).getFriendInfo().getUsername()).isEqualTo("requester2");
    }

    @Test
    @DisplayName("친구 목록 조회 - pending 상태는 제외됨")
    void getMyFriends_excludes_pending() {
        // given
        Long userId = 1L;
        User friend = User.builder().id(2L).username("friend").build();

        Friendship acceptedFriendship = Friendship.builder()
                .id(1L)
                .userId(userId)
                .friendId(2L)
                .status(FriendshipStatus.ACCEPTED)
                .build();

        // ACCEPTED 상태만 조회하므로 PENDING은 자동으로 제외됨
        given(friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED))
                .willReturn(Arrays.asList(acceptedFriendship));
        given(userRepository.findById(2L)).willReturn(Optional.of(friend));

        // when
        List<FriendshipResponseDto> friends = friendshipService.getMyFriends(userId);

        // then
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    @DisplayName("친구 조회 실패 - 친구 사용자 정보 없음")
    void getMyFriends_fail_friend_not_found() {
        // given
        Long userId = 1L;
        Friendship friendship = Friendship.builder()
                .id(1L)
                .userId(userId)
                .friendId(999L) // 존재하지 않는 사용자
                .status(FriendshipStatus.ACCEPTED)
                .build();

        given(friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED))
                .willReturn(Arrays.asList(friendship));
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> friendshipService.getMyFriends(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
    }
}
