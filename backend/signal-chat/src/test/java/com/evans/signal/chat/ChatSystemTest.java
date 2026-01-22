package com.evans.signal.chat;

import com.evans.signal.chat.domain.ChatMessage;
import com.evans.signal.chat.dto.ChatMessageDto;
import com.evans.signal.chat.infrastructure.ChatMessageJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatSystemTest {

    // 내부에 가능한 port로 열어라
    @LocalServerPort
    private int port;
    @Autowired
    private ChatMessageJpaRepository chatMessageRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private WebSocketStompClient stompClient;
    private final String url = "ws://localhost:";
    private StompSession userASession;
    private StompSession userBSession;
    private BlockingQueue<ChatMessageDto> userBQueue;

    @BeforeEach
    void setup() throws Exception {
        // 1. 클라이언트 설정
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // 2. Redis 데이터 초기화 (항상 1번부터 시작하도록)
        redisTemplate.delete("room:1:seq");

        // 3. 유저 A 연결
        userASession = connectUser();

        // 4. 유저 B 연결 및 구독 설정
        userBSession = connectUser();
        userBQueue = new LinkedBlockingDeque<>();
        userBSession.subscribe("/topic/channel.1", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                userBQueue.add((ChatMessageDto) payload);
            }
        });

        log.info("유저 A, B 연결 및 유저 B 구독 완료");
    }


    private StompSession connectUser() throws Exception {
        return stompClient.connectAsync(url + port + "/ws-stomp", new StompSessionHandlerAdapter() {
        }).get(1, TimeUnit.SECONDS);
    }



    // test 1
    @Test
    @DisplayName("단순 대화 테스트: 유저 A가 보낸 메시지를 유저 B가 수신해야 한다")
    void chatMessageTransferTest() throws Exception {
        // Given
        ChatMessageDto sendMsg = ChatMessageDto.builder()
                .roomId(1L).senderId(101L).content("안녕, B!").build();

        // When
        userASession.send("/pub/chat/message", sendMsg);

        // Then
        ChatMessageDto receivedMsg = userBQueue.poll(5, TimeUnit.SECONDS);
        assertThat(receivedMsg).isNotNull();
        assertThat(receivedMsg.getContent()).isEqualTo("안녕, B!");
    }

    // 2. 보낸 순서대로 오는지 확인 (Sequence 정합성)
    @Test
    @DisplayName("순서 보장 테스트: 메시지를 연속 발송했을 때 SeqId가 순차적으로 증가해야 한다")
    void messageSequenceOrderTest() throws Exception {
        // Given: 3개 메시지 준비
        int count = 3;

        // When: 연속 발송
        for (int i = 1; i <= count; i++) {
            userASession.send("/pub/chat/message",
                    ChatMessageDto.builder().roomId(1L).senderId(101L).content("Msg " + i).build());
        }

        // Then: 받은 메시지들의 SeqId가 1, 2, 3인지 확인
        for (int i = 1; i <= count; i++) {
            ChatMessageDto received = userBQueue.poll(5, TimeUnit.SECONDS);
            assertThat(received).isNotNull();
            assertThat(received.getSeqId()).isEqualTo((long) i);
            log.info("Received Sequence: {}", received.getSeqId());
        }
    }
    @Test
    @DisplayName("DB 저장 테스트: 발송된 메시지가 비동기적으로 DB에 저장되어야 한다")
    void databaseSaveTest() throws Exception {
        // Given
        String uniqueContent = "Storage Test " + System.currentTimeMillis();
        ChatMessageDto sendMsg = ChatMessageDto.builder()
                .roomId(1L).senderId(101L).content(uniqueContent).build();

        // When
        userASession.send("/pub/chat/message", sendMsg);

        // Then: DB 확인 (웹소켓 수신은 무시하고 DB만 체크)
        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            // 가장 최근 저장된 메시지 하나를 가져옴
            Optional<ChatMessage> dbMsg = chatMessageRepository.findTopByRoomIdOrderBySeqIdDesc(1L);
            assertThat(dbMsg).isPresent();
            assertThat(dbMsg.get().getContent()).isEqualTo(uniqueContent);
        });
    }

}
