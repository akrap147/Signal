package com.evans.signal.friendship.controller;

import com.evans.signal.friendship.dto.FriendRequestDto;
import com.evans.signal.friendship.dto.FriendshipResponseDto;
import com.evans.signal.friendship.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    /**
     * 친구 요청 보내기
     * POST /api/friends/request
     */
    @PostMapping("/request")
    public ResponseEntity<FriendshipResponseDto> sendFriendRequest(
            @AuthenticationPrincipal Long userId,
            @RequestBody FriendRequestDto dto) {
        FriendshipResponseDto response = friendshipService.sendFriendRequest(userId, dto.getFriendId());
        return ResponseEntity.ok(response);
    }

    /**
     * 친구 요청 수락
     * POST /api/friends/accept/{requesterId}
     */
    @PostMapping("/accept/{requesterId}")
    public ResponseEntity<Void> acceptFriendRequest(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long requesterId) {
        friendshipService.acceptFriendRequest(userId, requesterId);
        return ResponseEntity.ok().build();
    }

    /**
     * 친구 요청 거절 또는 친구 삭제
     * DELETE /api/friends/{friendId}
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriendship(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long friendId) {
        friendshipService.removeFriendship(userId, friendId);
        return ResponseEntity.ok().build();
    }

    /**
     * 내 친구 목록 조회 (accepted 상태)
     * GET /api/friends
     */
    @GetMapping
    public ResponseEntity<List<FriendshipResponseDto>> getMyFriends(
            @AuthenticationPrincipal Long userId) {
        List<FriendshipResponseDto> friends = friendshipService.getMyFriends(userId);
        return ResponseEntity.ok(friends);
    }

    /**
     * 내가 보낸 친구 요청 목록 조회 (pending 상태)
     * GET /api/friends/sent-requests
     */
    @GetMapping("/sent-requests")
    public ResponseEntity<List<FriendshipResponseDto>> getMySentRequests(
            @AuthenticationPrincipal Long userId) {
        List<FriendshipResponseDto> requests = friendshipService.getMySentRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * 내가 받은 친구 요청 목록 조회 (pending 상태)
     * GET /api/friends/received-requests
     */
    @GetMapping("/received-requests")
    public ResponseEntity<List<FriendshipResponseDto>> getMyReceivedRequests(
            @AuthenticationPrincipal Long userId) {
        List<FriendshipResponseDto> requests = friendshipService.getMyReceivedRequests(userId);
        return ResponseEntity.ok(requests);
    }
}
