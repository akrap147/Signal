package com.evans.signal.channel.infrastructure;

import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.infrastructure.entity.ChannelEntity;
import com.evans.signal.channel.service.port.ChannelRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChannelRepositoryImpl implements ChannelRepository {

    private final ChannelJpaRepository channelJpaRepository;

    @Override
    public Channel save(Channel channel) {
        ChannelEntity entity = ChannelMapper.toEntity(channel);
        ChannelEntity savedEntity = channelJpaRepository.save(entity);
        return ChannelMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Channel> findById(Long id) {
        return channelJpaRepository.findById(id)
                .map(ChannelMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        channelJpaRepository.deleteById(id);
    }

    @Override
    public List<Channel> findAllByCategoryId(Long categoryId) {
        return channelJpaRepository.findAllByCategoryId(categoryId).stream()
                .map(ChannelMapper::toDomain)
                .toList();
    }

}
