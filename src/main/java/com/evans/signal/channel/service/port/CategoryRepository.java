package com.evans.signal.channel.service.port;

import com.evans.signal.channel.domain.Category;

public interface CategoryRepository {
    Category save(Category category);
}
