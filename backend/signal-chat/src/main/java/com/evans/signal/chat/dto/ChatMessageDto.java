package com.evans.signal.chat.dto;

import lombok.*;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatMessageDto {
    public enum MessageType {
        CHANNEL,
        DM
    }

    private MessageType type;
    private Long roomId;
    private Long senderId;
    private String content;
    private String senderName;
}
