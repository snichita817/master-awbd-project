package com.awbd.financetracker;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.enums.BillingFrequency;
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
class IT3_UpcomingRenewalsTest {

    @MockitoBean
    private UserDirectoryClient userDirectoryClient;

    @Autowired
    private SubscriptionService subscriptionService;

    @Test
    void subscriptionRenewingInThreeDaysAppearsInUpcomingRenewals() {
        subscriptionService.createSubscription(
                7L,
                null,
                null,
                subscription("Hulu", "12.00", LocalDate.now().plusDays(3))
        );
        subscriptionService.createSubscription(
                7L,
                null,
                null,
                subscription("Disney+", "8.00", LocalDate.now().plusDays(10))
        );

        var renewals = subscriptionService.getUpcomingRenewals(7L);

        assertThat(renewals).hasSize(1);
        assertThat(renewals.get(0).getName()).isEqualTo("Hulu");
    }

    @Test
    void subscriptionRenewingTodayAppearsInUpcomingRenewals() {
        subscriptionService.createSubscription(
                7L,
                null,
                null,
                subscription("Prime Video", "9.00", LocalDate.now())
        );

        var renewals = subscriptionService.getUpcomingRenewals(7L);

        assertThat(renewals).hasSize(1);
        assertThat(renewals.get(0).getName()).isEqualTo("Prime Video");
    }

    @Test
    void noSubscriptionsReturnsEmptyUpcomingRenewals() {
        var renewals = subscriptionService.getUpcomingRenewals(7L);

        assertThat(renewals).isEmpty();
    }

    private static Subscription subscription(String name, String price, LocalDate renewalDate) {
        Subscription subscription = new Subscription();
        subscription.setName(name);
        subscription.setPrice(new BigDecimal(price));
        subscription.setBillingFrequency(BillingFrequency.MONTHLY);
        subscription.setRenewalDate(renewalDate);
        return subscription;
    }
}
