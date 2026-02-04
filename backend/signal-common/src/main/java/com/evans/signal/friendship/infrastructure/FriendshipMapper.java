package com.evans.signal.friendship.infrastructure;

import com.evans.signal.friendship.domain.Friendship;
import com.evans.signal.friendship.infrastructure.entity.FriendshipEntity;

public class FriendshipMapper {

    public static Friendship toDomain(FriendshipEntity entity) {
        if (entity == null) return null;
        return Friendship.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .friendId(entity.getFriendId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static FriendshipEntity toEntity(Friendship domain) {
        if (domain == null) return null;
        return FriendshipEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .friendId(domain.getFriendId())
                .status(domain.getStatus())
                .build();
    }
}
