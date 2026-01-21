package com.evans.signal.channel.service;

import com.evans.signal.channel.domain.Category;
import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.service.port.ChannelRepository;
import com.evans.signal.server.domain.Server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ChannelServiceIntegTest {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private com.evans.signal.server.service.port.ServerRepository serverRepository;

    @Autowired
    private com.evans.signal.channel.service.port.CategoryRepository categoryRepository;

    @Test
    @DisplayName("채널 순서 변경 통합 테스트 - Batch Update 검증")
    void updateChannelOrder_IntegrationTest() {
        // given
        // 1. Server & Category 먼저 생성 (FK 제약조건 해결을 위해)
        Server server = Server.create("Test Server", 1L);
        server = serverRepository.save(server);
        Long serverId = server.getId();

        Category category = Category.create(serverId, "Test Category", 0);
        category = categoryRepository.save(category);
        Long categoryId = category.getId();

        // 2. 채널 3개 생성 (초기값: 순서 0)
        Channel ch1 = channelRepository.save(Channel.create(serverId, categoryId, "Channel 1", "TEXT", 0));
        Channel ch2 = channelRepository.save(Channel.create(serverId, categoryId, "Channel 2", "TEXT", 0));
        Channel ch3 = channelRepository.save(Channel.create(serverId, categoryId, "Channel 3", "TEXT", 0));

        Long ch1Id = ch1.getId();
        Long ch2Id = ch2.getId();
        Long ch3Id = ch3.getId();

        // 3. 바꿀 순서: [ch3, ch1, ch2] (즉, 3번이 맨 위로)
        List<Long> newOrderIds = List.of(ch3Id, ch1Id, ch2Id);

        // when
        channelService.updateChannelOrder(categoryId, newOrderIds);

        // then
        // 변경된 값 검증
        Channel updatedCh3 = channelRepository.findById(ch3Id).orElseThrow();
        Channel updatedCh1 = channelRepository.findById(ch1Id).orElseThrow();
        Channel updatedCh2 = channelRepository.findById(ch2Id).orElseThrow();

        assertThat(updatedCh3.getDisplayOrder()).isEqualTo(0); // 1등
        assertThat(updatedCh1.getDisplayOrder()).isEqualTo(1); // 2등
        assertThat(updatedCh2.getDisplayOrder()).isEqualTo(2); // 3등
    }
}
