package com.awbd.financetracker.controllers;

import com.awbd.financetracker.client.FinanceCoreClient;
import com.awbd.financetracker.dto.DashboardReportDto;
import com.awbd.financetracker.dto.DisposableIncomeDto;
import com.awbd.financetracker.dto.SpendingSummaryDto;
import com.awbd.financetracker.service.ReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardReportDto> dashboard(@PathVariable Long userId) {
        return ResponseEntity.ok(reportingService.getDashboard(userId));
    }

    @GetMapping("/disposable-income/{userId}")
    public ResponseEntity<DisposableIncomeDto> disposableIncome(@PathVariable Long userId) {
        return ResponseEntity.ok(new DisposableIncomeDto(userId, reportingService.calculateDisposableIncome(userId)));
    }

    @GetMapping("/renewals/{userId}")
    public ResponseEntity<List<FinanceCoreClient.SubscriptionDto>> renewals(@PathVariable Long userId) {
        return ResponseEntity.ok(reportingService.getUpcomingRenewals(userId));
    }

    @GetMapping("/spending-summary/{userId}")
    public ResponseEntity<SpendingSummaryDto> spendingSummary(@PathVariable Long userId) {
        BigDecimal monthlyCost = reportingService.calculateTotalMonthlySubscriptionCost(userId);
        return ResponseEntity.ok(new SpendingSummaryDto(userId, monthlyCost, monthlyCost.multiply(BigDecimal.valueOf(12))));
    }

    @GetMapping("/category-spending/{userId}")
    public ResponseEntity<Map<String, BigDecimal>> categorySpending(@PathVariable Long userId) {
        return ResponseEntity.ok(reportingService.getSpendingByCategory(userId));
    }
}
