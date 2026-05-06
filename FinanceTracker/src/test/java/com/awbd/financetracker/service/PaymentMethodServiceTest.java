package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.PaymentMethodRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    private User user;
    private PaymentMethod paymentMethod;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", new BigDecimal("3000.00"));
        user.setId(1L);
        paymentMethod = new PaymentMethod(PaymentType.CREDIT_CARD, "Visa ending 1234", user);
        paymentMethod.setId(10L);
    }

    @Test
    void createPaymentMethod_validUser_savesAndReturns() {
        PaymentMethod pm = new PaymentMethod(PaymentType.CREDIT_CARD, "Visa", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentMethodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentMethod result = paymentMethodService.createPaymentMethod(1L, pm);

        assertThat(result.getUser()).isEqualTo(user);
        verify(paymentMethodRepository).save(pm);
    }

    @Test
    void createPaymentMethod_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.createPaymentMethod(99L, new PaymentMethod()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getPaymentMethodById_existing_returnsOptional() {
        when(paymentMethodRepository.findById(10L)).thenReturn(Optional.of(paymentMethod));

        Optional<PaymentMethod> result = paymentMethodService.getPaymentMethodById(10L);

        assertThat(result).isPresent().contains(paymentMethod);
    }

    @Test
    void getPaymentMethodsByUserId_returnsList() {
        when(paymentMethodRepository.findByUserId(1L)).thenReturn(List.of(paymentMethod));

        List<PaymentMethod> result = paymentMethodService.getPaymentMethodsByUserId(1L);

        assertThat(result).hasSize(1).contains(paymentMethod);
    }

    @Test
    void updatePaymentMethod_existing_updatesFields() {
        PaymentMethod update = new PaymentMethod(PaymentType.PAYPAL, "paypal@example.com", null);
        when(paymentMethodRepository.findById(10L)).thenReturn(Optional.of(paymentMethod));
        when(paymentMethodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentMethod result = paymentMethodService.updatePaymentMethod(10L, update);

        assertThat(result.getType()).isEqualTo(PaymentType.PAYPAL);
        assertThat(result.getDetails()).isEqualTo("paypal@example.com");
    }

    @Test
    void updatePaymentMethod_notFound_throwsResourceNotFoundException() {
        when(paymentMethodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.updatePaymentMethod(99L, new PaymentMethod()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deletePaymentMethod_existing_deletesSuccessfully() {
        when(paymentMethodRepository.findById(10L)).thenReturn(Optional.of(paymentMethod));

        paymentMethodService.deletePaymentMethod(10L);

        verify(paymentMethodRepository).delete(paymentMethod);
    }

    @Test
    void deletePaymentMethod_notFound_throwsResourceNotFoundException() {
        when(paymentMethodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.deletePaymentMethod(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
