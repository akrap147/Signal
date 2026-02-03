package com.evans.signal.user.domain;

import com.evans.signal.global.exception.CustomException;
import com.evans.signal.user.exception.UserErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private Long id;
    private String email;
    private String password; // encoded
    private String username;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updateProfile(String username, String profileImage) {
        if (username != null) this.username = username;
        if (profileImage != null) this.profileImage = profileImage;
    }

    public void changePassword(String newPassword, PasswordEncoder passwordEncoder) {
        validatePasswordPolicy(newPassword);
        this.password = passwordEncoder.encode(newPassword);
    }

    public static User create(String email, String rawPassword, String username, PasswordEncoder passwordEncoder) {
        validateEmailFormat(email);
        validatePasswordPolicy(rawPassword);

        String encodedPassword = passwordEncoder.encode(rawPassword);
        // createdAt, updatedAt은 JPA Auditing에 의해 자동 설정되므로 null로 전달
        return new User(null, email, encodedPassword, username, null, null, null);
    }

    private static void validateEmailFormat(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new CustomException(UserErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    private static void validatePasswordPolicy(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new CustomException(UserErrorCode.INVALID_PASSWORD_FORMAT);
        }
    }

    public boolean checkPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
}
