package com.evans.signal.channel.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Channel {
    private Long id;
    private Long categoryId; // ID 참조 or Null
    private Long serverId;   // ID 참조
    private String name;
    private String type;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Channel create(Long serverId, Long categoryId, String name, String type, Integer displayOrder) {
        return Channel.builder()
                .serverId(serverId)
                .categoryId(categoryId)
                .name(name)
                .type(type)
                .displayOrder(displayOrder)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
