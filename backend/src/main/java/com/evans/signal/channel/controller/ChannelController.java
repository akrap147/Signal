package com.evans.signal.channel.controller;

import com.evans.signal.channel.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@Tag(name = "Channel API", description = "Manage channels within categories")
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    @Operation(summary = "채널 생성", description = "특정 카테고리 내에 새로운 채널을 생성합니다.")
    public ResponseEntity<Long> createChannel(@RequestBody CreateChannelRequest request) {
        Long channelId = channelService.createChannel(request.serverId(), request.categoryId(), request.name(), request.type());
        return ResponseEntity.ok(channelId);
    }

    @PatchMapping("/{channelId}")
    @Operation(summary = "채널 이름 수정", description = "채널의 이름을 변경합니다.")
    public ResponseEntity<Void> updateChannel(@PathVariable Long channelId, @RequestBody Map<String, String> request) {
        channelService.updateChannel(channelId, request.get("name"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{channelId}")
    @Operation(summary = "채널 삭제", description = "채널을 삭제합니다.")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long channelId) {
        channelService.deleteChannel(channelId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/order")
    @Operation(summary = "채널 순서 변경", description = "카테고리 내 채널들의 순서를 변경합니다.")
    public ResponseEntity<Void> updateOrder(@RequestParam Long categoryId, @RequestBody List<Long> orderedIds) {
        channelService.updateChannelOrder(categoryId, orderedIds);
        return ResponseEntity.ok().build();
    }

    public record CreateChannelRequest(Long serverId, Long categoryId, String name, String type) {}
}
