package com.evans.signal.server.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.evans.signal.server.domain.Role;

@Getter
@Builder
public class MemberResponse {
    private Long id;
    private Long userId; // 실제 유저 ID
    private Role role; // OWNER, MEMBER etc.
    // 추후 nickname, avatarUrl 등 추가 예정
}
