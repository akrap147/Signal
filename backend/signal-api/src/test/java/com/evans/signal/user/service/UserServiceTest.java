package com.evans.signal.user.service;

import com.evans.signal.global.exception.CustomException;
import com.evans.signal.global.exception.GlobalErrorCode;
import com.evans.signal.user.domain.User;
import com.evans.signal.user.dto.UserCreateDto;
import com.evans.signal.user.dto.UserResponseDto;
import com.evans.signal.user.dto.UserUpdateDto;
import com.evans.signal.user.exception.UserErrorCode;
import com.evans.signal.user.service.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        UserCreateDto dto = new UserCreateDto("test@example.com", "password", "username");
        User user = User.builder().id(1L).email(dto.getEmail()).username(dto.getUsername()).build();

        given(userRepository.existsByEmail(dto.getEmail())).willReturn(false);
        given(passwordEncoder.encode(dto.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        UserResponseDto response = userService.signup(dto);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo(dto.getEmail());
        assertThat(response.getUsername()).isEqualTo(dto.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail_duplicate_email() {
        // given
        UserCreateDto dto = new UserCreateDto("test@example.com", "password", "username");
        given(userRepository.existsByEmail(dto.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.EMAIL_DUPLICATION);
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getUserProfile_success() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .username("username")
                .profileImage("image.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponseDto response = userService.getUserProfile(userId);

        // then
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUsername()).isEqualTo("username");
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 사용자 없음")
    void getUserProfile_fail_user_not_found() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserProfile(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfile_success() {
        // given
        Long userId = 1L;
        UserUpdateDto dto = new UserUpdateDto("newName", "newImage"); 
        
        User user = User.builder().id(userId).username("oldName").build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.updateProfile(userId, dto);

        // then
        verify(userRepository).save(user);
        assertThat(user.getUsername()).isEqualTo("newName");
        assertThat(user.getProfileImage()).isEqualTo("newImage");
    }
}
