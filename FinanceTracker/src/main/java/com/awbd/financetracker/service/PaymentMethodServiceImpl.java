package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.repository.PaymentMethodRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    public PaymentMethodServiceImpl(PaymentMethodRepository paymentMethodRepository,
                                    UserRepository userRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PaymentMethod createPaymentMethod(Long userId, PaymentMethod paymentMethod) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        paymentMethod.setUser(user);
        return paymentMethodRepository.save(paymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentMethod> getPaymentMethodById(Long id) {
        return paymentMethodRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethod> getPaymentMethodsByUserId(Long userId) {
        return paymentMethodRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethod> getPaymentMethodsByUserIdAndType(Long userId, PaymentType type) {
        return paymentMethodRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public PaymentMethod updatePaymentMethod(Long id, PaymentMethod updatedPaymentMethod) {
        PaymentMethod existing = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found with id: " + id));

        existing.setType(updatedPaymentMethod.getType());
        existing.setDetails(updatedPaymentMethod.getDetails());

        return paymentMethodRepository.save(existing);
    }

    @Override
    public void deletePaymentMethod(Long id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found with id: " + id));

        // Remove payment method reference from all subscriptions before deleting
        for (var subscription : paymentMethod.getSubscriptions()) {
            subscription.setPaymentMethod(null);
        }

        paymentMethodRepository.delete(paymentMethod);
    }
}

