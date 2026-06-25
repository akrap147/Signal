package com.evans.signal.chat.controller;

import com.evans.signal.chat.dto.CanvasEventDto;
import com.evans.signal.chat.service.CanvasPublisher;
import com.evans.signal.redis.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CanvasController {

    private final CanvasPublisher canvasPublisher;
    private final StringRedisTemplate redisTemplate;

    @MessageMapping("/canvas/draw")
    public void draw(@Payload CanvasEventDto event) {
        canvasPublisher.publish(event);
    }

    @GetMapping("/canvas/{roomId}/history")
    @ResponseBody
    public List<String> getHistory(@PathVariable Long roomId) {
        List<String> history = redisTemplate.opsForList().range(RedisKeys.canvasHistory(roomId), 0, -1);
        return history != null ? history : List.of();
    }

}
