package com.evans.signal.channel.service.port;

import com.evans.signal.channel.domain.Channel;

public interface ChannelRepository {
    Channel save(Channel channel);
}
