package com.evans.signal.signaling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalingMessage {
    private String type; // e.g., "join", "createTransport", "connectTransport", "produce", "consume"
    private String roomId;
    private Map<String, Object> data;
}
