package com.evans.signal.user.infrastructure;

import com.evans.signal.user.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// 실제 JPA가 동작하는 인터페이스 (DB와 1:1)
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
}
