package com.evans.signal.server.domain;

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
    private String inviteCode;
    private String iconImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Server create(String name, Long ownerId) {
        return Server.builder()
                .name(name)
                .ownerId(ownerId)
                .inviteCode(generateInviteCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static String generateInviteCode() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
