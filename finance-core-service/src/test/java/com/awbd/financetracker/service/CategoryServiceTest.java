package com.awbd.financetracker.service;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserDirectoryClient userDirectoryClient;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository, userDirectoryClient);
    }

    @Test
    void createCategoryAssignsOwnerAndSavesWhenNameIsUnique() {
        Category input = category("Streaming", "Subscriptions", null);
        Category saved = category("Streaming", "Subscriptions", 7L);
        saved.setId(10L);

        when(categoryRepository.existsByNameAndOwnerUserId("Streaming", 7L)).thenReturn(false);
        when(categoryRepository.save(input)).thenReturn(saved);

        Category result = categoryService.createCategory(7L, input);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(input.getOwnerUserId()).isEqualTo(7L);
        verify(userDirectoryClient).requireUser(7L);
        verify(categoryRepository).save(input);
    }

    @Test
    void createCategoryThrowsWhenDuplicateNameExistsForOwner() {
        Category input = category("Streaming", null, null);
        when(categoryRepository.existsByNameAndOwnerUserId("Streaming", 7L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(7L, input))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Streaming");

        verify(userDirectoryClient).requireUser(7L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoriesByOwnerValidatesUserAndReturnsPagedData() {
        PageRequest pageRequest = PageRequest.of(0, 5);
        Category category = category("Bills", null, 7L);
        when(categoryRepository.findByOwnerUserId(7L, pageRequest))
                .thenReturn(new PageImpl<>(List.of(category), pageRequest, 1));

        var result = categoryService.getCategoriesByOwnerUserId(7L, pageRequest);

        assertThat(result.getContent()).containsExactly(category);
        verify(userDirectoryClient).requireUser(7L);
    }

    @Test
    void updateCategoryThrowsWhenCategoryDoesNotExist() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, category("Food", null, 7L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    private static Category category(String name, String description, Long ownerUserId) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setOwnerUserId(ownerUserId);
        return category;
    }
}
