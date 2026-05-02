package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Budget;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.enums.BillingFrequency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BudgetService {

    Budget createBudget(Long categoryId, Budget budget);

    Optional<Budget> getBudgetById(Long id);

    Optional<Budget> getBudgetByCategoryId(Long categoryId);

    List<Budget> getBudgetsByUserId(Long userId);

    List<Budget> getAllBudgets();

    Budget updateBudget(Long id, Budget budget);

    void deleteBudget(Long id);

    // Subscription-related budget updates
    void addSubscriptionToBudget(Category category, BigDecimal price, BillingFrequency frequency);

    void removeSubscriptionFromBudget(Category category, BigDecimal price, BillingFrequency frequency);
}

