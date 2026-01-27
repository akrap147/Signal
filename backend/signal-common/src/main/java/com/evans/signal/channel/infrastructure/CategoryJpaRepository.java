package com.evans.signal.channel.infrastructure;

import com.evans.signal.channel.infrastructure.entity.CategoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findAllByServerId(Long serverId);
}
