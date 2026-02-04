package com.evans.signal.friendship.domain;

import com.evans.signal.global.exception.CustomException;
import com.evans.signal.friendship.exception.FriendshipErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FriendshipTest {

    @Test
    @DisplayName("친구 요청 생성 성공")
    void createRequest_success() {
        // given
        Long userId = 1L;
        Long friendId = 2L;

        // when
        Friendship friendship = Friendship.createRequest(userId, friendId);

        // then
        assertThat(friendship.getUserId()).isEqualTo(userId);
        assertThat(friendship.getFriendId()).isEqualTo(friendId);
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.PENDING);
    }

    @Test
    @DisplayName("친구 요청 생성 실패 - 자기 자신을 친구로 추가")
    void createRequest_fail_self() {
        // given
        Long userId = 1L;
        Long friendId = 1L;

        // when & then
        assertThatThrownBy(() -> Friendship.createRequest(userId, friendId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", FriendshipErrorCode.CANNOT_ADD_SELF_AS_FRIEND);
    }

    @Test
    @DisplayName("친구 요청 수락 성공")
    void accept_success() {
        // given
        Friendship friendship = Friendship.builder()
                .id(1L)
                .userId(1L)
                .friendId(2L)
                .status(FriendshipStatus.PENDING)
                .build();

        // when
        friendship.accept();

        // then
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    @DisplayName("친구 요청 수락 실패 - 이미 수락된 상태")
    void accept_fail_already_accepted() {
        // given
        Friendship friendship = Friendship.builder()
                .id(1L)
                .userId(1L)
                .friendId(2L)
                .status(FriendshipStatus.ACCEPTED)
                .build();

        // when & then
        assertThatThrownBy(friendship::accept)
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", FriendshipErrorCode.INVALID_FRIENDSHIP_STATUS);
    }

    @Test
    @DisplayName("역방향 친구 관계 생성 성공")
    void createReverseFriendship_success() {
        // given
        Friendship friendship = Friendship.builder()
                .id(1L)
                .userId(1L)
                .friendId(2L)
                .status(FriendshipStatus.ACCEPTED)
                .build();

        // when
        Friendship reverseFriendship = friendship.createReverseFriendship();

        // then
        assertThat(reverseFriendship.getUserId()).isEqualTo(2L);
        assertThat(reverseFriendship.getFriendId()).isEqualTo(1L);
        assertThat(reverseFriendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    @DisplayName("역방향 친구 관계 생성 실패 - pending 상태")
    void createReverseFriendship_fail_pending() {
        // given
        Friendship friendship = Friendship.builder()
                .id(1L)
                .userId(1L)
                .friendId(2L)
                .status(FriendshipStatus.PENDING)
                .build();

        // when & then
        assertThatThrownBy(friendship::createReverseFriendship)
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", FriendshipErrorCode.INVALID_FRIENDSHIP_STATUS);
    }

    @Test
    @DisplayName("친구 차단 성공")
    void block_success() {
        // given
        Friendship friendship = Friendship.builder()
                .id(1L)
                .userId(1L)
                .friendId(2L)
                .status(FriendshipStatus.ACCEPTED)
                .build();

        // when
        friendship.block();

        // then
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.BLOCKED);
    }

    @Test
    @DisplayName("요청자 확인 - 성공")
    void isRequester_success() {
        // given
        Long userId = 1L;
        Friendship friendship = Friendship.builder()
                .userId(userId)
                .friendId(2L)
                .status(FriendshipStatus.PENDING)
                .build();

        // when
        boolean result = friendship.isRequester(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("요청자 확인 - 실패")
    void isRequester_fail() {
        // given
        Friendship friendship = Friendship.builder()
                .userId(1L)
                .friendId(2L)
                .status(FriendshipStatus.PENDING)
                .build();

        // when
        boolean result = friendship.isRequester(2L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("수신자 확인 - 성공")
    void isReceiver_success() {
        // given
        Long friendId = 2L;
        Friendship friendship = Friendship.builder()
                .userId(1L)
                .friendId(friendId)
                .status(FriendshipStatus.PENDING)
                .build();

        // when
        boolean result = friendship.isReceiver(friendId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("수신자 확인 - 실패")
    void isReceiver_fail() {
        // given
        Friendship friendship = Friendship.builder()
                .userId(1L)
                .friendId(2L)
                .status(FriendshipStatus.PENDING)
                .build();

        // when
        boolean result = friendship.isReceiver(1L);

        // then
        assertThat(result).isFalse();
    }
}
