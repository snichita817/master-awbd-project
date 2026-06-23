package com.awbd.financetracker.dto;

import com.awbd.financetracker.enums.PaymentType;

public record PaymentMethodDto(Long id, PaymentType type, String details, Long ownerUserId) {
}
