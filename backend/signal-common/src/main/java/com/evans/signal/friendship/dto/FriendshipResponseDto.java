package com.evans.signal.friendship.dto;

import com.evans.signal.friendship.domain.Friendship;
import com.evans.signal.friendship.domain.FriendshipStatus;
import com.evans.signal.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FriendshipResponseDto {
    private Long id;
    private Long userId;
    private Long friendId;
    private FriendshipStatus status;
    private UserResponseDto friendInfo; // 친구의 상세 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FriendshipResponseDto from(Friendship friendship) {
        return FriendshipResponseDto.builder()
                .id(friendship.getId())
                .userId(friendship.getUserId())
                .friendId(friendship.getFriendId())
                .status(friendship.getStatus())
                .createdAt(friendship.getCreatedAt())
                .updatedAt(friendship.getUpdatedAt())
                .build();
    }

    public static FriendshipResponseDto from(Friendship friendship, UserResponseDto friendInfo) {
        return FriendshipResponseDto.builder()
                .id(friendship.getId())
                .userId(friendship.getUserId())
                .friendId(friendship.getFriendId())
                .status(friendship.getStatus())
                .friendInfo(friendInfo)
                .createdAt(friendship.getCreatedAt())
                .updatedAt(friendship.getUpdatedAt())
                .build();
    }
}
