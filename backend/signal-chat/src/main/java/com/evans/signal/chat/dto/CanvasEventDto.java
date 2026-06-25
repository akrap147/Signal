package com.evans.signal.chat.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanvasEventDto {

    public enum EventType {
        DRAW, ERASE, CLEAR
    }

    private EventType type;
    private Long roomId;
    private Long userId;
    private Double x;
    private Double y;
    private Double prevX;
    private Double prevY;
    private String color;
    private Integer brushSize;
}
