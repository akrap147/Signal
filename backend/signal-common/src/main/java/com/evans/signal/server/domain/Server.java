package com.evans.signal.server.domain;

import com.evans.signal.global.exception.CustomException;
import com.evans.signal.server.exception.ServerErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Server {
    private Long id;
    private String name;
    private Long ownerId;
    private String iconImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Server create(String name, Long ownerId) {
        validateName(name); // 도메인 비즈니스 규칙 검증
        return Server.builder()
                .name(name)
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new CustomException(ServerErrorCode.INVALID_SERVER_NAME);
        }
        if (name.length() > 100) {
            throw new CustomException(ServerErrorCode.SERVER_NAME_TOO_LONG);
        }
    }

    // 방장 여부 확인 로직을 도메인으로 이동
    public boolean isOwner(Long userId) {
        return this.ownerId.equals(userId);
    }

    // 방장 퇴장 불가 등 비즈니스 검증 로직
    public void validateLeave(Long userId) {
        if (isOwner(userId)) {
            throw new CustomException(ServerErrorCode.OWNER_CANNOT_LEAVE);
        }
    }

    public void validateDelete(Long userId) {
        if (!isOwner(userId)) {
            throw new CustomException(ServerErrorCode.NOT_OWNER);
        }
    }

    public void updateName(String newName, Long userId) {
        if (!isOwner(userId)) {
            throw new CustomException(ServerErrorCode.NOT_OWNER);
        }
        validateName(newName);
        this.name = newName;
        this.updatedAt = LocalDateTime.now();
    }
}