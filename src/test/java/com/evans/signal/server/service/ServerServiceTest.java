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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {

    @Mock private ServerRepository serverRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ChannelRepository channelRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private ServerService serverService;

    @Test
    @DisplayName("서버 생성 시 기본 채널과 멤버가 함께 생성된다")
    void createServer_success() {
        // given
        ServerCreateDto dto = new ServerCreateDto("My Server", 1L);

        // Server 저장 시 ID가 부여된 객체 리턴 가정
        Server savedServer = Server.builder()
                .id(100L)
                .name("My Server")
                .ownerId(1L)
                .build();

        given(serverRepository.save(any(Server.class))).willReturn(savedServer);

        Category savedCategory = Category.builder().id(10L).name("일반").build();
        given(categoryRepository.save(any(Category.class))).willReturn(savedCategory);

        // when
        serverService.createServer(dto);

        // then
        // 1. 서버 저장 확인
        verify(serverRepository).save(any(Server.class));
        
        // 2. 멤버(OWNER) 저장 확인
        verify(memberRepository).save(any(Member.class));
        
        // 3. 기본 카테고리 저장 확인
        verify(categoryRepository).save(any(Category.class));
        
        // 4. 기본 채널 저장 확인
        verify(channelRepository).save(any(Channel.class));
    }
}
