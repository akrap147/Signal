package com.evans.signal.server.infrastructure;

import com.evans.signal.server.domain.Server;
import com.evans.signal.server.infrastructure.entity.ServerEntity;
import com.evans.signal.server.service.port.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    public List<Server> findAllById(java.util.List<Long> ids) {
        return serverJpaRepository.findAllById(ids).stream()
                .map(ServerMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Server> findById(Long id) {
        return serverJpaRepository.findById(id)
                .map(ServerMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        serverJpaRepository.deleteById(id);
    }
}
