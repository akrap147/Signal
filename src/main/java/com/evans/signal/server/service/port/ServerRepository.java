package com.evans.signal.server.service.port;

import com.evans.signal.server.domain.Server;

public interface ServerRepository {
    Server save(Server server);
}
