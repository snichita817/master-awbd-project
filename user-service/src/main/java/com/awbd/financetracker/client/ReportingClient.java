package com.awbd.financetracker.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ReportingClient {

    private static final Logger log = LoggerFactory.getLogger(ReportingClient.class);

    private final RestClient restClient;

    public ReportingClient(RestClient.Builder builder,
                           @Value("${services.reporting-url}") String reportingUrl) {
        this.restClient = builder.baseUrl(reportingUrl).build();
    }

    @CircuitBreaker(name = "reportingClient")
    @Retry(name = "reportingClient", fallbackMethod = "getDashboardFallback")
    public DashboardReportDto getDashboard(Long userId) {
        return restClient.get()
                .uri("/api/reports/dashboard/{userId}", userId)
                .retrieve()
                .body(DashboardReportDto.class);
    }

    DashboardReportDto getDashboardFallback(Long userId, Throwable ex) {
        log.warn("Reporting dashboard lookup failed for userId={}. Returning empty dashboard report.", userId, ex);
        return new DashboardReportDto(
                userId,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Collections.emptyList(),
                0,
                Collections.emptyMap(),
                true
        );
    }

    public record DashboardReportDto(Long userId,
                                     BigDecimal monthlyIncome,
                                     BigDecimal disposableIncome,
                                     BigDecimal totalMonthlySubscriptionCost,
                                     BigDecimal totalAnnualSubscriptionCost,
                                     List<FinanceCoreClient.SubscriptionDto> upcomingRenewals,
                                     int upcomingRenewalsCount,
                                     Map<String, BigDecimal> spendingByCategory,
                                     Boolean dataUnavailable) {
        public boolean isDataUnavailable() {
            return Boolean.TRUE.equals(dataUnavailable);
        }
    }
}
