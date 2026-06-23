package com.awbd.financetracker.dto;

import java.math.BigDecimal;

public record BudgetDto(Long id, BigDecimal maxLimit, BigDecimal currentSpending, CategoryDto category) {
}
