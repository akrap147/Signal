package com.evans.signal.channel.infrastructure;

import com.evans.signal.channel.infrastructure.entity.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelJpaRepository extends JpaRepository<ChannelEntity, Long> {
    List<ChannelEntity> findAllByCategoryId(Long categoryId);

    List<ChannelEntity> findAllByServerId(Long serverId);
}
