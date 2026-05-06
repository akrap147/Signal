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
import com.evans.signal.server.dto.response.SimpleServerResponse;
import com.evans.signal.global.exception.CustomException;
import com.evans.signal.server.exception.ServerErrorCode;
import com.evans.signal.server.service.port.MemberRepository;
import com.evans.signal.server.service.port.ServerRepository;
import com.evans.signal.server.domain.Role;
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
    private final InviteService inviteService;

    @Transactional
    public Long createServer(ServerCreateDto dto, Long ownerId) {
        // 1. Server 생성 및 저장
        Server server = Server.create(dto.getName(), ownerId);
        Server savedServer = serverRepository.save(server);

        // 2. Member(Owner) 생성 및 저장
        Member member = Member.create(savedServer.getId(), ownerId, Role.OWNER);
        memberRepository.save(member);

        // 3. 기본 카테고리 및 채널 생성

        // 3-1. 정보 카테고리 (TEXT)
        Category infoCategory = Category.create(savedServer.getId(), "정보", 0);
        Category savedInfoCategory = categoryRepository.save(infoCategory);
        Channel noticeChannel = Channel.create(savedServer.getId(), savedInfoCategory.getId(), "공지사항", "TEXT", 0);
        channelRepository.save(noticeChannel);

        // 3-2. 채팅 채널 카테고리 (TEXT)
        Category chatCategory = Category.create(savedServer.getId(), "채팅 채널", 1);
        Category savedChatCategory = categoryRepository.save(chatCategory);
        Channel generalTextChannel = Channel.create(savedServer.getId(), savedChatCategory.getId(), "일반", "TEXT", 0);
        channelRepository.save(generalTextChannel);

        // 3-3. 음성 채널 카테고리 (VOICE)
        Category voiceCategory = Category.create(savedServer.getId(), "음성 채널", 2);
        Category savedVoiceCategory = categoryRepository.save(voiceCategory);
        Channel generalVoiceChannel = Channel.create(savedServer.getId(), savedVoiceCategory.getId(), "일반", "VOICE", 0);
        channelRepository.save(generalVoiceChannel);

        return savedServer.getId();
    }

    @Transactional
    public Long joinServer(String inviteCode, Long userId) {
        Long serverId = inviteService.getServerIdByInviteCode(inviteCode);
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new CustomException(ServerErrorCode.SERVER_NOT_FOUND));

        Member member = Member.create(server.getId(), userId, Role.MEMBER);
        memberRepository.save(member);
        
        return server.getId();
    }

    @Transactional
    public String createInviteCode(Long serverId, Long userId) {
        // 1. 서버 존재 여부 및 방장 권한 확인
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new CustomException(ServerErrorCode.SERVER_NOT_FOUND));

        // 도메인 모델에게 권한 검증 위임
        if (!server.isOwner(userId)) {
            throw new CustomException(ServerErrorCode.NOT_OWNER);
        }

        // 2. 1시간 만료 조건으로 초대 코드 생성 및 저장 (Redis)
        // 1시간 = 3600초
        // todo : 초대 시간이 hardCoding으로 되어있는 단점.
        return inviteService.createInvite(serverId, userId, 3600L);
    }


    @Transactional(readOnly = true)
    public List<SimpleServerResponse> findAllMyServers(Long userId) {
        List<Member> members = memberRepository.findAllByUserId(userId);
        List<Long> serverIds = members.stream()
                .map(Member::getServerId)
                .toList();

        List<Server> servers = serverRepository.findAllById(serverIds);
        return servers.stream()
                .map(s -> new SimpleServerResponse(s.getId(), s.getName(), s.getIconImage()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ServerDetailResponse getServerDetails(Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new CustomException(ServerErrorCode.SERVER_NOT_FOUND));

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

    @Transactional
    public void leaveServer(Long serverId, Long userId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new CustomException(ServerErrorCode.SERVER_NOT_FOUND));

        if (server.getOwnerId().equals(userId)) {
            throw new CustomException(ServerErrorCode.OWNER_CANNOT_LEAVE);
        }

        memberRepository.deleteByServerIdAndUserId(serverId, userId);
    }

    @Transactional
    public void deleteServer(Long serverId, Long userId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new CustomException(ServerErrorCode.SERVER_NOT_FOUND));

        if (!server.getOwnerId().equals(userId)) {
            throw new CustomException(ServerErrorCode.NOT_OWNER);
        }

        serverRepository.deleteById(serverId);
    }

    @Transactional
    public void kickMember(Long serverId, Long targetUserId, Long requestUserId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new CustomException(ServerErrorCode.SERVER_NOT_FOUND));

        if (!server.getOwnerId().equals(requestUserId)) {
            throw new CustomException(ServerErrorCode.NOT_OWNER);
        }

        if (server.getOwnerId().equals(targetUserId)) {
            throw new CustomException(ServerErrorCode.CANNOT_KICK_SELF);
        }

        memberRepository.deleteByServerIdAndUserId(serverId, targetUserId);
    }

    @Transactional
    public void updateServerName(Long serverId, String newName, Long userId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new CustomException(ServerErrorCode.SERVER_NOT_FOUND));

        server.updateName(newName, userId);
        serverRepository.save(server);
    }
}
