package com.evans.signal.channel.service.port;

import com.evans.signal.channel.domain.Channel;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository {
    Channel save(Channel channel);

    Optional<Channel> findById(Long id);

    void deleteById(Long id);


    List<Channel> findAllByCategoryId(Long categoryId);
}
