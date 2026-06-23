package com.awbd.financetracker.dto;

import com.awbd.financetracker.client.FinanceCoreClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardReportDto(Long userId,
                                 BigDecimal monthlyIncome,
                                 BigDecimal disposableIncome,
                                 BigDecimal totalMonthlySubscriptionCost,
                                 BigDecimal totalAnnualSubscriptionCost,
                                 List<FinanceCoreClient.SubscriptionDto> upcomingRenewals,
                                 int upcomingRenewalsCount,
                                 Map<String, BigDecimal> spendingByCategory) {
}
