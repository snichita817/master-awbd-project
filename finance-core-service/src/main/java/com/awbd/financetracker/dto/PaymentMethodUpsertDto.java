package com.awbd.financetracker.dto;

import com.awbd.financetracker.enums.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentMethodUpsertDto(
        @NotNull(message = "Payment type is required")
        PaymentType type,
        @NotBlank(message = "Payment details are required")
        @Size(max = 255, message = "Details must be less than 255 characters")
        String details
) {
}
