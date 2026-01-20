package com.evans.signal.channel.infrastructure;

import com.evans.signal.channel.domain.Category;
import com.evans.signal.channel.infrastructure.entity.CategoryEntity;
import com.evans.signal.channel.service.port.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Category save(Category category) {
        CategoryEntity entity = CategoryMapper.toEntity(category);
        CategoryEntity savedEntity = categoryJpaRepository.save(entity);
        return CategoryMapper.toDomain(savedEntity);
    }

    @Override
    public List<Category> findAllByServerId(Long serverId) {
        return categoryJpaRepository.findAllByServerId(serverId).stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpaRepository.findById(id)
                .map(CategoryMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        categoryJpaRepository.deleteById(id);
    }
}
