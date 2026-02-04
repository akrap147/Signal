package com.evans.signal.friendship.infrastructure;

import com.evans.signal.friendship.domain.Friendship;
import com.evans.signal.friendship.domain.FriendshipStatus;
import com.evans.signal.friendship.infrastructure.entity.FriendshipEntity;
import com.evans.signal.friendship.service.port.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FriendshipRepositoryImpl implements FriendshipRepository {

    private final FriendshipJpaRepository friendshipJpaRepository;

    @Override
    public Friendship save(Friendship friendship) {
        FriendshipEntity entity = FriendshipMapper.toEntity(friendship);
        FriendshipEntity savedEntity = friendshipJpaRepository.save(entity);
        return FriendshipMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Friendship> findById(Long id) {
        return friendshipJpaRepository.findById(id)
                .map(FriendshipMapper::toDomain);
    }

    @Override
    public Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId) {
        return friendshipJpaRepository.findByUserIdAndFriendId(userId, friendId)
                .map(FriendshipMapper::toDomain);
    }

    @Override
    public List<Friendship> findByUserIdAndStatus(Long userId, FriendshipStatus status) {
        return friendshipJpaRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(FriendshipMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Friendship> findByFriendIdAndStatus(Long friendId, FriendshipStatus status) {
        return friendshipJpaRepository.findByFriendIdAndStatus(friendId, status)
                .stream()
                .map(FriendshipMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsFriendshipBetween(Long userId, Long friendId) {
        return friendshipJpaRepository.existsByUserIdAndFriendId(userId, friendId);
    }

    @Override
    public void delete(Friendship friendship) {
        FriendshipEntity entity = FriendshipMapper.toEntity(friendship);
        friendshipJpaRepository.delete(entity);
    }
}
