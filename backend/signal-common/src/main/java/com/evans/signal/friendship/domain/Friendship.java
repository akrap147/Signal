package com.evans.signal.friendship.domain;

import com.evans.signal.global.exception.CustomException;
import com.evans.signal.friendship.exception.FriendshipErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Friendship {

    private Long id;
    private Long userId;
    private Long friendId;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    //  친구 요청 생성 (pending 상태)
    public static Friendship createRequest(Long userId, Long friendId) {
        validateDifferentUsers(userId, friendId);

        return Friendship.builder()
                .userId(userId)
                .friendId(friendId)
                .status(FriendshipStatus.PENDING)
                .build();
    }

    /**
     * 친구 요청 수락
     * - 기존 요청의 상태를 accepted로 변경
     */
    public void accept() {
        if (this.status != FriendshipStatus.PENDING) {
            throw new CustomException(FriendshipErrorCode.INVALID_FRIENDSHIP_STATUS);
        }
        this.status = FriendshipStatus.ACCEPTED;
    }

    /**
     * 친구 차단
     */
    public void block() {
        this.status = FriendshipStatus.BLOCKED;
    }

    /**
     * 역방향 친구 관계 생성 (수락 시 사용)
     * B가 A의 요청을 수락하면 (B, A, accepted) 생성
     */
    public Friendship createReverseFriendship() {
        if (this.status != FriendshipStatus.ACCEPTED) {
            throw new CustomException(FriendshipErrorCode.INVALID_FRIENDSHIP_STATUS);
        }

        return Friendship.builder()
                .userId(this.friendId)
                .friendId(this.userId)
                .status(FriendshipStatus.ACCEPTED)
                .build();
    }

    private static void validateDifferentUsers(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new CustomException(FriendshipErrorCode.CANNOT_ADD_SELF_AS_FRIEND);
        }
    }

    /**
     * 요청자인지 확인
     */
    public boolean isRequester(Long userId) {
        return this.userId.equals(userId);
    }

    /**
     * 수신자인지 확인
     */
    public boolean isReceiver(Long userId) {
        return this.friendId.equals(userId);
    }
}
