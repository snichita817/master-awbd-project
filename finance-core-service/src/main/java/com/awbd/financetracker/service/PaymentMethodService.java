package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.enums.PaymentType;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodService {

    PaymentMethod createPaymentMethod(Long ownerUserId, PaymentMethod paymentMethod);

    Optional<PaymentMethod> getPaymentMethodById(Long id);

    List<PaymentMethod> getAllPaymentMethods();

    List<PaymentMethod> getPaymentMethodsByOwnerUserId(Long ownerUserId);

    List<PaymentMethod> getPaymentMethodsByOwnerUserIdAndType(Long ownerUserId, PaymentType type);

    PaymentMethod updatePaymentMethod(Long id, PaymentMethod paymentMethod);

    void deletePaymentMethod(Long id);
}
