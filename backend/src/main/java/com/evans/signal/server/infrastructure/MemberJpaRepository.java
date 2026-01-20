package com.evans.signal.server.infrastructure;

import com.evans.signal.server.infrastructure.entity.MemberEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {
    List<MemberEntity> findAllByUserId(Long userId);

    List<MemberEntity> findAllByServerId(Long serverId);

    void deleteByServerIdAndUserId(Long serverId, Long userId);
}
