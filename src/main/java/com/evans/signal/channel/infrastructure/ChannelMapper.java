package com.evans.signal.channel.infrastructure;

import com.evans.signal.channel.domain.Category;
import com.evans.signal.channel.domain.Channel;
import com.evans.signal.server.infrastructure.ServerEntity;

public class ChannelMapper {

    // --- Category Mapping ---
    public static Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;
        return Category.builder()
                .id(entity.getId())
                .serverId(entity.getServer().getId())
                .name(entity.getName())
                .displayOrder(entity.getDisplayOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static CategoryEntity toEntity(Category domain, ServerEntity serverEntity) {
        if (domain == null) return null;
        return CategoryEntity.builder()
                .id(domain.getId())
                .server(serverEntity)
                .name(domain.getName())
                .displayOrder(domain.getDisplayOrder())
                .build();
    }

    // --- Channel Mapping ---
    public static Channel toDomain(ChannelEntity entity) {
        if (entity == null) return null;
        return Channel.builder()
                .id(entity.getId())
                .serverId(entity.getServer().getId())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .name(entity.getName())
                .type(entity.getType())
                .displayOrder(entity.getDisplayOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ChannelEntity toEntity(Channel domain, ServerEntity serverEntity, CategoryEntity categoryEntity) {
        if (domain == null) return null;
        return ChannelEntity.builder()
                .id(domain.getId())
                .server(serverEntity)
                .category(categoryEntity) // Nullable
                .name(domain.getName())
                .type(domain.getType())
                .displayOrder(domain.getDisplayOrder())
                .build();
    }
}
