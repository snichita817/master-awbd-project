package com.awbd.financetracker.controllers;

import com.awbd.financetracker.client.FinanceCoreClient;
import com.awbd.financetracker.dto.SubscriptionForm;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionViewController {

    private final FinanceCoreClient financeCoreClient;
    private final UserService userService;

    public SubscriptionViewController(FinanceCoreClient financeCoreClient, UserService userService) {
        this.financeCoreClient = financeCoreClient;
        this.userService = userService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(defaultValue = "name") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       Model model) {
        var subscriptionPage = financeCoreClient.getSubscriptions(currentUser(principal).getId(), page, size, sort, dir);
        model.addAttribute("subscriptionPage", subscriptionPage);
        model.addAttribute("activePage", subscriptionPage);
        model.addAttribute("subscriptions", subscriptionPage.content());
        model.addAttribute("currentSize", size);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        model.addAttribute("reverseDir", dir.equalsIgnoreCase("asc") ? "desc" : "asc");
        model.addAttribute("filter", "mine");
        return "subscriptions/list";
    }

    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        addFormAttributes(model, new SubscriptionForm(), currentUser(principal).getId(), "/subscriptions");
        return "subscriptions/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @Valid @ModelAttribute("subscription") SubscriptionForm subscription,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long userId = currentUser(principal).getId();
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, subscription, userId, "/subscriptions");
            return "subscriptions/form";
        }

        financeCoreClient.createSubscription(userId, toUpsertDto(subscription));
        redirectAttributes.addFlashAttribute("successMessage", "Subscription created successfully.");
        return "redirect:/subscriptions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id, Model model) {
        addFormAttributes(model, toForm(financeCoreClient.getSubscription(id)), currentUser(principal).getId(), "/subscriptions/" + id);
        return "subscriptions/form";
    }

    @PostMapping("/{id}")
    public String update(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long id,
                         @Valid @ModelAttribute("subscription") SubscriptionForm subscription,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long userId = currentUser(principal).getId();
        if (bindingResult.hasErrors()) {
            subscription.setId(id);
            addFormAttributes(model, subscription, userId, "/subscriptions/" + id);
            return "subscriptions/form";
        }

        financeCoreClient.updateSubscription(id, toUpsertDto(subscription));
        redirectAttributes.addFlashAttribute("successMessage", "Subscription updated successfully.");
        return "redirect:/subscriptions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        financeCoreClient.deleteSubscription(id);
        redirectAttributes.addFlashAttribute("successMessage", "Subscription deleted.");
        return "redirect:/subscriptions";
    }

    private void addFormAttributes(Model model, SubscriptionForm subscription, Long userId, String formAction) {
        model.addAttribute("subscription", subscription);
        model.addAttribute("billingFrequencies", BillingFrequency.values());
        model.addAttribute("categories", financeCoreClient.getCategories(userId, 0, 1000, "name", "asc").content());
        model.addAttribute("paymentMethods", financeCoreClient.getPaymentMethods(userId));
        model.addAttribute("selectedCategoryId", subscription.getCategoryId());
        model.addAttribute("selectedPaymentMethodId", subscription.getPaymentMethodId());
        model.addAttribute("formAction", formAction);
    }

    private SubscriptionForm toForm(FinanceCoreClient.SubscriptionDto subscription) {
        SubscriptionForm form = new SubscriptionForm();
        form.setId(subscription.id());
        form.setName(subscription.name());
        form.setPrice(subscription.price());
        form.setBillingFrequency(subscription.billingFrequency());
        form.setRenewalDate(subscription.renewalDate());
        form.setCategoryId(subscription.category() == null ? null : subscription.category().id());
        form.setPaymentMethodId(subscription.paymentMethod() == null ? null : subscription.paymentMethod().id());
        return form;
    }

    private FinanceCoreClient.SubscriptionUpsertDto toUpsertDto(SubscriptionForm subscription) {
        return new FinanceCoreClient.SubscriptionUpsertDto(
                subscription.getName(),
                subscription.getPrice(),
                subscription.getBillingFrequency(),
                subscription.getRenewalDate(),
                subscription.getCategoryId(),
                subscription.getPaymentMethodId()
        );
    }

    private User currentUser(UserDetails principal) {
        if (principal == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));
    }
}
