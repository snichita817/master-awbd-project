package com.awbd.financetracker.service;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.SubscriptionShareId;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
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
class SubscriptionShareServiceTest {

    @Mock
    private SubscriptionShareRepository subscriptionShareRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserDirectoryClient userDirectoryClient;

    private SubscriptionShareServiceImpl shareService;

    @BeforeEach
    void setUp() {
        shareService = new SubscriptionShareServiceImpl(subscriptionShareRepository, subscriptionRepository, userDirectoryClient);
    }

    @Test
    void assignSharePrefersFixedAmountOverPercentage() {
        Subscription subscription = subscription(20L);
        when(subscriptionShareRepository.existsById(new SubscriptionShareId(20L, 8L))).thenReturn(false);
        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(subscription));
        when(subscriptionShareRepository.save(org.mockito.ArgumentMatchers.any(SubscriptionShare.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionShare result = shareService.assignShare(
                20L,
                8L,
                new BigDecimal("50.00"),
                new BigDecimal("15.00")
        );

        assertThat(result.getSubscription()).isSameAs(subscription);
        assertThat(result.getId()).isEqualTo(new SubscriptionShareId(20L, 8L));
        assertThat(result.getPercentageShare()).isNull();
        assertThat(result.getFixedAmount()).isEqualByComparingTo("15.00");
        verify(userDirectoryClient).requireUser(8L);
    }

    @Test
    void assignShareRejectsDuplicateShare() {
        when(subscriptionShareRepository.existsById(new SubscriptionShareId(20L, 8L))).thenReturn(true);

        assertThatThrownBy(() -> shareService.assignShare(20L, 8L, new BigDecimal("50.00"), null))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void assignShareThrowsWhenSubscriptionIsMissing() {
        when(subscriptionShareRepository.existsById(new SubscriptionShareId(20L, 8L))).thenReturn(false);
        when(subscriptionRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareService.assignShare(20L, 8L, new BigDecimal("50.00"), null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("20");
    }

    @Test
    void updateShareSwitchesFromFixedAmountToPercentage() {
        SubscriptionShare existing = new SubscriptionShare(subscription(20L), 8L, null, new BigDecimal("15.00"));
        when(subscriptionShareRepository.findById(new SubscriptionShareId(20L, 8L))).thenReturn(Optional.of(existing));
        when(subscriptionShareRepository.save(existing)).thenReturn(existing);

        SubscriptionShare result = shareService.updateShare(20L, 8L, new BigDecimal("25.00"), null);

        assertThat(result.getPercentageShare()).isEqualByComparingTo("25.00");
        assertThat(result.getFixedAmount()).isNull();
    }

    @Test
    void removeShareDeletesTheExistingShare() {
        SubscriptionShare existing = new SubscriptionShare(subscription(20L), 8L, new BigDecimal("25.00"), null);
        when(subscriptionShareRepository.findById(new SubscriptionShareId(20L, 8L))).thenReturn(Optional.of(existing));

        shareService.removeShare(20L, 8L);

        verify(subscriptionShareRepository).delete(existing);
    }

    private static Subscription subscription(Long id) {
        Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setName("Netflix");
        subscription.setPrice(new BigDecimal("49.99"));
        subscription.setBillingFrequency(BillingFrequency.MONTHLY);
        subscription.setRenewalDate(LocalDate.now().plusDays(5));
        subscription.setOwnerUserId(7L);
        return subscription;
    }
}
