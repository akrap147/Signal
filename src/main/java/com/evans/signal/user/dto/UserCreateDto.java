package com.evans.signal.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserCreateDto {
    private String email;
    private String password;
    private String username;

    public UserCreateDto(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }
}
