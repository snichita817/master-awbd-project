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
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IT2_SubscriptionMonthlyCostTest {

    @MockitoBean
    private UserDirectoryClient userDirectoryClient;

    @Autowired
    private SubscriptionService subscriptionService;

    @Test
    void ownerSubscriptionsCanBeReadAndNormalisedToMonthlyCost() {
        subscriptionService.createSubscription(
                7L,
                null,
                null,
                subscription("Netflix", "50.00", BillingFrequency.MONTHLY, LocalDate.now().plusDays(10))
        );
        subscriptionService.createSubscription(
                7L,
                null,
                null,
                subscription("Adobe CC", "120.00", BillingFrequency.YEARLY, LocalDate.now().plusDays(200))
        );

        BigDecimal totalMonthlyCost = subscriptionService.getSubscriptionsByOwnerUserId(7L).stream()
                .map(IT2_SubscriptionMonthlyCostTest::monthlyCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(totalMonthlyCost).isEqualByComparingTo("60.00");
    }

    private static BigDecimal monthlyCost(Subscription subscription) {
        if (subscription.getBillingFrequency() == BillingFrequency.MONTHLY) {
            return subscription.getPrice();
        }
        return subscription.getPrice().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
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
