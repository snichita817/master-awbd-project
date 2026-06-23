package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.Transaction;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(transactionRepository, subscriptionRepository);
    }

    @Test
    void createTransactionCopiesSubscriptionPriceAndDate() {
        Subscription subscription = subscription("49.99");
        LocalDateTime transactionDate = LocalDateTime.of(2026, 5, 30, 10, 15);

        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(subscription));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(20L, transactionDate);

        assertThat(result.getAmount()).isEqualByComparingTo("49.99");
        assertThat(result.getTransactionDate()).isEqualTo(transactionDate);
        assertThat(result.getSubscription()).isSameAs(subscription);
    }

    @Test
    void createTransactionUsesCurrentTimeWhenDateIsMissing() {
        Subscription subscription = subscription("49.99");
        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(subscription));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(20L, null);

        assertThat(result.getTransactionDate()).isNotNull();
    }

    @Test
    void createTransactionThrowsWhenSubscriptionDoesNotExist() {
        when(subscriptionRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(20L, LocalDateTime.now()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("20");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactionsBySubscriptionIdValidatesSubscriptionExists() {
        when(subscriptionRepository.existsById(20L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.getTransactionsBySubscriptionId(20L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("20");

        verify(transactionRepository, never()).findBySubscriptionId(20L);
    }

    private static Subscription subscription(String price) {
        Subscription subscription = new Subscription();
        subscription.setId(20L);
        subscription.setName("Netflix");
        subscription.setPrice(new BigDecimal(price));
        subscription.setBillingFrequency(BillingFrequency.MONTHLY);
        subscription.setRenewalDate(LocalDate.now().plusDays(5));
        return subscription;
    }
}
