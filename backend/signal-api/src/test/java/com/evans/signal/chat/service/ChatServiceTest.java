package com.evans.signal.chat.service;

import com.evans.signal.chat.domain.ChatMessage;
import com.evans.signal.chat.dto.ChatMessageResponse;
import com.evans.signal.chat.infrastructure.ChatMessageJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private ChatMessageJpaRepository chatMessageJpaRepository;

    @Test
    @DisplayName("채널 메시지 히스토리 조회 성공")
    void getChannelMessages_success() {
        // given
        Long channelId = 1L;
        List<ChatMessage> messages = List.of(
                ChatMessage.builder().roomId(channelId).senderId(10L).senderName("Alice").content("안녕하세요").seqId(1L).build(),
                ChatMessage.builder().roomId(channelId).senderId(11L).senderName("Bob").content("반갑습니다").seqId(2L).build()
        );
        given(chatMessageJpaRepository.findAllByRoomIdOrderBySeqIdAsc(channelId)).willReturn(messages);

        // when
        List<ChatMessageResponse> result = messageService.getChannelMessages(channelId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSeqId()).isEqualTo(1L);
        assertThat(result.get(0).getContent()).isEqualTo("안녕하세요");
        assertThat(result.get(1).getSeqId()).isEqualTo(2L);
        assertThat(result.get(1).getContent()).isEqualTo("반갑습니다");
    }

    @Test
    @DisplayName("채널 메시지가 없을 때 빈 리스트 반환")
    void getChannelMessages_empty() {
        // given
        Long channelId = 1L;
        given(chatMessageJpaRepository.findAllByRoomIdOrderBySeqIdAsc(channelId)).willReturn(List.of());

        // when
        List<ChatMessageResponse> result = messageService.getChannelMessages(channelId);

        // then
        assertThat(result).isEmpty();
    }
}
