package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Category createCategory(Long ownerUserId, Category category);

    Optional<Category> getCategoryById(Long id);

    List<Category> getAllCategories();

    List<Category> getCategoriesByOwnerUserId(Long ownerUserId);

    Page<Category> getCategoriesByOwnerUserId(Long ownerUserId, Pageable pageable);

    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);
}
