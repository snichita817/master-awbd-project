package com.awbd.financetracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.awbd.financetracker.entity.Budget;
import com.awbd.financetracker.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetController.class)
@WithMockUser
class BudgetControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Budget budget;

    @BeforeEach
    void setUp(){
        budget = new Budget();
        budget.setId(1L);
        budget.setMaxLimit(new BigDecimal("500.00"));
        budget.setCurrentSpending(new BigDecimal("150.00"));
    }

    @Test
    void createBudget_ShouldReturnCreatedBudget() throws Exception {
        when(budgetService.createBudget(eq(1L), any(Budget.class)))
                .thenReturn(budget);

        Budget requestBudget = new Budget();
        requestBudget.setMaxLimit(new BigDecimal("500.00"));
        requestBudget.setCurrentSpending(new BigDecimal("150.00"));

        mockMvc.perform(post("/api/budgets/category/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBudget)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maxLimit").value(500.00))
                .andExpect(jsonPath("$.currentSpending").value(150.00));

        verify(budgetService).createBudget(eq(1L), any(Budget.class));
    }

    @Test
    void getBudgetById_ShouldReturnBudget() throws Exception {
        when(budgetService.getBudgetById(1L))
                .thenReturn(Optional.of(budget));

        mockMvc.perform(get("/api/budgets/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maxLimit").value(500.00))
                .andExpect(jsonPath("$.currentSpending").value(150.00));

        verify(budgetService).getBudgetById(1L);
    }

    @Test
    void getBudgetById_WhenNotFound_ShouldThrowException() throws Exception {
        when(budgetService.getBudgetById(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/budgets/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(budgetService).getBudgetById(1L);
    }

    @Test
    void getBudgetByCategoryId_ShouldReturnBudget() throws Exception {
        when(budgetService.getBudgetByCategoryId(1L))
                .thenReturn(Optional.of(budget));

        mockMvc.perform(get("/api/budgets/category/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maxLimit").value(500.00));

        verify(budgetService).getBudgetByCategoryId(1L);
    }

    @Test
    void getBudgetsByUserId_ShouldReturnListOfBudgets() throws Exception {
        List<Budget> budgets = Arrays.asList(budget);
        when(budgetService.getBudgetsByUserId(1L)).thenReturn(budgets);

        mockMvc.perform(get("/api/budgets/user/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].maxLimit").value(500.00))
                .andExpect(jsonPath("$[0].currentSpending").value(150.00));

        verify(budgetService).getBudgetsByUserId(1L);
    }

    @Test
    void updateBudget_WhenDecreasingCurrentSpending_ShouldThrowException() throws Exception {
        Budget requestBudget = new Budget();
        requestBudget.setMaxLimit(new BigDecimal("500.00"));
        requestBudget.setCurrentSpending(new BigDecimal("100.00"));

        when(budgetService.updateBudget(eq(1L), any(Budget.class)))
                .thenThrow(new IllegalArgumentException("Current spending cannot be decreased manually."));

        mockMvc.perform(put("/api/budgets/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBudget)))
                .andExpect(status().isNotFound());

        verify(budgetService).updateBudget(eq(1L), any(Budget.class));
    }

    @Test
    void deleteBudget_ShouldReturnNoContent() throws Exception {
        doNothing().when(budgetService).deleteBudget(1L);

        mockMvc.perform(delete("/api/budgets/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(budgetService).deleteBudget(1L);
    }
}