package com.evans.signal.user.infrastructure;

import com.evans.signal.user.domain.User;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .username(entity.getUsername())
                .profileImage(entity.getProfileImage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) return null;
        return UserEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .username(domain.getUsername())
                .profileImage(domain.getProfileImage())
                .build();
    }
}
