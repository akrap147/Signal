package com.evans.signal.dm.service.port;

import com.evans.signal.dm.domain.DmRoom;

import java.util.List;
import java.util.Optional;

public interface DmRoomRepository {
    DmRoom save(DmRoom dmRoom);

    Optional<DmRoom> findByUsers(Long user1Id, Long user2Id);

    List<DmRoom> findAllByUserId(Long userId);
}
