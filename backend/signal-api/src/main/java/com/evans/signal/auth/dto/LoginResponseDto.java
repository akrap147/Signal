package com.evans.signal.auth.dto;

public record LoginResponseDto(
    String accessToken,
    Long userId,
    String username,
    String email
) {}
