package com.evans.signal.server.service;

import com.evans.signal.channel.domain.Category;
import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.service.port.CategoryRepository;
import com.evans.signal.channel.service.port.ChannelRepository;
import com.evans.signal.server.domain.Member;
import com.evans.signal.server.domain.Server;
import com.evans.signal.server.dto.ServerCreateDto;
import com.evans.signal.server.service.port.MemberRepository;
import com.evans.signal.server.service.port.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evans.signal.server.dto.response.ServerDetailResponse;
import com.evans.signal.server.dto.response.ServerDetailResponse.CategoryDto;
import com.evans.signal.server.dto.response.ServerDetailResponse.ChannelDto;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;
    private final MemberRepository memberRepository;
    private final ChannelRepository channelRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long createServer(ServerCreateDto dto) {
        // 1. Server 생성 및 저장
        Server server = Server.create(dto.getName(), dto.getOwnerId());
        Server savedServer = serverRepository.save(server);

        // 2. Member(Owner) 생성 및 저장
        // todo: Owner hard Coding에 대한 생각.
        Member member = Member.create(savedServer.getId(), dto.getOwnerId(), "OWNER");
        memberRepository.save(member);

        // 3. Category(일반) 생성 및 저장
        Category category = Category.create(savedServer.getId(), "일반", 0);
        Category savedCategory = categoryRepository.save(category);

        // 4. Channel(일반) 생성 및 저장
        Channel channel = Channel.create(savedServer.getId(), savedCategory.getId(), "일반", "TEXT", 0);
        channelRepository.save(channel);

        return savedServer.getId();
    }

    @Transactional
    public Long joinServer(String inviteCode, Long userId) {
        Server server = serverRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        Member member = Member.create(server.getId(), userId, "MEMBER");
        return memberRepository.save(member).getId();
    }

    @Transactional(readOnly = true)
    public java.util.List<Server> findAllMyServers(Long userId) {
        List<Member> members = memberRepository.findAllByUserId(userId);
        List<Long> serverIds = members.stream()
                .map(Member::getServerId)
                .toList();
        
        return serverRepository.findAllById(serverIds);
    }

    @Transactional(readOnly = true)
    public ServerDetailResponse getServerDetails(Long serverId) {
        // 1. 서버 조회 (쿼리 1)
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found"));

        // 2. 카테고리 전체 조회 (쿼리 2)
        List<Category> categories = categoryRepository.findAllByServerId(serverId);

        // 3. 채널 전체 조회 (쿼리 3)
        List<Channel> allChannels = channelRepository.findAllByServerId(serverId);

        // 4. 채널을 카테고리별로 그룹핑 (메모리 연산)
        Map<Long, List<Channel>> channelsByCategory = allChannels.stream()
                .collect(Collectors.groupingBy(Channel::getCategoryId));

        // 5. DTO 조립
        List<CategoryDto> categoryDtos = categories.stream()
                .sorted(Comparator.comparingInt(Category::getDisplayOrder)) // 카테고리 순서 정렬
                .map(category -> {
                    List<Channel> channels = channelsByCategory.getOrDefault(category.getId(), Collections.emptyList());

                    List<ChannelDto> channelDtos = channels.stream()
                            .sorted(Comparator.comparingInt(Channel::getDisplayOrder)) // 채널 순서 정렬
                            .map(channel -> ChannelDto.builder()
                                    .id(channel.getId())
                                    .name(channel.getName())
                                    .type(channel.getType())
                                    .displayOrder(channel.getDisplayOrder())
                                    .build())
                            .toList();

                    return CategoryDto.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .displayOrder(category.getDisplayOrder())
                            .channels(channelDtos)
                            .build();
                })
                .toList();

        return ServerDetailResponse.builder()
                .id(server.getId())
                .name(server.getName())
                .ownerId(server.getOwnerId())
                .inviteCode(server.getInviteCode())
                .iconImage(server.getIconImage())
                .categories(categoryDtos)
                .build();
    }
}
