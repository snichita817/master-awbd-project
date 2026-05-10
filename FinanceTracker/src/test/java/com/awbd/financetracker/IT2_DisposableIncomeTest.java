package com.awbd.financetracker;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.service.FinanceService;
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
 * IT2: Create subscriptions -> call FinanceService -> assert disposable income formula is correct.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IT2_DisposableIncomeTest {

    @Autowired
    private UserService userService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    void disposableIncome_equalsIncomeMinusTotalMonthlyCost() {
        // Income: 2000.00
        User user = userService.createUser("Dave", "dave@it2.com", new BigDecimal("2000.00"));

        // Monthly subscription: 50.00
        Subscription s1 = new Subscription("Netflix", new BigDecimal("50.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(10), user);
        subscriptionRepository.save(s1);

        // Yearly subscription: 120.00 -> 10.00/month
        Subscription s2 = new Subscription("Adobe CC", new BigDecimal("120.00"),
                BillingFrequency.YEARLY, LocalDate.now().plusDays(200), user);
        subscriptionRepository.save(s2);

        // Total monthly cost = 50.00 + 10.00 = 60.00
        // Disposable income  = 2000.00 - 60.00 = 1940.00
        BigDecimal disposable = financeService.calculateDisposableIncome(user.getId());

        assertThat(disposable).isEqualByComparingTo("1940.00");
    }

    @Test
    void disposableIncome_noSubscriptions_equalsFullIncome() {
        User user = userService.createUser("Eve", "eve@it2.com", new BigDecimal("3500.00"));

        BigDecimal disposable = financeService.calculateDisposableIncome(user.getId());

        assertThat(disposable).isEqualByComparingTo("3500.00");
    }

    @Test
    void totalMonthlySubscriptionCost_onlyMonthly_sumIsCorrect() {
        User user = userService.createUser("Frank", "frank@it2.com", new BigDecimal("1000.00"));

        subscriptionRepository.save(new Subscription("S1", new BigDecimal("20.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(5), user));
        subscriptionRepository.save(new Subscription("S2", new BigDecimal("30.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(15), user));

        BigDecimal total = financeService.calculateTotalMonthlySubscriptionCost(user.getId());

        assertThat(total).isEqualByComparingTo("50.00");
    }
}
