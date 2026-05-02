package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@Tag(name = "Finance", description = "Financial insights and dashboard; View spending, income, and upcoming renewals")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Operation(summary = "Get financial dashboard", description = "Returns a complete financial overview including disposable income, subscription costs, and upcoming renewals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Long userId) {
        BigDecimal disposableIncome = financeService.calculateDisposableIncome(userId);
        BigDecimal totalMonthlySubscriptionCost = financeService.calculateTotalMonthlySubscriptionCost(userId);
        List<Subscription> upcomingRenewals = financeService.getUpcomingRenewals(userId);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("userId", userId);
        dashboard.put("disposableIncome", disposableIncome);
        dashboard.put("totalMonthlySubscriptionCost", totalMonthlySubscriptionCost);
        dashboard.put("totalAnnualSubscriptionCost", totalMonthlySubscriptionCost.multiply(BigDecimal.valueOf(12)));
        dashboard.put("upcomingRenewals", upcomingRenewals);
        dashboard.put("upcomingRenewalsCount", upcomingRenewals.size());

        return ResponseEntity.ok(dashboard);
    }

    @Operation(summary = "Calculate disposable income", description = "Returns how much money the user has left after all subscription costs (monthly income minus subscription expenses)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disposable income calculated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/disposable-income/{userId}")
    public ResponseEntity<Map<String, Object>> getDisposableIncome(@PathVariable Long userId) {
        BigDecimal disposableIncome = financeService.calculateDisposableIncome(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("disposableIncome", disposableIncome);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get upcoming renewals", description = "Get upcoming subscription renewals for a user, that expire within the next 7 days")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upcoming renewals retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/renewals/{userId}")
    public ResponseEntity<List<Subscription>> getUpcomingRenewals(@PathVariable Long userId) {
        List<Subscription> upcomingRenewals = financeService.getUpcomingRenewals(userId);
        return ResponseEntity.ok(upcomingRenewals);
    }

    @Operation(summary = "Get spending summary", description = "Returns total monthly and annual subscription costs for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Spending summary retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/spending-summary/{userId}")
    public ResponseEntity<Map<String, Object>> getSpendingSummary(@PathVariable Long userId) {
        BigDecimal totalMonthlySubscriptionCost = financeService.calculateTotalMonthlySubscriptionCost(userId);
        BigDecimal totalAnnualSubscriptionCost = totalMonthlySubscriptionCost.multiply(BigDecimal.valueOf(12));

        Map<String, Object> summary = new HashMap<>();
        summary.put("userId", userId);
        summary.put("monthlySubscriptionCost", totalMonthlySubscriptionCost);
        summary.put("annualSubscriptionCost", totalAnnualSubscriptionCost);

        return ResponseEntity.ok(summary);
    }
}

