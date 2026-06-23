package com.awbd.financetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetUpdateDto(
        @NotNull(message = "Maximum limit is required")
        @DecimalMin(value = "0.01", message = "Maximum limit must be greater than 0")
        BigDecimal maxLimit
) {
}
