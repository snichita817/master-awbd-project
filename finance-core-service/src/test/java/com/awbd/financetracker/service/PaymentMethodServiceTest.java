package com.awbd.financetracker.service;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.PaymentMethodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private UserDirectoryClient userDirectoryClient;

    private PaymentMethodServiceImpl paymentMethodService;

    @BeforeEach
    void setUp() {
        paymentMethodService = new PaymentMethodServiceImpl(paymentMethodRepository, userDirectoryClient);
    }

    @Test
    void createPaymentMethodAssignsOwnerAfterValidatingUser() {
        PaymentMethod input = paymentMethod(null, PaymentType.CREDIT_CARD, "Visa");
        PaymentMethod saved = paymentMethod(10L, PaymentType.CREDIT_CARD, "Visa");

        when(paymentMethodRepository.save(input)).thenReturn(saved);

        PaymentMethod result = paymentMethodService.createPaymentMethod(7L, input);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(input.getOwnerUserId()).isEqualTo(7L);
        verify(userDirectoryClient).requireUser(7L);
    }

    @Test
    void getPaymentMethodsByOwnerAndTypeValidatesUserAndUsesFilteredRepositoryQuery() {
        PaymentMethod card = paymentMethod(10L, PaymentType.CREDIT_CARD, "Visa");
        when(paymentMethodRepository.findByOwnerUserIdAndType(7L, PaymentType.CREDIT_CARD)).thenReturn(List.of(card));

        List<PaymentMethod> result = paymentMethodService.getPaymentMethodsByOwnerUserIdAndType(7L, PaymentType.CREDIT_CARD);

        assertThat(result).containsExactly(card);
        verify(userDirectoryClient).requireUser(7L);
    }

    @Test
    void updatePaymentMethodChangesOnlyEditableFields() {
        PaymentMethod existing = paymentMethod(10L, PaymentType.CREDIT_CARD, "Visa");
        existing.setOwnerUserId(7L);
        PaymentMethod update = paymentMethod(null, PaymentType.PAYPAL, "alex@example.com");

        when(paymentMethodRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(paymentMethodRepository.save(existing)).thenReturn(existing);

        PaymentMethod result = paymentMethodService.updatePaymentMethod(10L, update);

        assertThat(result.getType()).isEqualTo(PaymentType.PAYPAL);
        assertThat(result.getDetails()).isEqualTo("alex@example.com");
        assertThat(result.getOwnerUserId()).isEqualTo(7L);
    }

    @Test
    void deletePaymentMethodDetachesSubscriptionsBeforeDelete() {
        PaymentMethod paymentMethod = paymentMethod(10L, PaymentType.CREDIT_CARD, "Visa");
        Subscription subscription = subscription(paymentMethod);
        paymentMethod.setSubscriptions(List.of(subscription));

        when(paymentMethodRepository.findById(10L)).thenReturn(Optional.of(paymentMethod));

        paymentMethodService.deletePaymentMethod(10L);

        assertThat(subscription.getPaymentMethod()).isNull();
        verify(paymentMethodRepository).delete(paymentMethod);
    }

    @Test
    void deletePaymentMethodThrowsWhenMissing() {
        when(paymentMethodRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.deletePaymentMethod(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("10");
    }

    private static PaymentMethod paymentMethod(Long id, PaymentType type, String details) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(id);
        paymentMethod.setType(type);
        paymentMethod.setDetails(details);
        return paymentMethod;
    }

    private static Subscription subscription(PaymentMethod paymentMethod) {
        Subscription subscription = new Subscription();
        subscription.setId(20L);
        subscription.setName("Netflix");
        subscription.setPrice(new BigDecimal("49.99"));
        subscription.setBillingFrequency(BillingFrequency.MONTHLY);
        subscription.setRenewalDate(LocalDate.now().plusDays(5));
        subscription.setPaymentMethod(paymentMethod);
        return subscription;
    }
}
