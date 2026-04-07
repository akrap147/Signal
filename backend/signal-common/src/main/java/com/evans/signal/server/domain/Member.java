package com.evans.signal.server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Member {
    private Long id;
    private Long serverId; // ID 참조
    private Long userId;   // ID 참조
    private Role role;
    private LocalDateTime joinedAt;

    public static Member create(Long serverId, Long userId, Role role) {
        return Member.builder()
                .serverId(serverId)
                .userId(userId)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
    }
}
