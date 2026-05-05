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

    @PostMapping("/request")
    public ResponseEntity<FriendshipResponseDto> sendFriendRequest(
            @AuthenticationPrincipal Long userId,
            @RequestBody FriendRequestDto dto) {
        FriendshipResponseDto response = friendshipService.sendFriendRequest(userId, dto.getFriendName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accept/{requesterId}")
    public ResponseEntity<Void> acceptFriendRequest(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long requesterId) {
        friendshipService.acceptFriendRequest(userId, requesterId);
        return ResponseEntity.ok().build();
    }


    // todo: friendId -> friendName
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriendship(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long friendId) {
        friendshipService.removeFriendship(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<FriendshipResponseDto>> getMyFriends(
            @AuthenticationPrincipal Long userId) {
        List<FriendshipResponseDto> friends = friendshipService.getMyFriends(userId);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/sent-requests")
    public ResponseEntity<List<FriendshipResponseDto>> getMySentRequests(
            @AuthenticationPrincipal Long userId) {
        List<FriendshipResponseDto> requests = friendshipService.getMySentRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/received-requests")
    public ResponseEntity<List<FriendshipResponseDto>> getMyReceivedRequests(
            @AuthenticationPrincipal Long userId) {
        List<FriendshipResponseDto> requests = friendshipService.getMyReceivedRequests(userId);
        return ResponseEntity.ok(requests);
    }
}
