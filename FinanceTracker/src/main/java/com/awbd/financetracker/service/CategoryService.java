package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Category createCategory(Long userId, Category category);

    Optional<Category> getCategoryById(Long id);

    List<Category> getAllCategories();

    List<Category> getCategoriesByUserId(Long userId);

    Page<Category> getCategoriesByUserId(Long userId, Pageable pageable);

    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);
}

