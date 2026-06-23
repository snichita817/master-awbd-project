package com.awbd.financetracker.dto;

import com.awbd.financetracker.enums.BillingFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionUpsertDto(
        @NotBlank(message = "Subscription name is required")
        @Size(max = 100, message = "Name must be less than 100 characters")
        String name,
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,
        @NotNull(message = "Billing frequency is required")
        BillingFrequency billingFrequency,
        @NotNull(message = "Renewal date is required")
        @FutureOrPresent(message = "Renewal date must be today or in the future")
        LocalDate renewalDate,
        Long categoryId,
        Long paymentMethodId
) {
}
