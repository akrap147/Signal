package com.evans.signal.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServerCreateDto {
    private String name;
    private Long ownerId; // 로그인 구현 전이라 ID 직접 받음
}
