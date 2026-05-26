package com.awbd.financetracker.dto;

import java.math.BigDecimal;

public record SpendingSummaryDto(Long userId,
                                 BigDecimal monthlySubscriptionCost,
                                 BigDecimal annualSubscriptionCost) {
}
