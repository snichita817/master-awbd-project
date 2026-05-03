package com.awbd.financetracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

@WebMvcTest(CategoryController.class)
@WithMockUser
@ActiveProfiles("test")
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Category category;

    @BeforeEach
    void setUp(){
        category = new Category();
        category.setId(1L);
        category.setName("Entertainment");
    }

    @Test
    void createCategory_ShouldReturnCreatedCategory() throws Exception {
        when(categoryService.createCategory(eq(1L), any(Category.class)))
                .thenReturn(category);

        mockMvc.perform(post("/api/categories/user/{userId}", 1L)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Entertainment"));

        verify(categoryService).createCategory(eq(1L), any(Category.class));
    }

    @Test
    void getCategoryById_ShouldReturnCategory() throws Exception {
        when(categoryService.getCategoryById(1L))
                .thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/categories/{categoryId}", 1L)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Entertainment"));

        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void updateCategory_ShouldReturnCategory() throws Exception {
        category.setName("Other");

        when(categoryService.updateCategory(eq(1L), any(Category.class)))
                .thenReturn(category);

        mockMvc.perform(put("/api/categories/{categoryId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Other"));

        verify(categoryService).updateCategory(eq(1L), any(Category.class));
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/categories/{categoryId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }
}