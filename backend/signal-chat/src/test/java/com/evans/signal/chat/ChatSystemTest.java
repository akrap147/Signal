package com.evans.signal.chat;

import com.evans.signal.chat.dto.ChatMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ChatSystemTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:alpine"))
            .withExposedPorts(6379);

    @Container
    static GenericContainer<?> rabbitmq = new GenericContainer<>(DockerImageName.parse("rabbitmq:3-management"))
            .withExposedPorts(5672, 15672, 61613)
            // STOMP 플러그인 활성화를 위한 커맨드 설정
            .withCommand("/bin/sh", "-c", "rabbitmq-plugins enable --offline rabbitmq_stomp rabbitmq_management && rabbitmq-server");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbitmq.getMappedPort(5672));
        registry.add("spring.rabbitmq.stomp.port", () -> rabbitmq.getMappedPort(61613));
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("WebSocket Chat Minimal Test: A가 보내고 B가 받는다")
    void testWebSocketChatReference() throws Exception {
        // 1. 유저 B (수신자) 연결 및 구독
        StompSession userB = connect();
        BlockingQueue<ChatMessageDto> messages = new LinkedBlockingQueue<>();

        userB.subscribe("/topic/channel.1", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messages.add((ChatMessageDto) payload);
            }
        });

        // 2. 유저 A (송신자) 연결 및 메시지 전송
        StompSession userA = connect();
        ChatMessageDto msg = ChatMessageDto.builder()
                .roomId(1L)
                .senderId(101L)
                .content("Test Message")
                .build();

        userA.send("/pub/chat/message", msg);

        // 3. 검증: 메시지 수신 및 Redis SeqId 생성 확인
        ChatMessageDto received = messages.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.getContent()).isEqualTo("Test Message");
        assertThat(received.getSeqId()).isNotNull(); // Redis 연동 확인
    }

    private StompSession connect() throws Exception {
        return stompClient.connectAsync("ws://localhost:" + port + "/ws-stomp", new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);
    }
}
