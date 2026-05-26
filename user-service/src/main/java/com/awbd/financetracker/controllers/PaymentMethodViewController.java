package com.awbd.financetracker.controllers;

import com.awbd.financetracker.client.FinanceCoreClient;
import com.awbd.financetracker.dto.PaymentMethodForm;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment-methods")
public class PaymentMethodViewController {

    private final FinanceCoreClient financeCoreClient;
    private final UserService userService;

    public PaymentMethodViewController(FinanceCoreClient financeCoreClient, UserService userService) {
        this.financeCoreClient = financeCoreClient;
        this.userService = userService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("paymentMethods", financeCoreClient.getPaymentMethods(currentUser(principal).getId()));
        return "payment-methods/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addFormAttributes(model, new PaymentMethodForm(), "/payment-methods");
        return "payment-methods/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @Valid @ModelAttribute("paymentMethod") PaymentMethodForm paymentMethod,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, paymentMethod, "/payment-methods");
            return "payment-methods/form";
        }

        financeCoreClient.createPaymentMethod(
                currentUser(principal).getId(),
                new FinanceCoreClient.PaymentMethodUpsertDto(paymentMethod.getType(), paymentMethod.getDetails())
        );
        redirectAttributes.addFlashAttribute("successMessage", "Payment method created successfully.");
        return "redirect:/payment-methods";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        FinanceCoreClient.PaymentMethodDto paymentMethod = financeCoreClient.getPaymentMethod(id);
        addFormAttributes(
                model,
                new PaymentMethodForm(paymentMethod.id(), paymentMethod.type(), paymentMethod.details()),
                "/payment-methods/" + id
        );
        return "payment-methods/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("paymentMethod") PaymentMethodForm paymentMethod,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            paymentMethod.setId(id);
            addFormAttributes(model, paymentMethod, "/payment-methods/" + id);
            return "payment-methods/form";
        }

        financeCoreClient.updatePaymentMethod(
                id,
                new FinanceCoreClient.PaymentMethodUpsertDto(paymentMethod.getType(), paymentMethod.getDetails())
        );
        redirectAttributes.addFlashAttribute("successMessage", "Payment method updated successfully.");
        return "redirect:/payment-methods";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        financeCoreClient.deletePaymentMethod(id);
        redirectAttributes.addFlashAttribute("successMessage", "Payment method deleted.");
        return "redirect:/payment-methods";
    }

    private void addFormAttributes(Model model, PaymentMethodForm paymentMethod, String formAction) {
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentTypes", PaymentType.values());
        model.addAttribute("formAction", formAction);
    }

    private User currentUser(UserDetails principal) {
        if (principal == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));
    }
}
