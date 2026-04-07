package com.evans.signal.server.controller;

import com.evans.signal.server.domain.Server;
import com.evans.signal.server.dto.ServerCreateDto;
import com.evans.signal.server.dto.request.ServerJoinRequest;
import com.evans.signal.server.dto.request.ServerUpdateNameRequest;
import com.evans.signal.server.dto.response.InviteCodeResponse;
import com.evans.signal.server.dto.response.MemberResponse;
import com.evans.signal.server.dto.response.ServerDetailResponse;
import com.evans.signal.server.dto.response.SimpleServerResponse;
import com.evans.signal.server.dto.response.ServerIdResponse;
import com.evans.signal.server.dto.response.MemberIdResponse;
import com.evans.signal.common.dto.response.MessageResponse;
import com.evans.signal.server.service.ServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
@Tag(name = "Server API", description = "서버 관리 endPoints")
public class ServerController {

    private final ServerService serverService;

    @PostMapping
    @Operation(summary = "새 서버 생성", description = "새로운 서버를 생성하며, 기본 채널(일반)과 카테고리를 함께 생성합니다.")
    public ResponseEntity<ServerIdResponse> createServer(@AuthenticationPrincipal Long userId, @RequestBody ServerCreateDto dto) {
        Long serverId = serverService.createServer(dto, userId);
        return ResponseEntity.ok(new ServerIdResponse(serverId));
    }

    @GetMapping("/my")
    @Operation(summary = "내 서버 목록 조회", description = "사용자가 가입한 서버 목록을 조회합니다.")
    public ResponseEntity<List<SimpleServerResponse>> getMyServers(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(serverService.findAllMyServers(userId));
    }

    @GetMapping("/{serverId}")
    @Operation(summary = "서버 상세 정보 조회", description = "카테고리 및 채널 목록을 포함한 서버의 모든 상세 정보를 조회합니다.")
    public ResponseEntity<ServerDetailResponse> getServerDetails(@PathVariable Long serverId) {
        return ResponseEntity.ok(serverService.getServerDetails(serverId));
    }

    @PostMapping("/join")
    @Operation(summary = "서버 가입 (초대 코드)", description = "초대 코드를 사용하여 서버에 가입합니다. POST /join 요청 시 inviteCode를 Body에 담아 보냅니다.")
    public ResponseEntity<ServerIdResponse> joinServer(@AuthenticationPrincipal Long userId, @RequestBody ServerJoinRequest request) {
        Long serverId = serverService.joinServer(request.getInviteCode(), userId);
        return ResponseEntity.ok(new ServerIdResponse(serverId));
    }

    @GetMapping("/{serverId}/members")
    @Operation(summary = "서버 멤버 목록 조회", description = "해당 서버에 속한 모든 멤버 정보를 조회합니다.")
    public ResponseEntity<List<MemberResponse>> getServerMembers(@PathVariable Long serverId) {
        return ResponseEntity.ok(serverService.getServerMembers(serverId));
    }

    @PostMapping("/{serverId}/invites")
    @Operation(summary = "초대 코드 생성", description = "서버에 대한 1시간 유효한 초대 코드를 생성합니다. (방장만 가능)")
    public ResponseEntity<InviteCodeResponse> createInviteCode(@PathVariable Long serverId, @AuthenticationPrincipal Long userId) {
        String inviteCode = serverService.createInviteCode(serverId, userId);
        return ResponseEntity.ok(new InviteCodeResponse(inviteCode));
    }

    @DeleteMapping("/{serverId}")
    @Operation(summary = "서버 삭제", description = "서버 소유자(Owner)만 서버를 삭제할 수 있습니다.")
    public ResponseEntity<MessageResponse> deleteServer(@PathVariable Long serverId, @AuthenticationPrincipal Long userId) {
        serverService.deleteServer(serverId, userId);
        return ResponseEntity.ok(new MessageResponse("서버가 성공적으로 삭제되었습니다."));
    }

    @PostMapping("/{serverId}/leave")
    @Operation(summary = "서버 나가기 / 탈퇴", description = "사용자가 서버에서 나갑니다 (소유자는 나갈 수 없음).")
    public ResponseEntity<MessageResponse> leaveServer(@PathVariable Long serverId, @AuthenticationPrincipal Long userId) {
        serverService.leaveServer(serverId, userId);
        return ResponseEntity.ok(new MessageResponse("성공적으로 서버를 나갔습니다."));
    }

    @PatchMapping("/{serverId}/name")
    @Operation(summary = "서버 이름 변경", description = "서버 이름을 변경합니다. (방장만 가능)")
    public ResponseEntity<MessageResponse> updateServerName(@PathVariable Long serverId, @RequestBody ServerUpdateNameRequest request, @AuthenticationPrincipal Long userId) {
        serverService.updateServerName(serverId, request.getName(), userId);
        return ResponseEntity.ok(new MessageResponse("서버 이름이 성공적으로 변경되었습니다."));
    }
}
