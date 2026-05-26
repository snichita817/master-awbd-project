package com.awbd.financetracker.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ReportingClient {

    private final RestClient restClient;

    public ReportingClient(RestClient.Builder builder,
                           @Value("${services.reporting-url}") String reportingUrl) {
        this.restClient = builder.baseUrl(reportingUrl).build();
    }

    public DashboardReportDto getDashboard(Long userId) {
        return restClient.get()
                .uri("/api/reports/dashboard/{userId}", userId)
                .retrieve()
                .body(DashboardReportDto.class);
    }

    public record DashboardReportDto(Long userId,
                                     BigDecimal monthlyIncome,
                                     BigDecimal disposableIncome,
                                     BigDecimal totalMonthlySubscriptionCost,
                                     BigDecimal totalAnnualSubscriptionCost,
                                     List<FinanceCoreClient.SubscriptionDto> upcomingRenewals,
                                     int upcomingRenewalsCount,
                                     Map<String, BigDecimal> spendingByCategory) {
    }
}
