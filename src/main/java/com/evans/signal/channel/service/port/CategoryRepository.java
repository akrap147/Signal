package com.evans.signal.channel.service.port;

import com.evans.signal.channel.domain.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);

    Optional<Category> findById(Long id);

    void deleteById(Long id);

    List<Category> findAllByServerId(Long serverId);
}
