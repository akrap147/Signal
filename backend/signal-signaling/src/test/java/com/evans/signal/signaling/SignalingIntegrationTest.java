package com.evans.signal.signaling;

import com.evans.signal.signaling.dto.SignalingMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SignalingIntegrationTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private BlockingQueue<String> messages;

    @BeforeEach
    void setup() {
        messages = new LinkedBlockingQueue<>();
    }

    @Test
    void testSignalingFlow() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketSession session = client.doHandshake(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                messages.offer(message.getPayload());
            }
        }, new WebSocketHttpHeaders(), java.net.URI.create("ws://localhost:" + port + "/ws/signaling")).get(5, TimeUnit.SECONDS);

        assertThat(session.isOpen()).isTrue();

        // 1. Join Room Request
        SignalingMessage joinMessage = new SignalingMessage("join", "test-room-1", null);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(joinMessage)));

        // 2. Wait for Response (Mediated by RabbitMQ -> Node.js -> RabbitMQ -> Java)
        // Note: Node.js server needs to be running for this to pass fully.
        // If Node.js is not running, we expect a timeout or error, but connection should be fine.
        String response = messages.poll(5, TimeUnit.SECONDS);
        
        // Node.js가 켜져 있다면 응답이 올 것이고, 꺼져 있다면 null일 것입니다.
        // 현재 터미널에서 npm start가 돌고 있으므로 응답이 와야 합니다.
        assertThat(response).isNotNull();
        System.out.println("Received response: " + response);
        
        assertThat(response).contains("rtpCapabilities");
    }
}
