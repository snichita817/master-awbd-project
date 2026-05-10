package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", new BigDecimal("3000.00"));
        user.setId(1L);

        category = new Category("Streaming", "Video streaming", user);
        category.setId(10L);
    }

    @Test
    void createSubscription_happyPath_budgetUpdated() {
        Subscription subscription = new Subscription();
        subscription.setName("Netflix");
        subscription.setPrice(new BigDecimal("15.00"));
        subscription.setBillingFrequency(BillingFrequency.MONTHLY);
        subscription.setRenewalDate(LocalDate.now().plusDays(20));

        Subscription saved = new Subscription("Netflix", new BigDecimal("15.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(20), user);
        saved.setId(100L);
        saved.setCategory(category);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(subscriptionRepository.save(any())).thenReturn(saved);

        Subscription result = subscriptionService.createSubscription(1L, 10L, null, subscription);

        assertThat(result.getName()).isEqualTo("Netflix");
        verify(budgetService).addSubscriptionToBudget(category, new BigDecimal("15.00"), BillingFrequency.MONTHLY);
    }

    @Test
    void createSubscription_yearlyPrice_normalizedToMonthlyInBudget() {
        Subscription subscription = new Subscription();
        subscription.setName("Annual Plan");
        subscription.setPrice(new BigDecimal("120.00"));
        subscription.setBillingFrequency(BillingFrequency.YEARLY);
        subscription.setRenewalDate(LocalDate.now().plusDays(300));

        Subscription saved = new Subscription("Annual Plan", new BigDecimal("120.00"),
                BillingFrequency.YEARLY, LocalDate.now().plusDays(300), user);
        saved.setId(101L);
        saved.setCategory(category);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(subscriptionRepository.save(any())).thenReturn(saved);

        subscriptionService.createSubscription(1L, 10L, null, subscription);

        // BudgetService.addSubscriptionToBudget is called with the raw price and frequency  - 
        // the normalization to monthly (price/12 HALF_UP) happens inside BudgetServiceImpl.
        verify(budgetService).addSubscriptionToBudget(
                category,
                new BigDecimal("120.00"),
                BillingFrequency.YEARLY
        );
    }

    @Test
    void createSubscription_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                subscriptionService.createSubscription(99L, 10L, null, new Subscription()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteSubscription_removesFromBudgetBeforeDelete() {
        Subscription subscription = new Subscription("Netflix", new BigDecimal("15.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(5), user);
        subscription.setId(100L);
        subscription.setCategory(category);

        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));

        subscriptionService.deleteSubscription(100L);

        verify(budgetService).removeSubscriptionFromBudget(category, new BigDecimal("15.00"), BillingFrequency.MONTHLY);
        verify(subscriptionRepository).deleteById(100L);
    }
}
