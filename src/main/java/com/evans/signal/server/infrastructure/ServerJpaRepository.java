package com.evans.signal.server.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerJpaRepository extends JpaRepository<ServerEntity, Long> {
}
