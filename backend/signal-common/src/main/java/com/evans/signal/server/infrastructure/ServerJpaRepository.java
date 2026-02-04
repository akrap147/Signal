package com.evans.signal.server.infrastructure;

import com.evans.signal.server.infrastructure.entity.ServerEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerJpaRepository extends JpaRepository<ServerEntity, Long> {
}
