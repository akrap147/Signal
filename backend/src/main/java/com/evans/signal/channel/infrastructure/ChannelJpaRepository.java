package com.evans.signal.channel.infrastructure;

import com.evans.signal.channel.infrastructure.entity.ChannelEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelJpaRepository extends JpaRepository<ChannelEntity, Long> {
    List<ChannelEntity> findAllByCategoryId(Long categoryId);

    List<ChannelEntity> findAllByServerId(Long serverId);
}
