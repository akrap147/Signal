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
    private String role;
    private LocalDateTime joinedAt;
}
