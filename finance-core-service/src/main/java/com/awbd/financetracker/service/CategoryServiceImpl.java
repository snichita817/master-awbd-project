package com.awbd.financetracker.service;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final UserDirectoryClient userDirectoryClient;

    public CategoryServiceImpl(CategoryRepository categoryRepository, UserDirectoryClient userDirectoryClient) {
        this.categoryRepository = categoryRepository;
        this.userDirectoryClient = userDirectoryClient;
    }

    @Override
    public Category createCategory(Long ownerUserId, Category category) {
        userDirectoryClient.requireUser(ownerUserId);
        if (categoryRepository.existsByNameAndOwnerUserId(category.getName(), ownerUserId)) {
            throw new DuplicateResourceException("Category with name '" + category.getName() + "' already exists for this user");
        }
        category.setOwnerUserId(ownerUserId);
        Category saved = categoryRepository.save(category);
        log.info("Category created: id={}, name='{}', ownerUserId={}", saved.getId(), saved.getName(), ownerUserId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByOwnerUserId(Long ownerUserId) {
        userDirectoryClient.requireUser(ownerUserId);
        return categoryRepository.findByOwnerUserId(ownerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> getCategoriesByOwnerUserId(Long ownerUserId, Pageable pageable) {
        userDirectoryClient.requireUser(ownerUserId);
        return categoryRepository.findByOwnerUserId(ownerUserId, pageable);
    }

    @Override
    public Category updateCategory(Long id, Category updatedCategory) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (!category.getName().equals(updatedCategory.getName())
                && categoryRepository.existsByNameAndOwnerUserId(updatedCategory.getName(), category.getOwnerUserId())) {
            throw new DuplicateResourceException("Category with name '" + updatedCategory.getName() + "' already exists for this user");
        }
        category.setName(updatedCategory.getName());
        category.setDescription(updatedCategory.getDescription());
        Category saved = categoryRepository.save(category);
        log.info("Category updated: id={}, name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        for (Subscription subscription : category.getSubscriptions()) {
            subscription.setCategory(null);
        }
        categoryRepository.delete(category);
        log.info("Category deleted: id={}", id);
    }
}
