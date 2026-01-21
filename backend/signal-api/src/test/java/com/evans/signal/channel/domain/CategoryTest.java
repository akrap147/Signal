package com.evans.signal.channel.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    @DisplayName("자신의 카테고리에 속한 채널만 필터링하고 순서대로 정렬한다")
    void filterAndSortChannels() {
        // given
        Long categoryId = 10L;
        Long otherCategoryId = 20L;
        Category category = Category.builder().id(categoryId).build();

        Channel ch1 = Channel.builder().id(1L).categoryId(categoryId).displayOrder(2).name("Second").build();
        Channel ch2 = Channel.builder().id(2L).categoryId(categoryId).displayOrder(1).name("First").build();
        Channel otherCh = Channel.builder().id(3L).categoryId(otherCategoryId).displayOrder(1).name("Other").build();

        List<Channel> allChannels = List.of(ch1, ch2, otherCh);

        // when
        List<Channel> result = category.filterAndSortChannels(allChannels);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("First");  // Order 1
        assertThat(result.get(1).getName()).isEqualTo("Second"); // Order 2
    }
}
