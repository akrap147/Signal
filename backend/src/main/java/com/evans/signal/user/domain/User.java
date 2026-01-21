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

    public static User create(String email, String password, String username) {
        // TODO: 여기서 이메일 검증, 비밀번호 정책 검사 등을 수행 (Domain Logic)
        return User.builder()
                .email(email)
                .password(password)
                .username(username)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public boolean checkPassword(String plainPassword) {
        // 나중에 PasswordEncoder를 도입하면 여기서 match 로직을 변경하면 됨
        return this.password.equals(plainPassword);
    }
}
