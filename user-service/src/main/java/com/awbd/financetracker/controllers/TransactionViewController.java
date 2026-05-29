package com.awbd.financetracker.controllers;

import com.awbd.financetracker.client.FinanceCoreClient;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transactions")
public class TransactionViewController {

    private final FinanceCoreClient financeCoreClient;
    private final UserService userService;

    public TransactionViewController(FinanceCoreClient financeCoreClient, UserService userService) {
        this.financeCoreClient = financeCoreClient;
        this.userService = userService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "transactionDate") String sort,
                       @RequestParam(defaultValue = "desc") String dir,
                       Model model) {
        Long userId = currentUser(principal).getId();
        var txPage = financeCoreClient.getTransactions(userId, page, size, sort, dir);
        Map<Long, FinanceCoreClient.SubscriptionDto> subscriptionsById = financeCoreClient.getAllSubscriptions(userId)
                .stream()
                .collect(Collectors.toMap(FinanceCoreClient.SubscriptionDto::id, Function.identity()));

        model.addAttribute("transactions", txPage.content().stream()
                .map(transaction -> new TransactionView(
                        transaction.id(),
                        transaction.amount(),
                        transaction.transactionDate(),
                        subscriptionsById.get(transaction.subscriptionId())
                ))
                .toList());
        model.addAttribute("txPage", txPage);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        model.addAttribute("reverseDir", dir.equalsIgnoreCase("asc") ? "desc" : "asc");
        model.addAttribute("currentSize", size);
        return "transactions/list";
    }

    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("subscriptions", financeCoreClient.getAllSubscriptions(currentUser(principal).getId()));
        return "transactions/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @RequestParam Long subscriptionId,
                         @RequestParam(required = false) String transactionDate,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        try {
            financeCoreClient.createTransaction(subscriptionId, parseTransactionDate(transactionDate));
        } catch (RuntimeException ex) {
            model.addAttribute("subscriptions", financeCoreClient.getAllSubscriptions(currentUser(principal).getId()));
            model.addAttribute("errorMessage", "Could not record transaction: " + ex.getMessage());
            return "transactions/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Transaction recorded successfully.");
        return "redirect:/transactions";
    }

    private LocalDateTime parseTransactionDate(String transactionDate) {
        if (transactionDate == null || transactionDate.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(transactionDate);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now();
        }
    }

    private User currentUser(UserDetails principal) {
        if (principal == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));
    }

    public record TransactionView(Long id,
                                  java.math.BigDecimal amount,
                                  LocalDateTime transactionDate,
                                  FinanceCoreClient.SubscriptionDto subscription) {
    }
}
