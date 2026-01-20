package com.evans.signal.server.controller;

import com.evans.signal.server.domain.Server;
import com.evans.signal.server.dto.ServerCreateDto;
import com.evans.signal.server.dto.response.MemberResponse;
import com.evans.signal.server.dto.response.ServerDetailResponse;
import com.evans.signal.server.service.ServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
@Tag(name = "Server API", description = "Server management endpoints (Discord Guilds)")
public class ServerController {

    private final ServerService serverService;

    @PostMapping
    @Operation(summary = "새 서버 생성", description = "새로운 서버를 생성하며, 기본 채널(일반)과 카테고리를 함께 생성합니다.")
    public ResponseEntity<Long> createServer(@RequestBody ServerCreateDto dto) {
        Long serverId = serverService.createServer(dto);
        return ResponseEntity.ok(serverId);
    }

    @GetMapping("/my")
    @Operation(summary = "내 서버 목록 조회", description = "사용자가 가입한 서버 목록을 조회합니다.")
    public ResponseEntity<List<SimpleServerResponse>> getMyServers(@RequestParam Long userId) {
        // 추후 SecurityContextHolder에서 userId 추출 예정
        List<Server> servers = serverService.findAllMyServers(userId);
        List<SimpleServerResponse> response = servers.stream()
                .map(s -> new SimpleServerResponse(s.getId(), s.getName(), s.getIconImage()))
                .toList();

        return ResponseEntity.ok(response);
    }

    public record SimpleServerResponse(Long id, String name, String iconImage) {}

    @GetMapping("/{serverId}")
    @Operation(summary = "서버 상세 정보 조회", description = "카테고리 및 채널 목록을 포함한 서버의 모든 상세 정보를 조회합니다.")
    public ResponseEntity<ServerDetailResponse> getServerDetails(@PathVariable Long serverId) {
        return ResponseEntity.ok(serverService.getServerDetails(serverId));
    }

    @PostMapping("/join")
    @Operation(summary = "서버 가입 (초대 코드)", description = "초대 코드를 사용하여 서버에 가입합니다.")
    public ResponseEntity<Long> joinServer(@RequestBody Map<String, Object> request) {
        String inviteCode = (String) request.get("inviteCode");
        Long userId = Long.valueOf(request.get("userId").toString());
        Long memberId = serverService.joinServer(inviteCode, userId);
        return ResponseEntity.ok(memberId);
    }

    @GetMapping("/{serverId}/members")
    @Operation(summary = "서버 멤버 목록 조회", description = "해당 서버에 속한 모든 멤버 정보를 조회합니다.")
    public ResponseEntity<List<MemberResponse>> getServerMembers(@PathVariable Long serverId) {
        return ResponseEntity.ok(serverService.getServerMembers(serverId));
    }
    
    @DeleteMapping("/{serverId}")
    @Operation(summary = "서버 삭제", description = "서버 소유자(Owner)만 서버를 삭제할 수 있습니다.")
    public ResponseEntity<String> deleteServer(@PathVariable Long serverId, @RequestParam Long userId) {
        serverService.deleteServer(serverId, userId);
        return ResponseEntity.ok("Server deleted successfully.");
    }

    @PostMapping("/{serverId}/leave")
    @Operation(summary = "서버 나가기 / 탈퇴", description = "사용자가 서버에서 나갑니다 (소유자는 나갈 수 없음).")
    public ResponseEntity<String> leaveServer(@PathVariable Long serverId, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        serverService.leaveServer(serverId, userId);
        return ResponseEntity.ok("Left server successfully.");
    }
}
