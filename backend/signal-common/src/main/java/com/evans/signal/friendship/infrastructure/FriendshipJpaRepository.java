package com.evans.signal.friendship.infrastructure;

import com.evans.signal.friendship.domain.FriendshipStatus;
import com.evans.signal.friendship.infrastructure.entity.FriendshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipJpaRepository extends JpaRepository<FriendshipEntity, Long> {

    /**
     * 특정 사용자 간의 친구 관계 조회
     */
    Optional<FriendshipEntity> findByUserIdAndFriendId(Long userId, Long friendId);

    /**
     * 특정 사용자의 모든 친구 관계 조회 (상태별)
     */
    List<FriendshipEntity> findByUserIdAndStatus(Long userId, FriendshipStatus status);

    /**
     * 특정 사용자가 받은 친구 요청 조회 (pending 상태)
     */
    List<FriendshipEntity> findByFriendIdAndStatus(Long friendId, FriendshipStatus status);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
}
