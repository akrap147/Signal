package com.evans.signal.chat.service;

import com.evans.signal.chat.dto.CanvasEventDto;
import com.evans.signal.redis.RedisKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CanvasPublisher {

    private static final long HISTORY_MAX_SIZE = 1000;
    private static final long HISTORY_TTL_MINUTES = 30;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(CanvasEventDto event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            if (event.getType() == CanvasEventDto.EventType.CLEAR) {
                redisTemplate.delete(RedisKeys.canvasHistory(event.getRoomId()));
            } else {
                String historyKey = RedisKeys.canvasHistory(event.getRoomId());
                redisTemplate.opsForList().rightPush(historyKey, json);
                redisTemplate.opsForList().trim(historyKey, -HISTORY_MAX_SIZE, -1);
                redisTemplate.expire(historyKey, HISTORY_TTL_MINUTES, TimeUnit.MINUTES);
            }

            redisTemplate.convertAndSend(RedisKeys.canvasChannel(event.getRoomId()), json);
        } catch (JsonProcessingException e) {
            log.error("Canvas publish 실패: {}", e.getMessage());
        }
    }
}
