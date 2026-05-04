package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", new BigDecimal("3000.00"));
        user.setId(1L);
    }

    @Test
    void createCategory_happyPath_savesAndReturnsCategory() {
        Category category = new Category("Streaming", "Video", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameAndUserId("Streaming", 1L)).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        Category result = categoryService.createCategory(1L, category);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    void createCategory_duplicateNameSameUser_throwsDuplicateResourceException() {
        Category category = new Category("Streaming", "Video", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameAndUserId("Streaming", 1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(1L, category))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Streaming");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createCategory_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory(99L, new Category()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateCategory_duplicateNewName_throwsDuplicateResourceException() {
        Category existing = new Category("Streaming", "Video", user);
        existing.setId(10L);

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndUserId("Music", 1L)).thenReturn(true);

        Category updated = new Category("Music", "Music streaming", null);

        assertThatThrownBy(() -> categoryService.updateCategory(10L, updated))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Music");
    }

    @Test
    void deleteCategory_nonExistingId_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
