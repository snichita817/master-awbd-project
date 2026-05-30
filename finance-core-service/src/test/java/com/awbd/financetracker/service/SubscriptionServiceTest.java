package com.awbd.financetracker.service;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.PaymentMethodRepository;
import com.awbd.financetracker.repository.SubscriptionRepository;
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
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetService budgetService;

    @Mock
    private UserDirectoryClient userDirectoryClient;

    private SubscriptionServiceImpl subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionServiceImpl(
                subscriptionRepository,
                paymentMethodRepository,
                categoryRepository,
                budgetService,
                userDirectoryClient
        );
    }

    @Test
    void createSubscriptionLinksOwnerCategoryPaymentMethodAndUpdatesBudget() {
        Category category = category(3L, 7L);
        PaymentMethod paymentMethod = paymentMethod(4L, 7L);
        Subscription input = subscription("Netflix", "49.99", BillingFrequency.MONTHLY);
        Subscription saved = subscription("Netflix", "49.99", BillingFrequency.MONTHLY);
        saved.setId(20L);
        saved.setOwnerUserId(7L);
        saved.setCategory(category);
        saved.setPaymentMethod(paymentMethod);

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(paymentMethodRepository.findByIdAndOwnerUserId(4L, 7L)).thenReturn(Optional.of(paymentMethod));
        when(subscriptionRepository.save(input)).thenReturn(saved);

        Subscription result = subscriptionService.createSubscription(7L, 3L, 4L, input);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(input.getOwnerUserId()).isEqualTo(7L);
        assertThat(input.getCategory()).isSameAs(category);
        assertThat(input.getPaymentMethod()).isSameAs(paymentMethod);
        verify(userDirectoryClient).requireUser(7L);
        verify(budgetService).addSubscriptionToBudget(category, new BigDecimal("49.99"), BillingFrequency.MONTHLY);
    }

    @Test
    void createSubscriptionRejectsCategoryThatBelongsToAnotherOwner() {
        Category category = category(3L, 8L);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> subscriptionService.createSubscription(
                7L,
                3L,
                null,
                subscription("Netflix", "49.99", BillingFrequency.MONTHLY)
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void updateSubscriptionRemovesOldBudgetAmountAndAddsNewAmount() {
        Category oldCategory = category(3L, 7L);
        Category newCategory = category(5L, 7L);
        Subscription existing = subscription("Old", "30.00", BillingFrequency.MONTHLY);
        existing.setId(20L);
        existing.setOwnerUserId(7L);
        existing.setCategory(oldCategory);

        Subscription update = subscription("New", "120.00", BillingFrequency.YEARLY);

        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(newCategory));
        when(subscriptionRepository.save(existing)).thenReturn(existing);

        Subscription result = subscriptionService.updateSubscription(20L, 5L, null, update);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getCategory()).isSameAs(newCategory);
        verify(budgetService).removeSubscriptionFromBudget(oldCategory, new BigDecimal("30.00"), BillingFrequency.MONTHLY);
        verify(budgetService).addSubscriptionToBudget(newCategory, new BigDecimal("120.00"), BillingFrequency.YEARLY);
    }

    @Test
    void deleteSubscriptionRemovesBudgetAmountBeforeDeleting() {
        Category category = category(3L, 7L);
        Subscription subscription = subscription("Netflix", "49.99", BillingFrequency.MONTHLY);
        subscription.setId(20L);
        subscription.setCategory(category);

        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(subscription));

        subscriptionService.deleteSubscription(20L);

        verify(budgetService).removeSubscriptionFromBudget(category, new BigDecimal("49.99"), BillingFrequency.MONTHLY);
        verify(subscriptionRepository).delete(subscription);
    }

    private static Category category(Long id, Long ownerUserId) {
        Category category = new Category();
        category.setId(id);
        category.setName("Category " + id);
        category.setOwnerUserId(ownerUserId);
        return category;
    }

    private static PaymentMethod paymentMethod(Long id, Long ownerUserId) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(id);
        paymentMethod.setType(PaymentType.CREDIT_CARD);
        paymentMethod.setDetails("Visa");
        paymentMethod.setOwnerUserId(ownerUserId);
        return paymentMethod;
    }

    private static Subscription subscription(String name, String price, BillingFrequency billingFrequency) {
        Subscription subscription = new Subscription();
        subscription.setName(name);
        subscription.setPrice(new BigDecimal(price));
        subscription.setBillingFrequency(billingFrequency);
        subscription.setRenewalDate(LocalDate.now().plusDays(5));
        return subscription;
    }
}
