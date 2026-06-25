package com.evans.signal.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class SignalingMessage {
    private String action;
    private Long roomId;
    private Long userId;
    private Map<String, Object> payload;
}
