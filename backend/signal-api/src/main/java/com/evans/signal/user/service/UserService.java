package com.evans.signal.user.service;

import com.evans.signal.global.exception.CustomException;
import com.evans.signal.user.domain.User;
import com.evans.signal.user.dto.UserCreateDto;
import com.evans.signal.user.dto.UserResponseDto;
import com.evans.signal.user.dto.UserUpdateDto;
import com.evans.signal.user.exception.UserErrorCode;
import com.evans.signal.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto signup(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(UserErrorCode.EMAIL_DUPLICATION);
        }

        String rawPassword = dto.getPassword();
        User user = User.create(dto.getEmail(), rawPassword, dto.getUsername(), passwordEncoder);
        User savedUser = userRepository.save(user);
        
        return UserResponseDto.from(savedUser);
    }


    @Transactional
    public void updateProfile(Long userId, UserUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        user.updateProfile(dto.getUsername(), dto.getProfileImageUrl());
        userRepository.save(user); // 필수: 도메인 객체이므로 명시적 저장 필요
    }

    // 내 정보 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        return UserResponseDto.from(user);
    }

}
