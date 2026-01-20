package com.evans.signal.server.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    private Long id;
    private Long userId; // 실제 유저 ID
    private String role; // OWNER, MEMBER etc.
    // 추후 nickname, avatarUrl 등 추가 예정
}
