package com.evans.signal.friendship.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDto {
    private Long friendId; // 친구 요청을 보낼 대상의 ID
}
