package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.SubscriptionShareId;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.DuplicateResourceException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionShareServiceTest {

    @Mock
    private SubscriptionShareRepository subscriptionShareRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private SubscriptionShareServiceImpl subscriptionShareService;

    private User user;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", new BigDecimal("3000.00"));
        user.setId(1L);

        subscription = new Subscription("Netflix", new BigDecimal("30.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(10), user);
        subscription.setId(100L);
    }

    @Test
    void assignShare_percentageShare_savesCorrectly() {
        SubscriptionShareId shareId = new SubscriptionShareId(100L, 1L);

        when(subscriptionShareRepository.existsById(shareId)).thenReturn(false);
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionShareRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionShare result = subscriptionShareService.assignShare(
                100L, 1L, new BigDecimal("50.00"), null);

        assertThat(result.getPercentageShare()).isEqualByComparingTo("50.00");
        assertThat(result.getFixedAmount()).isNull();
        verify(subscriptionShareRepository).save(any());
        // subscription has no category, so budgetService should not be called
        verifyNoInteractions(budgetService);
    }

    @Test
    void assignShare_fixedAmount_savesCorrectly() {
        SubscriptionShareId shareId = new SubscriptionShareId(100L, 1L);

        when(subscriptionShareRepository.existsById(shareId)).thenReturn(false);
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionShareRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionShare result = subscriptionShareService.assignShare(
                100L, 1L, null, new BigDecimal("10.00"));

        assertThat(result.getFixedAmount()).isEqualByComparingTo("10.00");
        assertThat(result.getPercentageShare()).isNull();
        verifyNoInteractions(budgetService);
    }

    @Test
    void assignShare_alreadyExists_throwsDuplicateResourceException() {
        SubscriptionShareId shareId = new SubscriptionShareId(100L, 1L);
        when(subscriptionShareRepository.existsById(shareId)).thenReturn(true);

        assertThatThrownBy(() ->
                subscriptionShareService.assignShare(100L, 1L, new BigDecimal("50.00"), null))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("100")
                .hasMessageContaining("1");
    }

    @Test
    void assignShare_subscriptionNotFound_throwsResourceNotFoundException() {
        SubscriptionShareId shareId = new SubscriptionShareId(999L, 1L);
        when(subscriptionShareRepository.existsById(shareId)).thenReturn(false);
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                subscriptionShareService.assignShare(999L, 1L, new BigDecimal("50.00"), null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void removeShare_nonExisting_throwsResourceNotFoundException() {
        SubscriptionShareId shareId = new SubscriptionShareId(100L, 1L);
        when(subscriptionShareRepository.existsById(shareId)).thenReturn(false);

        assertThatThrownBy(() -> subscriptionShareService.removeShare(100L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateShare_existing_updatesValues() {
        SubscriptionShareId shareId = new SubscriptionShareId(100L, 1L);
        SubscriptionShare share = new SubscriptionShare(subscription, user,
                new BigDecimal("50.00"), null);

        when(subscriptionShareRepository.findById(shareId)).thenReturn(Optional.of(share));
        when(subscriptionShareRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionShare result = subscriptionShareService.updateShare(
                100L, 1L, new BigDecimal("75.00"), new BigDecimal("5.00"));

        assertThat(result.getPercentageShare()).isEqualByComparingTo("75.00");
        assertThat(result.getFixedAmount()).isEqualByComparingTo("5.00");
    }
}
