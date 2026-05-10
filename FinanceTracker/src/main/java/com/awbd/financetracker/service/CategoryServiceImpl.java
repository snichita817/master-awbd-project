package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Category createCategory(Long userId, Category category) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (categoryRepository.existsByNameAndUserId(category.getName(), userId)) {
            throw new DuplicateResourceException("Category with name '" + category.getName() + "' already exists for this user");
        }

        category.setUser(user);
        Category saved = categoryRepository.save(category);
        log.info("Category created: id={}, name='{}', userId={}", saved.getId(), saved.getName(), userId);
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
    public List<Category> getCategoriesByUserId(Long userId) {
        if(!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return categoryRepository.findByUserId(userId);
    }

    @Override
    public Category updateCategory(Long id, Category updatedCategory) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if name is being changed to one that already exists
        if (!category.getName().equals(updatedCategory.getName()) &&
            categoryRepository.existsByNameAndUserId(updatedCategory.getName(), category.getUser().getId())) {
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

        // Remove category reference from all subscriptions before deleting
        for (var subscription : category.getSubscriptions()) {
            subscription.setCategory(null);
        }

        categoryRepository.delete(category);
        log.info("Category deleted: id={}", id);
    }
}

