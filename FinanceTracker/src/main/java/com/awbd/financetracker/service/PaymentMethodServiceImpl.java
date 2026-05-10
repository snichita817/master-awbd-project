package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.repository.PaymentMethodRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private static final Logger log = LoggerFactory.getLogger(PaymentMethodServiceImpl.class);

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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        paymentMethod.setUser(user);
        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        log.info("PaymentMethod created: id={}, type={}, userId={}", saved.getId(), saved.getType(), userId);
        return saved;
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
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id: " + id));

        existing.setType(updatedPaymentMethod.getType());
        existing.setDetails(updatedPaymentMethod.getDetails());

        PaymentMethod saved = paymentMethodRepository.save(existing);
        log.info("PaymentMethod updated: id={}, type={}", saved.getId(), saved.getType());
        return saved;
    }

    @Override
    public void deletePaymentMethod(Long id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id: " + id));

        // Remove payment method reference from all subscriptions before deleting
        for (var subscription : paymentMethod.getSubscriptions()) {
            subscription.setPaymentMethod(null);
        }

        paymentMethodRepository.delete(paymentMethod);
        log.info("PaymentMethod deleted: id={}", id);
    }
}

