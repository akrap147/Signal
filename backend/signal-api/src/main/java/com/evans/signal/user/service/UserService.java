package com.evans.signal.user.service;

import com.evans.signal.global.security.JwtTokenProvider;
import com.evans.signal.user.domain.User;
import com.evans.signal.user.dto.LoginResponseDto; // Add import
import com.evans.signal.user.dto.UserCreateDto;
import com.evans.signal.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signup(UserCreateDto dto) {
        // 1. 도메인 로직을 통해 객체 생성
        User user = User.create(dto.getEmail(), dto.getPassword(), dto.getUsername());

        // 2. 저장
        return userRepository.save(user).getId();
    }

    public LoginResponseDto login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(u -> u.checkPassword(password))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // 2. 토큰 생성 (여기서 username을 넣으면 프론트가 편해집니다)
        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getUsername());
        
        // 3. 토큰 + 유저 정보 반환
        return new LoginResponseDto(accessToken, user.getId(), user.getUsername(), user.getEmail());
    }
}
