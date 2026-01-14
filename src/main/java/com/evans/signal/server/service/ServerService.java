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
}
