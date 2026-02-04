package com.evans.signal.friendship.controller;

import com.evans.signal.friendship.domain.FriendshipStatus;
import com.evans.signal.friendship.dto.FriendRequestDto;
import com.evans.signal.friendship.dto.FriendshipResponseDto;
import com.evans.signal.friendship.service.FriendshipService;
import com.evans.signal.user.dto.UserResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipControllerTest {

    @InjectMocks
    private FriendshipController friendshipController;

    @Mock
    private FriendshipService friendshipService;

    @Test
    @DisplayName("친구 요청 보내기 성공")
    void sendFriendRequest_success() {
        // given
        Long userId = 1L;
        FriendRequestDto dto = new FriendRequestDto(2L);
        
        UserResponseDto friendInfo = UserResponseDto.builder()
                .id(2L)
                .email("friend@test.com")
                .username("friend")
                .build();

        FriendshipResponseDto response = FriendshipResponseDto.builder()
                .id(1L)
                .userId(userId)
                .friendId(2L)
                .status(FriendshipStatus.PENDING)
                .friendInfo(friendInfo)
                .createdAt(LocalDateTime.now())
                .build();

        given(friendshipService.sendFriendRequest(userId, dto.getFriendId())).willReturn(response);

        // when
        ResponseEntity<FriendshipResponseDto> result = friendshipController.sendFriendRequest(userId, dto);

        // then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getUserId()).isEqualTo(userId);
        assertThat(result.getBody().getFriendId()).isEqualTo(2L);
        assertThat(result.getBody().getStatus()).isEqualTo(FriendshipStatus.PENDING);
        verify(friendshipService).sendFriendRequest(userId, dto.getFriendId());
    }

    @Test
    @DisplayName("친구 요청 수락 성공")
    void acceptFriendRequest_success() {
        // given
        Long userId = 2L;
        Long requesterId = 1L;

        doNothing().when(friendshipService).acceptFriendRequest(userId, requesterId);

        // when
        ResponseEntity<Void> result = friendshipController.acceptFriendRequest(userId, requesterId);

        // then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        verify(friendshipService).acceptFriendRequest(userId, requesterId);
    }

    @Test
    @DisplayName("친구 삭제 성공")
    void removeFriendship_success() {
        // given
        Long userId = 1L;
        Long friendId = 2L;

        doNothing().when(friendshipService).removeFriendship(userId, friendId);

        // when
        ResponseEntity<Void> result = friendshipController.removeFriendship(userId, friendId);

        // then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        verify(friendshipService).removeFriendship(userId, friendId);
    }

    @Test
    @DisplayName("내 친구 목록 조회 성공")
    void getMyFriends_success() {
        // given
        Long userId = 1L;
        
        UserResponseDto friend1Info = UserResponseDto.builder()
                .id(2L)
                .username("friend1")
                .build();
        
        UserResponseDto friend2Info = UserResponseDto.builder()
                .id(3L)
                .username("friend2")
                .build();

        FriendshipResponseDto friendship1 = FriendshipResponseDto.builder()
                .id(1L)
                .userId(userId)
                .friendId(2L)
                .status(FriendshipStatus.ACCEPTED)
                .friendInfo(friend1Info)
                .build();

        FriendshipResponseDto friendship2 = FriendshipResponseDto.builder()
                .id(2L)
                .userId(userId)
                .friendId(3L)
                .status(FriendshipStatus.ACCEPTED)
                .friendInfo(friend2Info)
                .build();

        List<FriendshipResponseDto> friends = Arrays.asList(friendship1, friendship2);
        given(friendshipService.getMyFriends(userId)).willReturn(friends);

        // when
        ResponseEntity<List<FriendshipResponseDto>> result = friendshipController.getMyFriends(userId);

        // then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).hasSize(2);
        assertThat(result.getBody().get(0).getFriendInfo().getUsername()).isEqualTo("friend1");
        assertThat(result.getBody().get(1).getFriendInfo().getUsername()).isEqualTo("friend2");
        verify(friendshipService).getMyFriends(userId);
    }

    @Test
    @DisplayName("내가 보낸 친구 요청 목록 조회 성공")
    void getMySentRequests_success() {
        // given
        Long userId = 1L;
        
        UserResponseDto friendInfo = UserResponseDto.builder()
                .id(2L)
                .username("friend")
                .build();

        FriendshipResponseDto request = FriendshipResponseDto.builder()
                .id(1L)
                .userId(userId)
                .friendId(2L)
                .status(FriendshipStatus.PENDING)
                .friendInfo(friendInfo)
                .build();

        List<FriendshipResponseDto> requests = Arrays.asList(request);
        given(friendshipService.getMySentRequests(userId)).willReturn(requests);

        // when
        ResponseEntity<List<FriendshipResponseDto>> result = friendshipController.getMySentRequests(userId);

        // then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody().get(0).getStatus()).isEqualTo(FriendshipStatus.PENDING);
        verify(friendshipService).getMySentRequests(userId);
    }

    @Test
    @DisplayName("내가 받은 친구 요청 목록 조회 성공")
    void getMyReceivedRequests_success() {
        // given
        Long userId = 2L;
        
        UserResponseDto requesterInfo = UserResponseDto.builder()
                .id(1L)
                .username("requester")
                .build();

        FriendshipResponseDto request = FriendshipResponseDto.builder()
                .id(1L)
                .userId(1L)
                .friendId(userId)
                .status(FriendshipStatus.PENDING)
                .friendInfo(requesterInfo)
                .build();

        List<FriendshipResponseDto> requests = Arrays.asList(request);
        given(friendshipService.getMyReceivedRequests(userId)).willReturn(requests);

        // when
        ResponseEntity<List<FriendshipResponseDto>> result = friendshipController.getMyReceivedRequests(userId);

        // then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody().get(0).getStatus()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(result.getBody().get(0).getFriendInfo().getUsername()).isEqualTo("requester");
        verify(friendshipService).getMyReceivedRequests(userId);
    }
}
