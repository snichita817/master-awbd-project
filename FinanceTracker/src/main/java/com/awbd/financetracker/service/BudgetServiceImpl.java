package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Budget;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.BudgetRepository;
import com.awbd.financetracker.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BudgetServiceImpl implements BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetServiceImpl.class);

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Budget createBudget(Long categoryId, Budget budget) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (category.getBudget() != null) {
            throw new DuplicateResourceException("Budget already exists for category id: " + categoryId);
        }

        budget.setCategory(category);

        if (budget.getCurrentSpending() == null) {
            budget.setCurrentSpending(BigDecimal.ZERO);
        }

        var userSubscriptions = category.getSubscriptions();

        for (var subscription : userSubscriptions) {
            BigDecimal monthlyCost = toMonthlyCost(subscription.getPrice(), subscription.getBillingFrequency());
            budget.setCurrentSpending(budget.getCurrentSpending().add(monthlyCost));
        }

        Budget saved = budgetRepository.save(budget);
        log.info("Budget created: id={}, categoryId={}, maxLimit={}", saved.getId(), categoryId, saved.getMaxLimit());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Budget> getBudgetById(Long id) {
        return budgetRepository.findByIdWithCategory(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Budget> getBudgetByCategoryId(Long categoryId) {
        return budgetRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByUserId(Long userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    @Override
    public Budget updateBudget(Long id, Budget updatedBudget) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        budget.setMaxLimit(updatedBudget.getMaxLimit());
        if (updatedBudget.getCurrentSpending() != null) {
            if (budget.getCurrentSpending().compareTo(updatedBudget.getCurrentSpending()) > 0) {
                throw new IllegalArgumentException("Current spending cannot be decreased manually.");
            }
            budget.setCurrentSpending(updatedBudget.getCurrentSpending());
        }

        Budget saved = budgetRepository.save(budget);
        log.info("Budget updated: id={}, maxLimit={}, currentSpending={}", saved.getId(), saved.getMaxLimit(), saved.getCurrentSpending());
        return saved;
    }

    @Override
    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        budgetRepository.delete(budget);
        log.info("Budget deleted: id={}", id);
    }

    @Override
    public void addSubscriptionToBudget(Category category, BigDecimal price, BillingFrequency frequency) {
        if (category == null) return;

        budgetRepository.findByCategoryId(category.getId()).ifPresent(budget -> {
            BigDecimal monthlyCost = toMonthlyCost(price, frequency);
            budget.setCurrentSpending(budget.getCurrentSpending().add(monthlyCost));
            budgetRepository.save(budget);
        });
    }

    @Override
    public void removeSubscriptionFromBudget(Category category, BigDecimal price, BillingFrequency frequency) {
        if (category == null) return;

        budgetRepository.findByCategoryId(category.getId()).ifPresent(budget -> {
            BigDecimal monthlyCost = toMonthlyCost(price, frequency);
            BigDecimal newSpending = budget.getCurrentSpending().subtract(monthlyCost);
            // Don't go below zero
            budget.setCurrentSpending(newSpending.max(BigDecimal.ZERO));
            budgetRepository.save(budget);
        });
    }

    private BigDecimal toMonthlyCost(BigDecimal price, BillingFrequency frequency) {
        if (price == null || frequency == null) return BigDecimal.ZERO;
        return frequency == BillingFrequency.YEARLY
                ? price.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
                : price;
    }
}

