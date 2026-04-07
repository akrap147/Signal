package com.evans.signal.user.controller;

import com.evans.signal.auth.service.AuthService;
import com.evans.signal.user.dto.*;
import com.evans.signal.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@RequestBody UserCreateDto dto) {
        return ResponseEntity.ok(userService.signup(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyProfile(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateMyProfile(@AuthenticationPrincipal Long userId, @RequestBody UserUpdateDto dto) {
        userService.updateProfile(userId, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Long userId, @RequestBody PasswordChangeDto dto) {
        authService.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
