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

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDisplayOrder(Integer newOrder) {
        this.displayOrder = newOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public static Category create(Long serverId, String name, Integer displayOrder) {
        return Category.builder()
                .serverId(serverId)
                .name(name)
                .displayOrder(displayOrder)
                .build();
    }
    public java.util.List<Channel> filterAndSortChannels(java.util.List<Channel> channels) {
        return channels.stream()
                .filter(channel -> channel.getCategoryId().equals(this.id))
                .sorted(java.util.Comparator.comparingInt(Channel::getDisplayOrder))
                .toList();
    }
}
