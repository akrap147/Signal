package com.evans.signal.user.domain;

import com.evans.signal.global.exception.CustomException;
import com.evans.signal.user.exception.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("유저 생성 성공")
    void create_success() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String username = "testUser";

        // when
        User user = User.create(email, password, username, passwordEncoder);

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(passwordEncoder.matches(password, user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("유저 생성 실패 - 잘못된 이메일 형식")
    void create_fail_invalid_email() {
        // given
        String email = "invalid-email";
        String password = "password123";
        String username = "testUser";

        // when & then
        assertThatThrownBy(() -> User.create(email, password, username, passwordEncoder))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_EMAIL_FORMAT);
    }

    @Test
    @DisplayName("유저 생성 실패 - 비밀번호 정책 위반 (8자 미만)")
    void create_fail_short_password() {
        // given
        String email = "test@example.com";
        String password = "short";
        String username = "testUser";

        // when & then
        assertThatThrownBy(() -> User.create(email, password, username, passwordEncoder))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_PASSWORD_FORMAT);
    }

    @Test
    @DisplayName("비밀번호 확인 성공")
    void checkPassword_success() {
        // given
        String rawPassword = "password123";
        User user = User.create("test@example.com", rawPassword, "user", passwordEncoder);

        // when
        boolean result = user.checkPassword(rawPassword, passwordEncoder);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("비밀번호 확인 실패")
    void checkPassword_fail() {
        // given
        User user = User.create("test@example.com", "password123", "user", passwordEncoder);

        // when
        boolean result = user.checkPassword("wrongPassword", passwordEncoder);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("프로필 업데이트 - 필드 변경")
    void updateProfile_success() {
        // given
        User user = User.create("test@example.com", "password123", "oldName", passwordEncoder);
        String newName = "newName";
        String newImage = "newImage.jpg";

        // when
        user.updateProfile(newName, newImage);

        // then
        assertThat(user.getUsername()).isEqualTo(newName);
        assertThat(user.getProfileImage()).isEqualTo(newImage);
    }

    @Test
    @DisplayName("프로필 업데이트 - null 입력 시 변경 없음")
    void updateProfile_ignore_null() {
        // given
        String oldName = "oldName";
        User user = User.create("test@example.com", "password123", oldName, passwordEncoder);

        // when
        user.updateProfile(null, null);

        // then
        assertThat(user.getUsername()).isEqualTo(oldName); // 변경되지 않아야 함
        assertThat(user.getProfileImage()).isNull(); // 초기 상태 null
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        // given
        User user = User.create("test@example.com", "password123", "user", passwordEncoder);
        String newPassword = "newPassword123";

        // when
        user.changePassword(newPassword, passwordEncoder);

        // then
        assertThat(passwordEncoder.matches(newPassword, user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 정책 위반")
    void changePassword_fail_policy() {
        // given
        User user = User.create("test@example.com", "password123", "user", passwordEncoder);
        String newPassword = "short";

        // when & then
        assertThatThrownBy(() -> user.changePassword(newPassword, passwordEncoder))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_PASSWORD_FORMAT);
    }
}
