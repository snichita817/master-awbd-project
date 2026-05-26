package com.awbd.financetracker.service;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.PaymentMethodRepository;
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
    private final UserDirectoryClient userDirectoryClient;

    public PaymentMethodServiceImpl(PaymentMethodRepository paymentMethodRepository, UserDirectoryClient userDirectoryClient) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.userDirectoryClient = userDirectoryClient;
    }

    @Override
    public PaymentMethod createPaymentMethod(Long ownerUserId, PaymentMethod paymentMethod) {
        userDirectoryClient.requireUser(ownerUserId);
        paymentMethod.setOwnerUserId(ownerUserId);
        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        log.info("PaymentMethod created: id={}, type={}, ownerUserId={}", saved.getId(), saved.getType(), ownerUserId);
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
    public List<PaymentMethod> getPaymentMethodsByOwnerUserId(Long ownerUserId) {
        userDirectoryClient.requireUser(ownerUserId);
        return paymentMethodRepository.findByOwnerUserId(ownerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethod> getPaymentMethodsByOwnerUserIdAndType(Long ownerUserId, PaymentType type) {
        userDirectoryClient.requireUser(ownerUserId);
        return paymentMethodRepository.findByOwnerUserIdAndType(ownerUserId, type);
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
        for (Subscription subscription : paymentMethod.getSubscriptions()) {
            subscription.setPaymentMethod(null);
        }
        paymentMethodRepository.delete(paymentMethod);
        log.info("PaymentMethod deleted: id={}", id);
    }
}
