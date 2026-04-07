package com.evans.signal.friendship.service.port;

import com.evans.signal.friendship.domain.Friendship;
import com.evans.signal.friendship.domain.FriendshipStatus;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository {
    
    /**
     * 친구 관계 저장
     */
    Friendship save(Friendship friendship);
    
    /**
     * ID로 친구 관계 조회
     */
    Optional<Friendship> findById(Long id);
    
    /**
     * 특정 사용자 간의 친구 관계 조회
     */
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
    
    /**
     * 특정 사용자의 친구 목록 조회 (상태별)
     */
    List<Friendship> findByUserIdAndStatus(Long userId, FriendshipStatus status);
    
    /**
     * 특정 사용자가 받은 친구 요청 조회
     */
    List<Friendship> findByFriendIdAndStatus(Long friendId, FriendshipStatus status);
    
    /**
     * 두 사용자 간에 친구 관계가 존재하는지 확인
     */
    boolean existsFriendshipBetween(Long userId, Long friendId);
    
    /**
     * 친구 관계 삭제
     */
    void delete(Friendship friendship);
}
