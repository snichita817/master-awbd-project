package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByUserId(Long userId);

    Optional<PaymentMethod> findByIdAndUserId(Long id, Long userId);

    List<PaymentMethod> findByUserIdAndType(Long userId, PaymentType type);
}

