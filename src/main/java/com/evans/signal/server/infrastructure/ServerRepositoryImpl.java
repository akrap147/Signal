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

    @Override
    public java.util.Optional<Server> findByInviteCode(String inviteCode) {
        return serverJpaRepository.findByInviteCode(inviteCode)
                .map(ServerMapper::toDomain);
    }

    @Override
    public java.util.List<Server> findAllById(java.util.List<Long> ids) {
        return serverJpaRepository.findAllById(ids).stream()
                .map(ServerMapper::toDomain)
                .toList();
    }
}
