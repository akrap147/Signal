package com.evans.signal.server.service;

import com.evans.signal.channel.domain.Category;
import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.service.port.CategoryRepository;
import com.evans.signal.channel.service.port.ChannelRepository;
import com.evans.signal.server.domain.Member;
import com.evans.signal.server.domain.Server;
import com.evans.signal.server.dto.ServerCreateDto;
import com.evans.signal.server.dto.response.MemberResponse;
import com.evans.signal.server.dto.response.ServerDetailResponse;
import com.evans.signal.server.dto.response.ServerDetailResponse.CategoryDto;
import com.evans.signal.server.dto.response.ServerDetailResponse.ChannelDto;
import com.evans.signal.server.service.port.MemberRepository;
import com.evans.signal.server.service.port.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

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
    public List<Server> findAllMyServers(Long userId) {
        List<Member> members = memberRepository.findAllByUserId(userId);
        List<Long> serverIds = members.stream()
                .map(Member::getServerId)
                .toList();

        return serverRepository.findAllById(serverIds);
    }

    @Transactional(readOnly = true)
    public ServerDetailResponse getServerDetails(Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found"));

        List<Category> categories = categoryRepository.findAllByServerId(serverId);
        List<Channel> allChannels = channelRepository.findAllByServerId(serverId);

        // DTO 조립 및 도메인 로직 위임 (Category 객체가 채널 정렬 담당)
        List<CategoryDto> categoryDtos = categories.stream()
                .sorted(Comparator.comparingInt(Category::getDisplayOrder))
                .map(category -> {
                    // Category 도메인 객체에게 채널 필터링 및 정렬 위임
                    List<Channel> myChannels = category.filterAndSortChannels(allChannels);

                    List<ChannelDto> channelDtos = myChannels.stream()
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

    @Transactional(readOnly = true)
    public List<MemberResponse> getServerMembers(Long serverId) {
        return memberRepository.findAllByServerId(serverId).stream()
                .map(member -> MemberResponse.builder()
                        .id(member.getId())
                        .userId(member.getUserId())
                        .role(member.getRole())
                        .build())
                .toList();
    }
}
