package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionShareRepository subscriptionShareRepository;

    @InjectMocks
    private FinanceServiceImpl financeService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", new BigDecimal("3000.00"));
        user.setId(1L);
    }

    @Test
    void calculateDisposableIncome_subtractsTotalMonthlyFromIncome() {
        Subscription monthly = new Subscription("Netflix", new BigDecimal("15.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(10), user);
        Subscription yearly = new Subscription("Adobe", new BigDecimal("120.00"),
                BillingFrequency.YEARLY, LocalDate.now().plusDays(60), user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(List.of(monthly, yearly));
        when(subscriptionShareRepository.findBySubscriptionOwnerId(1L)).thenReturn(List.of());

        // total monthly = 15.00 + (120.00 / 12) = 15.00 + 10.00 = 25.00
        // disposable = 3000.00 - 25.00 = 2975.00
        BigDecimal result = financeService.calculateDisposableIncome(1L);

        assertThat(result).isEqualByComparingTo("2975.00");
    }

    @Test
    void calculateDisposableIncome_noSubscriptions_returnsFullIncome() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(List.of());
        when(subscriptionShareRepository.findBySubscriptionOwnerId(1L)).thenReturn(List.of());

        BigDecimal result = financeService.calculateDisposableIncome(1L);

        assertThat(result).isEqualByComparingTo("3000.00");
    }

    @Test
    void calculateDisposableIncome_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financeService.calculateDisposableIncome(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getUpcomingRenewals_returnsSubscriptionsWithin30Days() {
        Subscription soon = new Subscription("Netflix", new BigDecimal("15.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(7), user);

        when(subscriptionRepository.findUpcomingRenewals(1L)).thenReturn(List.of(soon));

        List<Subscription> result = financeService.getUpcomingRenewals(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Netflix");
    }

    @Test
    void calculateTotalMonthlySubscriptionCost_mixedFrequencies_correctSum() {
        Subscription s1 = new Subscription("S1", new BigDecimal("10.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(5), user);
        Subscription s2 = new Subscription("S2", new BigDecimal("24.00"),
                BillingFrequency.YEARLY, LocalDate.now().plusDays(200), user);

        when(subscriptionRepository.findByUserId(1L)).thenReturn(List.of(s1, s2));
        when(subscriptionShareRepository.findBySubscriptionOwnerId(1L)).thenReturn(List.of());

        // 10.00 + (24.00 / 12) = 10.00 + 2.00 = 12.00
        BigDecimal result = financeService.calculateTotalMonthlySubscriptionCost(1L);

        assertThat(result).isEqualByComparingTo("12.00");
    }

    @Test
    void getSpendingByCategory_groupsByCategory() {
        Category cat = new Category("Streaming", "video", user);
        cat.setId(10L);
        Subscription s1 = new Subscription("Netflix", new BigDecimal("15.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(5), user);
        s1.setCategory(cat);
        Subscription s2 = new Subscription("Spotify", new BigDecimal("10.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(3), user);
        s2.setCategory(cat);

        when(subscriptionRepository.findByUserId(1L)).thenReturn(List.of(s1, s2));
        when(subscriptionShareRepository.findBySubscriptionOwnerId(1L)).thenReturn(List.of());

        Map<String, BigDecimal> result = financeService.getSpendingByCategory(1L);

        assertThat(result).containsKey("Streaming");
        assertThat(result.get("Streaming")).isEqualByComparingTo("25.00");
    }

    @Test
    void calculateTotalMonthlySubscriptionCost_withPercentageShare_subtractsSharedAway() {
        Subscription s = new Subscription("Netflix", new BigDecimal("30.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(5), user);
        s.setId(1L);

        SubscriptionShare share = new SubscriptionShare(s, user, new BigDecimal("50"), null);

        when(subscriptionRepository.findByUserId(1L)).thenReturn(List.of(s));
        when(subscriptionShareRepository.findBySubscriptionOwnerId(1L)).thenReturn(List.of(share));

        // total = 30.00; shared away = 30 * 50/100 = 15.00; net = 15.00
        BigDecimal result = financeService.calculateTotalMonthlySubscriptionCost(1L);

        assertThat(result).isEqualByComparingTo("15.00");
    }

}
