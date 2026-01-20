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
    public void signup(UserCreateDto dto) {
        // 1. 도메인 로직을 통해 객체 생성
        User user = User.create(dto.getEmail(), dto.getPassword(), dto.getUsername());

        // 2. 저장
        userRepository.save(user);
    }
}
