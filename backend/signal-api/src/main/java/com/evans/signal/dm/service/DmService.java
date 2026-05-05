package com.evans.signal.dm.service;

import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.service.port.ChannelRepository;
import com.evans.signal.dm.domain.DmRoom;
import com.evans.signal.dm.service.port.DmRoomRepository;
import com.evans.signal.global.exception.CustomException;
import com.evans.signal.user.domain.User;
import com.evans.signal.user.exception.UserErrorCode;
import com.evans.signal.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DmService {

    private final DmRoomRepository dmRoomRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Transactional
    public DmChannelResponse getOrCreateDmChannel(Long userId, Long friendId) {
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        return dmRoomRepository.findByUsers(userId, friendId)
                .map(room -> new DmChannelResponse(room.getChannelId(), friendId, friend.getUsername()))
                .orElseGet(() -> {
                    Channel channel = channelRepository.save(Channel.builder().name("DM").type("DM").build());
                    long min = Math.min(userId, friendId);
                    long max = Math.max(userId, friendId);
                    dmRoomRepository.save(DmRoom.builder()
                            .user1Id(min).user2Id(max).channelId(channel.getId()).build());
                    return new DmChannelResponse(channel.getId(), friendId, friend.getUsername());
                });
    }

    @Transactional(readOnly = true)
    public List<DmChannelResponse> getMyDmChannels(Long userId) {
        return dmRoomRepository.findAllByUserId(userId).stream()
                .map(room -> {
                    Long friendId = room.getUser1Id().equals(userId) ? room.getUser2Id() : room.getUser1Id();
                    User friend = userRepository.findById(friendId)
                            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
                    return new DmChannelResponse(room.getChannelId(), friendId, friend.getUsername());
                })
                .toList();
    }

    public record DmChannelResponse(Long channelId, Long friendId, String friendName) {}
}
