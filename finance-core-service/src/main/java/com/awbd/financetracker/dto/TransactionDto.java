package com.awbd.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDto(Long id, BigDecimal amount, LocalDateTime transactionDate, Long subscriptionId) {
}
