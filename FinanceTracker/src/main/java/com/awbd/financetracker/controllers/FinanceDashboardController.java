package com.awbd.financetracker.controllers;

import com.awbd.financetracker.service.FinanceService;
import com.awbd.financetracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FinanceDashboardController {

    private final FinanceService financeService;
    private final UserService userService;

    public FinanceDashboardController(FinanceService financeService, UserService userService) {
        this.financeService = financeService;
        this.userService = userService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            model.addAttribute("user", user);
            model.addAttribute("disposableIncome", financeService.calculateDisposableIncome(user.getId()));
            model.addAttribute("totalMonthlySpend", financeService.calculateTotalMonthlySubscriptionCost(user.getId()));
            model.addAttribute("upcomingRenewals", financeService.getUpcomingRenewals(user.getId()));
            model.addAttribute("spendingByCategory", financeService.getSpendingByCategory(user.getId()));
        });
        return "finance/dashboard";
    }
}
