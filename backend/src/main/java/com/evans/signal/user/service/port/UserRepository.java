package com.evans.signal.user.service.port;

import com.evans.signal.user.domain.User;

public interface UserRepository {
    User save(User user);
    // User findByEmail(String email); // 나중에 필요할 것
}
