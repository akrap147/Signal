package com.evans.signal.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    @DisplayName("User 생성 성공 테스트")
    void create_success() {
        // given
        String email = "test@example.com";
        String password = "password123"; // TODO: 추후 암호화된 비밀번호 필요
        String username = "testuser";

        // when
        User user = User.create(email, password, username);

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getUsername()).isEqualTo(username);
    }

//    @Test
//    @DisplayName("비밀번호가 8자리 미만이면 실패")
//    void create_fail_password_length() {
//        assertThrows(IllegalArgumentException.class, () -> {
//            User.create("test@example.com", "1234", "testuser");
//        });
//    }
}
