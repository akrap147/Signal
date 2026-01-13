package com.evans.signal.channel.infrastructure;

import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.service.port.ChannelRepository;
import com.evans.signal.server.infrastructure.ServerEntity;
import com.evans.signal.server.infrastructure.ServerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChannelRepositoryImpl implements ChannelRepository {

    private final ChannelJpaRepository channelJpaRepository;
    private final ServerJpaRepository serverJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Channel save(Channel channel) {
        ServerEntity serverEntity = serverJpaRepository.findById(channel.getServerId())
                .orElseThrow(() -> new IllegalArgumentException("Server not found with ID: " + channel.getServerId()));

        CategoryEntity categoryEntity = null;
        if (channel.getCategoryId() != null) {
            categoryEntity = categoryJpaRepository.findById(channel.getCategoryId())
                    .orElse(null);
        }

        ChannelEntity entity = ChannelMapper.toEntity(channel, serverEntity, categoryEntity);
        ChannelEntity savedEntity = channelJpaRepository.save(entity);
        return ChannelMapper.toDomain(savedEntity);
    }
}
