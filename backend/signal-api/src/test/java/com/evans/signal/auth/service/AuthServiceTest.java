package com.evans.signal.auth.service;

import com.evans.signal.auth.jwt.JwtTokenProvider;
import com.evans.signal.global.exception.CustomException;
import com.evans.signal.user.domain.User;
import com.evans.signal.user.dto.LoginResponseDto;
import com.evans.signal.user.exception.UserErrorCode;
import com.evans.signal.user.service.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        String email = "test@example.com";
        String password = "password";
        User user = User.builder()
                .id(1L)
                .email(email)
                .password("encodedPassword")
                .username("testUser")
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, "encodedPassword")).willReturn(true);
        given(jwtTokenProvider.createToken(user.getId(), user.getUsername())).willReturn("accessToken");

        // when
        LoginResponseDto response = authService.login(email, password);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.userId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_password_mismatch() {
        // given
        String email = "test@example.com";
        String password = "wrongPassword";
        User user = User.builder()
                .id(1L)
                .email(email)
                .password("encodedPassword")
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        // given
        Long userId = 1L;
        String currentPassword = "oldPassword";
        String newPassword = "newPassword";
        User user = User.builder().id(userId).password("encodedOldPassword").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(currentPassword, "encodedOldPassword")).willReturn(true);
        given(passwordEncoder.encode(newPassword)).willReturn("encodedNewPassword");

        // when
        authService.changePassword(userId, currentPassword, newPassword);

        // then
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_fail_mismatch() {
        // given
        Long userId = 1L;
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword";
        User user = User.builder().id(userId).password("encodedOldPassword").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(currentPassword, "encodedOldPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.changePassword(userId, currentPassword, newPassword))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.LOGIN_FAILED);
    }
}
