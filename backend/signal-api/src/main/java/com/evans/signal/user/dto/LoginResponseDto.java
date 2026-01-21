package com.evans.signal.user.dto;

public record LoginResponseDto(
    String accessToken,
    Long userId,
    String username,
    String email
) {}
