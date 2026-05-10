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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IT3: Create subscription with renewalDate = today + 3 days; assert it appears in upcoming renewals.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IT3_UpcomingRenewalsTest {

    @Autowired
    private UserService userService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    void subscriptionRenewingIn3Days_appearsInUpcomingRenewals() {
        User user = userService.createUser("Grace", "grace@it3.com", new BigDecimal("3000.00"));

        // Renewal in 3 days  -  within the 30-day window
        Subscription upcoming = new Subscription("Hulu", new BigDecimal("12.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(3), user);
        subscriptionRepository.save(upcoming);

        // Renewal in 10 days  -  outside the 7-day window
        Subscription distant = new Subscription("Disney+", new BigDecimal("8.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(10), user);
        subscriptionRepository.save(distant);

        List<Subscription> renewals = financeService.getUpcomingRenewals(user.getId());

        assertThat(renewals).hasSize(1);
        assertThat(renewals.get(0).getName()).isEqualTo("Hulu");
    }

    @Test
    void subscriptionRenewingToday_appearsInUpcomingRenewals() {
        User user = userService.createUser("Henry", "henry@it3.com", new BigDecimal("2000.00"));

        Subscription today = new Subscription("Prime Video", new BigDecimal("9.00"),
                BillingFrequency.MONTHLY, LocalDate.now(), user);
        subscriptionRepository.save(today);

        List<Subscription> renewals = financeService.getUpcomingRenewals(user.getId());

        assertThat(renewals).hasSize(1);
        assertThat(renewals.get(0).getName()).isEqualTo("Prime Video");
    }

    @Test
    void noSubscriptions_upcomingRenewalsIsEmpty() {
        User user = userService.createUser("Iris", "iris@it3.com", new BigDecimal("1500.00"));

        List<Subscription> renewals = financeService.getUpcomingRenewals(user.getId());

        assertThat(renewals).isEmpty();
    }
}
