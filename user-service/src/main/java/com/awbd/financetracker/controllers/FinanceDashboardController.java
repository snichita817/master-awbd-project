package com.awbd.financetracker.controllers;

import com.awbd.financetracker.client.ReportingClient;
import com.awbd.financetracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FinanceDashboardController {

    private final UserService userService;
    private final ReportingClient reportingClient;

    public FinanceDashboardController(UserService userService, ReportingClient reportingClient) {
        this.userService = userService;
        this.reportingClient = reportingClient;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (principal != null) {
            userService.getUserByEmail(principal.getUsername())
                    .ifPresent(user -> {
                        model.addAttribute("user", user);
                        ReportingClient.DashboardReportDto report = reportingClient.getDashboard(user.getId());
                        model.addAttribute("dashboardDataUnavailable", report.isDataUnavailable());
                        model.addAttribute("totalMonthlySpend", report.totalMonthlySubscriptionCost());
                        model.addAttribute("disposableIncome", report.disposableIncome());
                        model.addAttribute("upcomingRenewals", report.upcomingRenewals());
                        model.addAttribute("spendingByCategory", report.spendingByCategory());
                    });
        }
        return "finance/dashboard";
    }
}
