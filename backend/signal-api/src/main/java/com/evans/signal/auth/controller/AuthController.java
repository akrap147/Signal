package com.evans.signal.auth.controller;

import com.evans.signal.auth.dto.LogoutRequestDto;
import com.evans.signal.auth.service.AuthService;
import com.evans.signal.auth.dto.LoginRequestDto;
import com.evans.signal.auth.dto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto.getEmail(), dto.getPassword()));
    }

    @PostMapping("/logout")
    public void logout(@RequestBody LogoutRequestDto dto){
        authService.logout(dto.getId());
    }

}
