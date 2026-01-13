package com.evans.signal.channel.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Category {
    private Long id;
    private Long serverId; // ID 참조
    private String name;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Category create(Long serverId, String name, Integer displayOrder) {
        return Category.builder()
                .serverId(serverId)
                .name(name)
                .displayOrder(displayOrder)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
