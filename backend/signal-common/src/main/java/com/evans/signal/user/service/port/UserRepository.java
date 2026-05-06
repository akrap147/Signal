package com.evans.signal.user.service.port;

import com.evans.signal.user.domain.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByName(String userName);
}
