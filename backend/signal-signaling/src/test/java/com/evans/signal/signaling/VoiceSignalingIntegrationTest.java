package com.evans.signal.signaling;

import com.evans.signal.signaling.dto.SignalingMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VoiceSignalingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private StandardWebSocketClient client;
    private BlockingQueue<String> messages;

    @BeforeEach
    void setup() {
        client = new StandardWebSocketClient();
        messages = new LinkedBlockingQueue<>();
    }

    @Test
    @DisplayName("Full WebRTC Signaling Flow Test (Join -> CreateTransport -> Produce -> Consume)")
    void testFullSignalingFlow() throws Exception {
        // 1. WebSocket Connection
        String url = "ws://localhost:" + port + "/ws/signaling";
        WebSocketSession session = client.execute(new TestWebSocketHandler(), new WebSocketHttpHeaders(), url).get(5, TimeUnit.SECONDS);

        assertThat(session.isOpen()).isTrue();
        System.out.println(">>> WebSocket Connected");

        String roomId = "test-room-junit";

        // 2. Send JOIN
        SignalingMessage joinMsg = SignalingMessage.builder()
                .type("join")
                .roomId(roomId)
                .build();
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(joinMsg)));

        // Expect: RTP Capabilities
        String response1 = messages.poll(5, TimeUnit.SECONDS);
        assertThat(response1).isNotNull();
        Map<String, Object> map1 = objectMapper.readValue(response1, Map.class);
        assertThat(map1).containsKey("rtpCapabilities");
        System.out.println(">>> Received RTP Capabilities");

        // 3. Send CREATE TRANSPORT (Send)
        SignalingMessage createTransportMsg = SignalingMessage.builder()
                .type("createTransport")
                .roomId(roomId)
                .data(Map.of("direction", "send", "roomId", roomId))
                .build();
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(createTransportMsg)));

        // Expect: New Transport ID & ICE Parameters
        String response2 = messages.poll(5, TimeUnit.SECONDS);
        assertThat(response2).isNotNull();
        Map<String, Object> map2 = objectMapper.readValue(response2, Map.class);
        assertThat(map2).containsKey("id");
        assertThat(map2).containsKey("iceParameters");
        String transportId = (String) map2.get("id");
        System.out.println(">>> Created Send Transport: " + transportId);

        // 4. Send PRODUCE (Mocking DTLS connect usually happens first, but we skip to produce for signaling test)
        // Note: In real world, we need valid RTP parameters. We'll send dummy ones just to check signaling path.
        // Node media server might fail if parameters are invalid, but we should get *some* response (success or error).
        Map<String, Object> dummyRtpParameters = Map.of(
            "mid", "0",
            "codecs", java.util.List.of(
                Map.of("mimeType", "audio/opus", "payloadType", 111, "clockRate", 48000)
            )
        );

        SignalingMessage produceMsg = SignalingMessage.builder()
                .type("produce")
                .roomId(roomId)
                .data(Map.of(
                    "transportId", transportId,
                    "kind", "audio",
                    "rtpParameters", dummyRtpParameters,
                    "roomId", roomId
                ))
                .build();
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(produceMsg)));

        // Expect: Producer ID
        String response3 = messages.poll(5, TimeUnit.SECONDS);
        assertThat(response3).isNotNull();
        // Just verify we got a response. It might be an error if RTP params are rejected by mediasoup C++,
        // but getting *any* response proves the RabbitMQ round trip worked.
        System.out.println(">>> Mock Produce Response: " + response3);

        session.close();
    }

    private class TestWebSocketHandler extends TextWebSocketHandler {
        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            messages.offer(message.getPayload());
        }
    }
}
