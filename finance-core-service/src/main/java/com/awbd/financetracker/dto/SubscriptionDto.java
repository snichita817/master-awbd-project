package com.awbd.financetracker.dto;

import com.awbd.financetracker.enums.BillingFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionDto(Long id,
                              String name,
                              BigDecimal price,
                              BillingFrequency billingFrequency,
                              LocalDate renewalDate,
                              Long ownerUserId,
                              CategoryDto category,
                              PaymentMethodDto paymentMethod) {
}
