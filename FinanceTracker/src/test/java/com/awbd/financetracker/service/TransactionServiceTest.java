package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.Transaction;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", new BigDecimal("3000.00"));
        user.setId(1L);
        subscription = new Subscription("Netflix", new BigDecimal("15.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(10), user);
        subscription.setId(100L);
    }

    @Test
    void createTransaction_validSubscription_savesTransaction() {
        LocalDateTime now = LocalDateTime.now();
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionService.createTransaction(100L, now);

        assertThat(result.getAmount()).isEqualByComparingTo("15.00");
        assertThat(result.getSubscription()).isEqualTo(subscription);
        verify(transactionRepository).save(any());
    }

    @Test
    void createTransaction_nullDate_setsCurrentTime() {
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionService.createTransaction(100L, null);

        assertThat(result.getTransactionDate()).isNotNull();
    }

    @Test
    void createTransaction_subscriptionNotFound_throwsResourceNotFoundException() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(999L, LocalDateTime.now()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getTransactionById_existing_returnsOptional() {
        Transaction tx = new Transaction(new BigDecimal("15.00"), LocalDateTime.now(), subscription);
        tx.setId(1L);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));

        Optional<Transaction> result = transactionService.getTransactionById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualByComparingTo("15.00");
    }

    @Test
    void getTransactionsBySubscriptionId_existing_returnsList() {
        Transaction tx = new Transaction(new BigDecimal("15.00"), LocalDateTime.now(), subscription);
        when(subscriptionRepository.existsById(100L)).thenReturn(true);
        when(transactionRepository.findBySubscriptionId(100L)).thenReturn(List.of(tx));

        List<Transaction> result = transactionService.getTransactionsBySubscriptionId(100L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTransactionsBySubscriptionId_notFound_throwsResourceNotFoundException() {
        when(subscriptionRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.getTransactionsBySubscriptionId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getTransactionsByUserId_returnsList() {
        Transaction tx = new Transaction(new BigDecimal("15.00"), LocalDateTime.now(), subscription);
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of(tx));

        List<Transaction> result = transactionService.getTransactionsByUserId(1L);

        assertThat(result).hasSize(1);
    }
}
