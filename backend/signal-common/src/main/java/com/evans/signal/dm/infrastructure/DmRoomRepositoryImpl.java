package com.evans.signal.dm.infrastructure;

import com.evans.signal.dm.domain.DmRoom;
import com.evans.signal.dm.infrastructure.entity.DmRoomEntity;
import com.evans.signal.dm.service.port.DmRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DmRoomRepositoryImpl implements DmRoomRepository {

    private final DmRoomJpaRepository dmRoomJpaRepository;

    @Override
    public DmRoom save(DmRoom dmRoom) {
        DmRoomEntity entity = DmRoomEntity.builder()
                .id(dmRoom.getId())
                .user1Id(dmRoom.getUser1Id())
                .user2Id(dmRoom.getUser2Id())
                .channelId(dmRoom.getChannelId())
                .build();
        DmRoomEntity saved = dmRoomJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<DmRoom> findByUsers(Long user1Id, Long user2Id) {
        // unique constraint는 (user1, user2) 순서로만 저장하므로 min/max로 조회
        long min = Math.min(user1Id, user2Id);
        long max = Math.max(user1Id, user2Id);
        return dmRoomJpaRepository.findByUser1IdAndUser2Id(min, max).map(this::toDomain);
    }

    @Override
    public List<DmRoom> findAllByUserId(Long userId) {
        return dmRoomJpaRepository.findAllByUser1IdOrUser2Id(userId, userId).stream()
                .map(this::toDomain)
                .toList();
    }

    private DmRoom toDomain(DmRoomEntity entity) {
        return DmRoom.builder()
                .id(entity.getId())
                .user1Id(entity.getUser1Id())
                .user2Id(entity.getUser2Id())
                .channelId(entity.getChannelId())
                .build();
    }
}
