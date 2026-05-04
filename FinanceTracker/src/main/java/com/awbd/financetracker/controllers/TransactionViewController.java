package com.awbd.financetracker.controllers;

import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.service.SubscriptionService;
import com.awbd.financetracker.service.TransactionService;
import com.awbd.financetracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/transactions")
public class TransactionViewController {

    private final TransactionService transactionService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;

    public TransactionViewController(TransactionService transactionService,
                                     SubscriptionService subscriptionService,
                                     UserService userService) {
        this.transactionService = transactionService;
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
            model.addAttribute("transactions", transactionService.getTransactionsByUserId(user.getId())));
        return "transactions/list";
    }

    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
            model.addAttribute("subscriptions", subscriptionService.getSubscriptionsByUserId(user.getId())));
        return "transactions/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @RequestParam Long subscriptionId,
                         @RequestParam(required = false) String transactionDate,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        LocalDateTime dateTime;
        if (transactionDate == null || transactionDate.isBlank()) {
            dateTime = LocalDateTime.now();
        } else {
            try {
                dateTime = LocalDateTime.parse(transactionDate);
            } catch (DateTimeParseException e) {
                dateTime = LocalDateTime.now();
            }
        }
        try {
            transactionService.createTransaction(subscriptionId, dateTime);
        } catch (ResourceNotFoundException ex) {
            userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
                model.addAttribute("subscriptions", subscriptionService.getSubscriptionsByUserId(user.getId())));
            model.addAttribute("errorMessage", ex.getMessage());
            return "transactions/form";
        }
        redirectAttrs.addFlashAttribute("successMessage", "Transaction recorded successfully.");
        return "redirect:/transactions";
    }
}
