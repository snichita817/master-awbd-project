package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Budget;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.BudgetRepository;
import com.awbd.financetracker.repository.CategoryRepository;
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
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private User user;
    private Category category;
    private Budget budget;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", new BigDecimal("3000.00"));
        user.setId(1L);

        category = new Category("Streaming", "Video streaming", user);
        category.setId(10L);

        budget = new Budget(new BigDecimal("500.00"), new BigDecimal("50.00"), category);
        budget.setId(5L);
    }

    @Test
    void addSubscriptionToBudget_monthlyPrice_increasesCurrentSpending() {
        when(budgetRepository.findByCategoryId(10L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        budgetService.addSubscriptionToBudget(category, new BigDecimal("30.00"), BillingFrequency.MONTHLY);

        assertThat(budget.getCurrentSpending()).isEqualByComparingTo("80.00");
        verify(budgetRepository).save(budget);
    }

    @Test
    void addSubscriptionToBudget_yearlyPrice_dividedByTwelveAndAdded() {
        when(budgetRepository.findByCategoryId(10L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // 120.00 / 12 = 10.00; currentSpending was 50.00, should become 60.00
        budgetService.addSubscriptionToBudget(category, new BigDecimal("120.00"), BillingFrequency.YEARLY);

        assertThat(budget.getCurrentSpending()).isEqualByComparingTo("60.00");
    }

    @Test
    void removeSubscriptionFromBudget_decreasesCurrentSpending() {
        when(budgetRepository.findByCategoryId(10L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        budgetService.removeSubscriptionFromBudget(category, new BigDecimal("30.00"), BillingFrequency.MONTHLY);

        assertThat(budget.getCurrentSpending()).isEqualByComparingTo("20.00");
    }

    @Test
    void removeSubscriptionFromBudget_doesNotGoBelowZero() {
        when(budgetRepository.findByCategoryId(10L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // spending is 50.00, removing 200.00 should clamp to 0
        budgetService.removeSubscriptionFromBudget(category, new BigDecimal("200.00"), BillingFrequency.MONTHLY);

        assertThat(budget.getCurrentSpending()).isEqualByComparingTo("0.00");
    }

    @Test
    void addSubscriptionToBudget_nullCategory_doesNothing() {
        budgetService.addSubscriptionToBudget(null, new BigDecimal("30.00"), BillingFrequency.MONTHLY);

        verifyNoInteractions(budgetRepository);
    }

    @Test
    void createBudget_categoryAlreadyHasBudget_throwsDuplicateResourceException() {
        category.setBudget(budget);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        Budget newBudget = new Budget();
        newBudget.setMaxLimit(new BigDecimal("300.00"));

        assertThatThrownBy(() -> budgetService.createBudget(10L, newBudget))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("10");
    }

    @Test
    void deleteBudget_nonExistingId_throwsResourceNotFoundException() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.deleteBudget(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createBudget_happyPath_savesWithZeroInitialSpending() {
        category.setBudget(null); // no existing budget
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(budgetRepository.save(any())).thenAnswer(inv -> {
            Budget b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        Budget newBudget = new Budget();
        newBudget.setMaxLimit(new BigDecimal("300.00"));

        Budget result = budgetService.createBudget(10L, newBudget);

        assertThat(result.getCurrentSpending()).isEqualByComparingTo("0.00");
        assertThat(result.getMaxLimit()).isEqualByComparingTo("300.00");
        verify(budgetRepository).save(newBudget);
    }

    @Test
    void createBudget_categoryNotFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.createBudget(99L, new Budget()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateBudget_happyPath_updatesMaxLimit() {
        when(budgetRepository.findById(5L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Budget updateRequest = new Budget();
        updateRequest.setMaxLimit(new BigDecimal("800.00"));

        Budget result = budgetService.updateBudget(5L, updateRequest);

        assertThat(result.getMaxLimit()).isEqualByComparingTo("800.00");
        verify(budgetRepository).save(budget);
    }

    @Test
    void updateBudget_notFound_throwsResourceNotFoundException() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.updateBudget(99L, new Budget()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
