package com.evans.signal.friendship.service;

import com.evans.signal.friendship.domain.Friendship;
import com.evans.signal.friendship.domain.FriendshipStatus;
import com.evans.signal.friendship.dto.FriendshipResponseDto;
import com.evans.signal.friendship.exception.FriendshipErrorCode;
import com.evans.signal.friendship.service.port.FriendshipRepository;
import com.evans.signal.global.exception.CustomException;
import com.evans.signal.user.domain.User;
import com.evans.signal.user.dto.UserResponseDto;
import com.evans.signal.user.exception.UserErrorCode;
import com.evans.signal.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * 친구 요청 보내기
     * A가 B에게 친구 요청 시 (A, B, 'pending') row 생성
     */
    @Transactional
    public FriendshipResponseDto sendFriendRequest(Long userId, String friendEmail) {
        // 1. 친구 대상 사용자 존재 확인
        User friend = userRepository.findByEmail(friendEmail)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 2. 이미 친구 관계가 존재하는지 확인 (양방향)
        if (friendshipRepository.existsFriendshipBetween(userId, friend.getId())) {
            throw new CustomException(FriendshipErrorCode.FRIENDSHIP_ALREADY_EXISTS);
        }

        // 3. 친구 요청 생성
        Friendship friendship = Friendship.createRequest(userId, friend.getId());
        Friendship savedFriendship = friendshipRepository.save(friendship);

        return FriendshipResponseDto.from(savedFriendship, UserResponseDto.from(friend));
    }

    /**
     * 친구 요청 수락
     * B가 A의 요청을 수락하면:
     * 1. (A, B, 'pending') -> (A, B, 'accepted')로 변경
     * 2. (B, A, 'accepted') row 추가
     */
    @Transactional
    public void acceptFriendRequest(Long userId, Long requesterId) {
        // 1. 친구 요청 조회 (requesterId가 userId에게 보낸 요청)
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(requesterId, userId)
                .orElseThrow(() -> new CustomException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND));

        // 2. 수락 권한 확인 (요청을 받은 사람만 수락 가능)
        if (!friendship.isReceiver(userId)) {
            throw new CustomException(FriendshipErrorCode.NOT_AUTHORIZED_TO_ACCEPT);
        }

        // 3. 요청 수락 (상태를 accepted로 변경)
        friendship.accept();
        friendshipRepository.save(friendship);

        // 4. 역방향 친구 관계 생성 (B, A, 'accepted')
        Friendship reverseFriendship = friendship.createReverseFriendship();
        friendshipRepository.save(reverseFriendship);
    }

    /**
     * 친구 요청 거절 또는 친구 삭제
     */
    @Transactional
    public void removeFriendship(Long userId, Long friendId) {
        // userId -> friendId 관계 삭제
        friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .ifPresent(friendshipRepository::delete);

        // friendId -> userId 관계도 삭제 (양방향)
        friendshipRepository.findByUserIdAndFriendId(friendId, userId)
                .ifPresent(friendshipRepository::delete);
    }

    /**
     * 내 친구 목록 조회 (accepted 상태만)
     */
    @Transactional(readOnly = true)
    public List<FriendshipResponseDto> getMyFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED);

        return friendships.stream()
                .map(friendship -> {
                    User friend = userRepository.findById(friendship.getFriendId())
                            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
                    return FriendshipResponseDto.from(friendship, UserResponseDto.from(friend));
                })
                .collect(Collectors.toList());
    }

    /**
     * 내가 보낸 친구 요청 목록 조회 (pending 상태)
     */
    @Transactional(readOnly = true)
    public List<FriendshipResponseDto> getMySentRequests(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.PENDING);

        return friendships.stream()
                .map(friendship -> {
                    User friend = userRepository.findById(friendship.getFriendId())
                            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
                    return FriendshipResponseDto.from(friendship, UserResponseDto.from(friend));
                })
                .collect(Collectors.toList());
    }

    /**
     * 내가 받은 친구 요청 목록 조회 (pending 상태)
     */
    @Transactional(readOnly = true)
    public List<FriendshipResponseDto> getMyReceivedRequests(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByFriendIdAndStatus(userId, FriendshipStatus.PENDING);

        return friendships.stream()
                .map(friendship -> {
                    User requester = userRepository.findById(friendship.getUserId())
                            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
                    return FriendshipResponseDto.from(friendship, UserResponseDto.from(requester));
                })
                .collect(Collectors.toList());
    }
}
