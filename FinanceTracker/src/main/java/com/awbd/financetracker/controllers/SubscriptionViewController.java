package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.service.CategoryService;
import com.awbd.financetracker.service.PaymentMethodService;
import com.awbd.financetracker.service.SubscriptionService;
import com.awbd.financetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionViewController {

    private final SubscriptionService subscriptionService;
    private final CategoryService categoryService;
    private final PaymentMethodService paymentMethodService;
    private final UserService userService;

    public SubscriptionViewController(SubscriptionService subscriptionService,
                                      CategoryService categoryService,
                                      PaymentMethodService paymentMethodService,
                                      UserService userService) {
        this.subscriptionService = subscriptionService;
        this.categoryService = categoryService;
        this.paymentMethodService = paymentMethodService;
        this.userService = userService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
            model.addAttribute("subscriptions", subscriptionService.getSubscriptionsByUserId(user.getId())));
        return "subscriptions/list";
    }

    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            model.addAttribute("categories", categoryService.getCategoriesByUserId(user.getId()));
            model.addAttribute("paymentMethods", paymentMethodService.getPaymentMethodsByUserId(user.getId()));
        });
        model.addAttribute("subscription", new Subscription());
        model.addAttribute("billingFrequencies", BillingFrequency.values());
        model.addAttribute("formAction", "/subscriptions");
        return "subscriptions/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long paymentMethodId,
                         @Valid @ModelAttribute("subscription") Subscription subscription,
                         BindingResult result,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        if (result.hasErrors()) {
            userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
                model.addAttribute("categories", categoryService.getCategoriesByUserId(user.getId()));
                model.addAttribute("paymentMethods", paymentMethodService.getPaymentMethodsByUserId(user.getId()));
            });
            model.addAttribute("billingFrequencies", BillingFrequency.values());
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("selectedPaymentMethodId", paymentMethodId);
            model.addAttribute("formAction", "/subscriptions");
            return "subscriptions/form";
        }
        try {
            userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
                subscriptionService.createSubscription(user.getId(), categoryId, paymentMethodId, subscription));
        } catch (IllegalArgumentException ex) {
            result.reject("error", ex.getMessage());
            userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
                model.addAttribute("categories", categoryService.getCategoriesByUserId(user.getId()));
                model.addAttribute("paymentMethods", paymentMethodService.getPaymentMethodsByUserId(user.getId()));
            });
            model.addAttribute("billingFrequencies", BillingFrequency.values());
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("selectedPaymentMethodId", paymentMethodId);
            model.addAttribute("formAction", "/subscriptions");
            return "subscriptions/form";
        }
        redirectAttrs.addFlashAttribute("successMessage", "Subscription created successfully.");
        return "redirect:/subscriptions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails principal,
                           Model model) {
        Subscription subscription = subscriptionService.getSubscriptionById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            model.addAttribute("categories", categoryService.getCategoriesByUserId(user.getId()));
            model.addAttribute("paymentMethods", paymentMethodService.getPaymentMethodsByUserId(user.getId()));
        });
        model.addAttribute("subscription", subscription);
        model.addAttribute("billingFrequencies", BillingFrequency.values());
        model.addAttribute("selectedCategoryId", subscription.getCategory() != null ? subscription.getCategory().getId() : null);
        model.addAttribute("selectedPaymentMethodId", subscription.getPaymentMethod() != null ? subscription.getPaymentMethod().getId() : null);
        model.addAttribute("formAction", "/subscriptions/" + id);
        return "subscriptions/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long paymentMethodId,
                         @Valid @ModelAttribute("subscription") Subscription subscription,
                         BindingResult result,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        if (result.hasErrors()) {
            userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
                model.addAttribute("categories", categoryService.getCategoriesByUserId(user.getId()));
                model.addAttribute("paymentMethods", paymentMethodService.getPaymentMethodsByUserId(user.getId()));
            });
            model.addAttribute("billingFrequencies", BillingFrequency.values());
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("selectedPaymentMethodId", paymentMethodId);
            model.addAttribute("formAction", "/subscriptions/" + id);
            return "subscriptions/form";
        }
        try {
            subscriptionService.updateSubscription(id, categoryId, paymentMethodId, subscription);
        } catch (IllegalArgumentException ex) {
            result.reject("error", ex.getMessage());
            userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
                model.addAttribute("categories", categoryService.getCategoriesByUserId(user.getId()));
                model.addAttribute("paymentMethods", paymentMethodService.getPaymentMethodsByUserId(user.getId()));
            });
            model.addAttribute("billingFrequencies", BillingFrequency.values());
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("selectedPaymentMethodId", paymentMethodId);
            model.addAttribute("formAction", "/subscriptions/" + id);
            return "subscriptions/form";
        }
        redirectAttrs.addFlashAttribute("successMessage", "Subscription updated successfully.");
        return "redirect:/subscriptions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        subscriptionService.deleteSubscription(id);
        redirectAttrs.addFlashAttribute("successMessage", "Subscription deleted.");
        return "redirect:/subscriptions";
    }
}
