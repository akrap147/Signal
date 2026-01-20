package com.evans.signal.channel.controller;

import com.evans.signal.channel.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category API", description = "Manage server categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "카테고리 생성", description = "서버 내에 새로운 카테고리를 생성합니다.")
    public ResponseEntity<Long> createCategory(@RequestBody CreateCategoryRequest request) {
        Long categoryId = categoryService.createCategory(request.serverId(), request.name());
        return ResponseEntity.ok(categoryId);
    }

    @PatchMapping("/{categoryId}")
    @Operation(summary = "카테고리 이름 수정", description = "카테고리의 이름을 변경합니다.")
    public ResponseEntity<Void> updateCategory(@PathVariable Long categoryId, @RequestBody Map<String, String> request) {
        categoryService.updateCategory(categoryId, request.get("name"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다. (하위 채널도 함께 삭제됨)")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/order")
    @Operation(summary = "카테고리 순서 변경", description = "카테고리 ID 목록 순서대로 정렬을 업데이트합니다.")
    public ResponseEntity<Void> updateOrder(@RequestParam Long serverId, @RequestBody List<Long> orderedIds) {
        categoryService.updateCategoryOrder(serverId, orderedIds);
        return ResponseEntity.ok().build();
    }

    // DTO Inner Class
    public record CreateCategoryRequest(Long serverId, String name) {}
}
