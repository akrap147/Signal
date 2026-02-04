package com.evans.signal.server.service.port;

import com.evans.signal.server.domain.Server;

import java.util.List;
import java.util.Optional;

public interface ServerRepository {
    Server save(Server server);

    List<Server> findAllById(List<Long> ids);

    Optional<Server> findById(Long id);

    void deleteById(Long id);
}
