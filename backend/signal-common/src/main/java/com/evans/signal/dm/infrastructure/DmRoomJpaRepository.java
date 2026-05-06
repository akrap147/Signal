package com.evans.signal.dm.infrastructure;

import com.evans.signal.dm.infrastructure.entity.DmRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DmRoomJpaRepository extends JpaRepository<DmRoomEntity, Long> {

    Optional<DmRoomEntity> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

    List<DmRoomEntity> findAllByUser1IdOrUser2Id(Long user1Id, Long user2Id);
}
