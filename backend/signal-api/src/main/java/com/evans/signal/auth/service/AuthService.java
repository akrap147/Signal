package com.evans.signal.auth.service;

import com.evans.signal.auth.jwt.JwtTokenProvider;
import com.evans.signal.global.exception.CustomException;
import com.evans.signal.redis.presence.RedisUserStatusService;
import com.evans.signal.user.domain.User;
import com.evans.signal.auth.dto.LoginResponseDto;
import com.evans.signal.user.exception.UserErrorCode;
import com.evans.signal.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(u -> u.checkPassword(password, passwordEncoder))
                .orElseThrow(() -> new CustomException(UserErrorCode.LOGIN_FAILED));

        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getUsername());

        return new LoginResponseDto(accessToken, user.getId(), user.getUsername(), user.getEmail());
    }

    //todo : 로그아웃 기능 완성
    public void logout(long id){
        // accessToken 없애기
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (!user.checkPassword(currentPassword, passwordEncoder)) {
            throw new CustomException(UserErrorCode.LOGIN_FAILED); // 혹은 비밀번호 불일치 전용 에러
        }

        user.changePassword(newPassword, passwordEncoder);
        userRepository.save(user);
    }
}
