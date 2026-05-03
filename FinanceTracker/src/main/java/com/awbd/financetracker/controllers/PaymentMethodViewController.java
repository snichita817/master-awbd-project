package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.service.PaymentMethodService;
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
@RequestMapping("/payment-methods")
public class PaymentMethodViewController {

    private final PaymentMethodService paymentMethodService;
    private final UserService userService;

    public PaymentMethodViewController(PaymentMethodService paymentMethodService, UserService userService) {
        this.paymentMethodService = paymentMethodService;
        this.userService = userService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            model.addAttribute("paymentMethods", paymentMethodService.getPaymentMethodsByUserId(user.getId()));
            model.addAttribute("userId", user.getId());
        });
        return "payment-methods/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("paymentMethod", new PaymentMethod());
        model.addAttribute("paymentTypes", PaymentType.values());
        model.addAttribute("formAction", "/payment-methods");
        return "payment-methods/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @Valid @ModelAttribute("paymentMethod") PaymentMethod paymentMethod,
                         BindingResult result,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("paymentTypes", PaymentType.values());
            model.addAttribute("formAction", "/payment-methods");
            return "payment-methods/form";
        }
        userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
                paymentMethodService.createPaymentMethod(user.getId(), paymentMethod)
        );
        redirectAttrs.addFlashAttribute("successMessage", "Payment method added successfully.");
        return "redirect:/payment-methods";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PaymentMethod pm = paymentMethodService.getPaymentMethodById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + id));
        model.addAttribute("paymentMethod", pm);
        model.addAttribute("paymentTypes", PaymentType.values());
        model.addAttribute("formAction", "/payment-methods/" + id);
        return "payment-methods/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("paymentMethod") PaymentMethod paymentMethod,
                         BindingResult result,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("paymentTypes", PaymentType.values());
            model.addAttribute("formAction", "/payment-methods/" + id);
            return "payment-methods/form";
        }
        paymentMethodService.updatePaymentMethod(id, paymentMethod);
        redirectAttrs.addFlashAttribute("successMessage", "Payment method updated successfully.");
        return "redirect:/payment-methods";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        paymentMethodService.deletePaymentMethod(id);
        redirectAttrs.addFlashAttribute("successMessage", "Payment method deleted.");
        return "redirect:/payment-methods";
    }
}
