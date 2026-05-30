package com.awbd.financetracker;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.Budget;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.repository.BudgetRepository;
import com.awbd.financetracker.service.BudgetService;
import com.awbd.financetracker.service.CategoryService;
import com.awbd.financetracker.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IT1_BudgetAutoUpdateTest {

    @MockitoBean
    private UserDirectoryClient userDirectoryClient;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Test
    void createMonthlySubscriptionUpdatesBudgetCurrentSpending() {
        Category category = categoryService.createCategory(7L, category("Entertainment", "Fun stuff"));

        Budget budget = budget("200.00");
        budget = budgetService.createBudget(category.getId(), budget);
        assertThat(budget.getCurrentSpending()).isEqualByComparingTo("0.00");

        subscriptionService.createSubscription(
                7L,
                category.getId(),
                null,
                subscription("Spotify", "10.00", BillingFrequency.MONTHLY, LocalDate.now().plusDays(15))
        );

        Budget updated = budgetRepository.findById(budget.getId()).orElseThrow();
        assertThat(updated.getCurrentSpending()).isEqualByComparingTo("10.00");
    }

    @Test
    void createYearlySubscriptionUpdatesBudgetWithMonthlyEquivalent() {
        Category category = categoryService.createCategory(7L, category("Software", "Dev tools"));

        Budget budget = budgetService.createBudget(category.getId(), budget("500.00"));

        subscriptionService.createSubscription(
                7L,
                category.getId(),
                null,
                subscription("JetBrains", "120.00", BillingFrequency.YEARLY, LocalDate.now().plusDays(300))
        );

        Budget updated = budgetRepository.findById(budget.getId()).orElseThrow();
        assertThat(updated.getCurrentSpending()).isEqualByComparingTo("10.00");
    }

    private static Category category(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    private static Budget budget(String maxLimit) {
        Budget budget = new Budget();
        budget.setMaxLimit(new BigDecimal(maxLimit));
        return budget;
    }

    private static Subscription subscription(String name, String price, BillingFrequency frequency, LocalDate renewalDate) {
        Subscription subscription = new Subscription();
        subscription.setName(name);
        subscription.setPrice(new BigDecimal(price));
        subscription.setBillingFrequency(frequency);
        subscription.setRenewalDate(renewalDate);
        return subscription;
    }
}
