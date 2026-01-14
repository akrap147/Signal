package com.evans.signal.server.infrastructure;

import com.evans.signal.server.domain.Server;
import com.evans.signal.server.infrastructure.entity.ServerEntity;
import com.evans.signal.server.service.port.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ServerRepositoryImpl implements ServerRepository {

    private final ServerJpaRepository serverJpaRepository;

    @Override
    public Server save(Server server) {
        ServerEntity entity = ServerMapper.toEntity(server);
        ServerEntity savedEntity = serverJpaRepository.save(entity);
        return ServerMapper.toDomain(savedEntity);
    }
}
