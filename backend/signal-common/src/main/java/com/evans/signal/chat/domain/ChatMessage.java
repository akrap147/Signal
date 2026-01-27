package com.evans.signal.chat.domain;

import com.evans.signal.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message")
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long seqId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public ChatMessage(Long roomId, Long senderId, String content, Long seqId) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.seqId = seqId;
    }
}
