package com.evans.signal.user.infrastructure;

import com.evans.signal.user.domain.User;
import com.evans.signal.user.infrastructure.entity.UserEntity;
import com.evans.signal.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        // 1. Domain -> Entity 변환
        UserEntity entity = UserMapper.toEntity(user);
        
        // 2. DB 저장
        UserEntity savedEntity = userJpaRepository.save(entity);
        
        // 3. Entity -> Domain 변환해서 리턴
        return UserMapper.toDomain(savedEntity);
    }

    // 나중에 findByEmail 등도 여기서 구현
}
