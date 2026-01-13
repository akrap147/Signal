package com.evans.signal.server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Server {
    private Long id;
    private String name;
    private Long ownerId;
    private String inviteCode;
    private String iconImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
