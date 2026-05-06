package com.evans.signal.dm.controller;

import com.evans.signal.dm.service.DmService;
import com.evans.signal.dm.service.DmService.DmChannelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
@Tag(name = "DM API", description = "Direct Message channels")
public class DmController {

    private final DmService dmService;

    @PostMapping
    @Operation(summary = "DM 채널 생성 또는 조회")
    public ResponseEntity<DmChannelResponse> getOrCreateDmChannel(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long friendId) {
        return ResponseEntity.ok(dmService.getOrCreateDmChannel(userId, friendId));
    }

    @GetMapping
    @Operation(summary = "내 DM 채널 목록 조회")
    public ResponseEntity<List<DmChannelResponse>> getMyDmChannels(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(dmService.getMyDmChannels(userId));
    }
}
