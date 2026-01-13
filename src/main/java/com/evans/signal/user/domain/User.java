package com.evans.signal.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String password;
    private String username;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
