package com.awbd.financetracker.controllers;

import com.awbd.financetracker.dto.*;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/owner/{ownerUserId}")
    public ResponseEntity<CategoryDto> createCategory(@PathVariable Long ownerUserId,
                                                      @Valid @RequestBody CategoryUpsertDto request) {
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinanceCoreMapper.toDto(categoryService.createCategory(ownerUserId, category)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(FinanceCoreMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerUserId}")
    public ResponseEntity<PageResponse<CategoryDto>> getCategoriesByOwner(@PathVariable Long ownerUserId,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "5") int size,
                                                                          @RequestParam(defaultValue = "name") String sort,
                                                                          @RequestParam(defaultValue = "asc") String dir) {
        Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        var categoryPage = categoryService.getCategoriesByOwnerUserId(ownerUserId, PageRequest.of(page, size, Sort.by(direction, sort)))
                .map(FinanceCoreMapper::toDto);
        return ResponseEntity.ok(PageResponse.from(categoryPage));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories().stream().map(FinanceCoreMapper::toDto).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id,
                                                      @Valid @RequestBody CategoryUpsertDto request) {
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return ResponseEntity.ok(FinanceCoreMapper.toDto(categoryService.updateCategory(id, category)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
