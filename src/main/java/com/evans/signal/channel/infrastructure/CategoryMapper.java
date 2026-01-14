package com.evans.signal.channel.infrastructure;

import com.evans.signal.channel.domain.Category;
import com.evans.signal.channel.infrastructure.entity.CategoryEntity;

public class CategoryMapper {

    public static Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;
        return Category.builder()
                .id(entity.getId())
                .serverId(entity.getServerId())
                .name(entity.getName())
                .displayOrder(entity.getDisplayOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static CategoryEntity toEntity(Category domain) {
        if (domain == null) return null;
        return CategoryEntity.builder()
                .id(domain.getId())
                .serverId(domain.getServerId())
                .name(domain.getName())
                .displayOrder(domain.getDisplayOrder())
                .build();
    }
}
