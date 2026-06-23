package com.awbd.financetracker.service;

import com.awbd.financetracker.client.FinanceCoreClient;
import com.awbd.financetracker.dto.DashboardReportDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ReportingService {

    DashboardReportDto getDashboard(Long userId);

    BigDecimal calculateDisposableIncome(Long userId);

    BigDecimal calculateTotalMonthlySubscriptionCost(Long userId);

    List<FinanceCoreClient.SubscriptionDto> getUpcomingRenewals(Long userId);

    Map<String, BigDecimal> getSpendingByCategory(Long userId);
}
