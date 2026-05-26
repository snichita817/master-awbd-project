package com.awbd.financetracker.dto;

import java.math.BigDecimal;

public record DisposableIncomeDto(Long userId, BigDecimal disposableIncome) {
}
