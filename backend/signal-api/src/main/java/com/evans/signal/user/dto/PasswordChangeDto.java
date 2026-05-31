package com.evans.signal.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordChangeDto {
    private String currentPassword;
    private String newPassword;
}
