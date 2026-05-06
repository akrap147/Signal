package com.evans.signal.channel.service;

import com.evans.signal.channel.domain.Category;
import com.evans.signal.channel.service.port.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

import com.evans.signal.channel.domain.Channel;
import com.evans.signal.channel.service.port.ChannelRepository;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ChannelRepository channelRepository;

    @Transactional
    public void updateCategoryOrder(Long serverId, List<Long> orderedCategoryIds) {
        List<Category> categories = categoryRepository.findAllByServerId(serverId);

        Map<Long, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        for (int i = 0; i < orderedCategoryIds.size(); i++) {
            Category category = categoryMap.get(orderedCategoryIds.get(i));
            if (category != null) {
                category.updateDisplayOrder(i);
                categoryRepository.save(category);
            }
        }
    }

    @Transactional
    public Long createCategory(Long serverId, String name) {
        Category category = Category.create(serverId, name, 0);
        return categoryRepository.save(category).getId();
    }

    @Transactional
    public void updateCategory(Long categoryId, String name) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        category.updateName(name);
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        List<Channel> channels = channelRepository.findAllByCategoryId(categoryId);
        channels.forEach(channel -> channelRepository.deleteById(channel.getId()));

        categoryRepository.deleteById(categoryId);
    }
}