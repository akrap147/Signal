package com.evans.signal.user.controller;

import com.evans.signal.user.dto.UserCreateDto;
import com.evans.signal.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 등록된 사용자의 ID를 반환합니다.")
    public ResponseEntity<Long> signup(@RequestBody UserCreateDto dto) {
        Long userId = userService.signup(dto);
        return ResponseEntity.ok(userId);
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회 (Mock)", description = "현재 개발 단계용 Mock 유저 정보를 반환합니다. 추후 인증 구현 시 실제 정보로 대체됩니다.")
    public ResponseEntity<String> getMe() {
        // TODO: Implement actual authentication and return UserDetailResponse
        return ResponseEntity.ok("Current User Info (Mock)");
    }
}
