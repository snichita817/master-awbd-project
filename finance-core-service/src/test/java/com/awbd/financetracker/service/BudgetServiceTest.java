package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Budget;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.repository.BudgetRepository;
import com.awbd.financetracker.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private BudgetServiceImpl budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetServiceImpl(budgetRepository, categoryRepository);
    }

    @Test
    void createBudgetInitialisesCurrentSpendingFromExistingSubscriptions() {
        Category category = category(3L);
        category.getSubscriptions().add(subscription("30.00", BillingFrequency.MONTHLY));
        category.getSubscriptions().add(subscription("120.00", BillingFrequency.YEARLY));
        Budget input = budget("500.00", null);

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(budgetRepository.save(input)).thenReturn(input);

        Budget result = budgetService.createBudget(3L, input);

        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getCurrentSpending()).isEqualByComparingTo("40.00");
    }

    @Test
    void createBudgetRejectsSecondBudgetForSameCategory() {
        Category category = category(3L);
        category.setBudget(budget("200.00", "0.00"));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> budgetService.createBudget(3L, budget("500.00", null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void addSubscriptionToBudgetUsesMonthlyEquivalentForYearlyPrice() {
        Category category = category(3L);
        Budget existing = budget("500.00", "20.00");
        when(budgetRepository.findByCategoryId(3L)).thenReturn(Optional.of(existing));
        when(budgetRepository.save(existing)).thenReturn(existing);

        budgetService.addSubscriptionToBudget(category, new BigDecimal("120.00"), BillingFrequency.YEARLY);

        assertThat(existing.getCurrentSpending()).isEqualByComparingTo("30.00");
        verify(budgetRepository).save(existing);
    }

    @Test
    void removeSubscriptionFromBudgetDoesNotGoBelowZero() {
        Category category = category(3L);
        Budget existing = budget("500.00", "5.00");
        when(budgetRepository.findByCategoryId(3L)).thenReturn(Optional.of(existing));
        when(budgetRepository.save(existing)).thenReturn(existing);

        budgetService.removeSubscriptionFromBudget(category, new BigDecimal("30.00"), BillingFrequency.MONTHLY);

        assertThat(existing.getCurrentSpending()).isEqualByComparingTo("0.00");
        verify(budgetRepository).save(existing);
    }

    private static Category category(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Entertainment");
        category.setOwnerUserId(7L);
        return category;
    }

    private static Budget budget(String maxLimit, String currentSpending) {
        Budget budget = new Budget();
        budget.setMaxLimit(new BigDecimal(maxLimit));
        if (currentSpending != null) {
            budget.setCurrentSpending(new BigDecimal(currentSpending));
        }
        return budget;
    }

    private static Subscription subscription(String price, BillingFrequency frequency) {
        Subscription subscription = new Subscription();
        subscription.setName("Subscription");
        subscription.setPrice(new BigDecimal(price));
        subscription.setBillingFrequency(frequency);
        subscription.setRenewalDate(LocalDate.now().plusDays(5));
        return subscription;
    }
}
