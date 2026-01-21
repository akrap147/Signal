package com.evans.signal.user.service;

import com.evans.signal.user.domain.User;
import com.evans.signal.user.dto.UserCreateDto;
import com.evans.signal.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public Long signup(UserCreateDto dto) {
        // 1. 도메인 로직을 통해 객체 생성
        User user = User.create(dto.getEmail(), dto.getPassword(), dto.getUsername());

        // 2. 저장
        return userRepository.save(user).getId();
    }

    public Long login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password)) // TODO: Password Encoder 적용 필요
                .map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
    }
}
