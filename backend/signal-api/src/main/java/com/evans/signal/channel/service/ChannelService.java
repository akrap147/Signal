package com.evans.signal.channel.service;

import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.service.port.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;

    @Transactional
    public void updateChannelOrder(Long categoryId, List<Long> orderedChannelIds) {
        List<Channel> channels = channelRepository.findAllByCategoryId(categoryId);

        Map<Long, Channel> channelMap = channels.stream()
                .collect(Collectors.toMap(Channel::getId, c -> c));

        //Batch-size 50으로 정해서 50개 다 모아지고 날라갈 예정
        for (int i = 0; i < orderedChannelIds.size(); i++) {
            Channel channel = channelMap.get(orderedChannelIds.get(i));
            if (channel != null) {
                channel.updateDisplayOrder(i);
                channelRepository.save(channel);
            }
        }
    }

    @Transactional
    public Long createChannel(Long serverId, Long categoryId, String name, String type) {
        // TODO: displayOrder 로직 추가 (마지막 순서 + 1)
        Channel channel = Channel.create(serverId, categoryId, name, type, 0);
        return channelRepository.save(channel).getId();
    }

    @Transactional
    public void updateChannel(Long channelId, String name) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        channel.update(name);
        channelRepository.save(channel);
    }

    @Transactional
    public void deleteChannel(Long channelId) {
        channelRepository.deleteById(channelId);
    }
}
