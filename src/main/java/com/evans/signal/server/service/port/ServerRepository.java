package com.evans.signal.server.service.port;

import com.evans.signal.server.domain.Server;

import java.util.Optional;

public interface ServerRepository {
    Server save(Server server);

    Optional<Server> findByInviteCode(String inviteCode);
}
