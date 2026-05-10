package com.awbd.financetracker;

import com.awbd.financetracker.entity.Budget;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.repository.BudgetRepository;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.UserRepository;
import com.awbd.financetracker.service.BudgetService;
import com.awbd.financetracker.service.SubscriptionService;
import com.awbd.financetracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IT1: Create user -> category -> budget -> subscription; assert budget.currentSpending is updated.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IT1_BudgetAutoUpdateTest {

    @Autowired
    private UserService userService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createSubscription_shouldUpdateBudgetCurrentSpending() {
        // 1. Create user
        User user = userService.createUser("Bob", "bob@it1.com", new BigDecimal("4000.00"));

        // 2. Create category
        Category category = new Category("Entertainment", "Fun stuff", user);
        category = categoryRepository.save(category);

        // 3. Create budget for the category (maxLimit=200, currentSpending initialised to 0)
        Budget budget = new Budget();
        budget.setMaxLimit(new BigDecimal("200.00"));
        budget = budgetService.createBudget(category.getId(), budget);

        assertThat(budget.getCurrentSpending()).isEqualByComparingTo("0.00");

        // 4. Create subscription linked to the same category
        Subscription subscription = new Subscription();
        subscription.setName("Spotify");
        subscription.setPrice(new BigDecimal("10.00"));
        subscription.setBillingFrequency(BillingFrequency.MONTHLY);
        subscription.setRenewalDate(LocalDate.now().plusDays(15));

        subscriptionService.createSubscription(user.getId(), category.getId(), null, subscription);

        // 5. Assert budget current spending was increased by the monthly cost (10.00)
        Budget updated = budgetRepository.findById(budget.getId()).orElseThrow();
        assertThat(updated.getCurrentSpending()).isEqualByComparingTo("10.00");
    }

    @Test
    void createYearlySubscription_shouldUpdateBudgetWithMonthlyEquivalent() {
        // 120.00 / 12 = 10.00 monthly
        User user = userService.createUser("Carol", "carol@it1.com", new BigDecimal("5000.00"));

        Category category = new Category("Software", "Dev tools", user);
        category = categoryRepository.save(category);

        Budget budget = new Budget();
        budget.setMaxLimit(new BigDecimal("500.00"));
        budget = budgetService.createBudget(category.getId(), budget);

        Subscription subscription = new Subscription();
        subscription.setName("JetBrains");
        subscription.setPrice(new BigDecimal("120.00"));
        subscription.setBillingFrequency(BillingFrequency.YEARLY);
        subscription.setRenewalDate(LocalDate.now().plusDays(300));

        subscriptionService.createSubscription(user.getId(), category.getId(), null, subscription);

        Budget updated = budgetRepository.findById(budget.getId()).orElseThrow();
        // 120 / 12 = 10.00
        assertThat(updated.getCurrentSpending()).isEqualByComparingTo("10.00");
    }
}
