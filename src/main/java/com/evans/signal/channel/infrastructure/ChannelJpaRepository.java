package com.evans.signal.channel.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelJpaRepository extends JpaRepository<ChannelEntity, Long> {
}
