package com.evans.signal.channel.infrastructure;

import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.infrastructure.entity.ChannelEntity;


public class ChannelMapper {


    public static Channel toDomain(ChannelEntity entity) {
        if (entity == null) return null;
        return Channel.builder()
                .id(entity.getId())
                .serverId(entity.getServerId())
                .categoryId(entity.getCategoryId())
                .name(entity.getName())
                .type(entity.getType())
                .displayOrder(entity.getDisplayOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ChannelEntity toEntity(Channel domain) {
        if (domain == null) return null;
        return ChannelEntity.builder()
                .id(domain.getId())
                .serverId(domain.getServerId())
                .categoryId(domain.getCategoryId())
                .name(domain.getName())
                .type(domain.getType())
                .displayOrder(domain.getDisplayOrder())
                .build();
    }
}
