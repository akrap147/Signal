package com.evans.signal.channel.infrastructure;

import com.evans.signal.channel.domain.Category;
import com.evans.signal.channel.service.port.CategoryRepository;
import com.evans.signal.server.infrastructure.ServerEntity;
import com.evans.signal.server.infrastructure.ServerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;
    private final ServerJpaRepository serverJpaRepository;

    @Override
    public Category save(Category category) {
        ServerEntity serverEntity = serverJpaRepository.findById(category.getServerId())
                .orElseThrow(() -> new IllegalArgumentException("Server not found with ID: " + category.getServerId()));

        CategoryEntity entity = ChannelMapper.toEntity(category, serverEntity);
        CategoryEntity savedEntity = categoryJpaRepository.save(entity);
        return ChannelMapper.toDomain(savedEntity);
    }
}
