package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByOwnerUserId(Long ownerUserId);

    List<PaymentMethod> findByOwnerUserIdAndType(Long ownerUserId, PaymentType type);

    Optional<PaymentMethod> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
