package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
import com.awbd.financetracker.service.CategoryService;
import com.awbd.financetracker.service.PaymentMethodService;
import com.awbd.financetracker.service.SubscriptionService;
import com.awbd.financetracker.service.SubscriptionShareService;
import com.awbd.financetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionViewController {

    private final SubscriptionService subscriptionService;
    private final CategoryService categoryService;
    private final PaymentMethodService paymentMethodService;
    private final UserService userService;
    private final SubscriptionShareRepository subscriptionShareRepository;
    private final SubscriptionShareService subscriptionShareService;

    public SubscriptionViewController(SubscriptionService subscriptionService,
                                      CategoryService categoryService,
                                      PaymentMethodService paymentMethodService,
                                      UserService userService,
                                      SubscriptionShareRepository subscriptionShareRepository,
                                      SubscriptionShareService subscriptionShareService) {
        this.subscriptionService = subscriptionService;
        this.categoryService = categoryService;
        this.paymentMethodService = paymentMethodService;
        this.userService = userService;
        this.subscriptionShareRepository = subscriptionShareRepository;
        this.subscriptionShareService = subscriptionShareService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal,
                       @RequestParam(defaultValue = "all") String filter,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(defaultValue = "name") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sort));
            Page<Subscription> subscriptionPage = subscriptionService.getSubscriptionsByUserId(user.getId(), pageRequest);
            List<SubscriptionShare> allSharedWithMe = subscriptionShareRepository.findByIdUserId(user.getId());
            List<SubscriptionShare> sharedWithMe = allSharedWithMe;
            Set<Long> sharedIds = subscriptionShareRepository.findSharedSubscriptionIdsByOwnerId(user.getId());
            List<Subscription> visibleSubscriptions = subscriptionPage.getContent();
            Page<?> activePage = subscriptionPage;

            if ("shared".equals(filter)) {
                List<Subscription> sharedSubscriptions = subscriptionService.getSubscriptionsByUserId(user.getId()).stream()
                        .filter(subscription -> sharedIds.contains(subscription.getId()))
                        .toList();
                Page<Subscription> sharedPage = paginateList(sharedSubscriptions, pageRequest);
                visibleSubscriptions = sharedPage.getContent();
                activePage = sharedPage;
            } else if ("received".equals(filter)) {
                Page<SubscriptionShare> sharedWithMePage = paginateList(allSharedWithMe, pageRequest);
                visibleSubscriptions = List.of();
                sharedWithMe = sharedWithMePage.getContent();
                activePage = sharedWithMePage;
            }

            Map<Long, BigDecimal> sharedMonthlyAmounts = new LinkedHashMap<>();
            for (SubscriptionShare share : sharedWithMe) {
                Subscription sub = share.getSubscription();
                BigDecimal monthlyPrice = sub.getBillingFrequency() == BillingFrequency.YEARLY
                        ? sub.getPrice().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
                        : sub.getPrice();
                BigDecimal amount = null;
                if (share.getPercentageShare() != null && share.getPercentageShare().compareTo(BigDecimal.ZERO) > 0) {
                    amount = monthlyPrice.multiply(share.getPercentageShare())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                } else if (share.getFixedAmount() != null && share.getFixedAmount().compareTo(BigDecimal.ZERO) > 0) {
                    amount = share.getFixedAmount();
                }
                sharedMonthlyAmounts.put(share.getId().getSubscriptionId(), amount);
            }

            model.addAttribute("subscriptions", visibleSubscriptions);
            model.addAttribute("subscriptionPage", subscriptionPage);
            model.addAttribute("activePage", activePage);
            model.addAttribute("sharedIds", sharedIds);
            model.addAttribute("sharedWithMe", sharedWithMe);
            model.addAttribute("sharedWithMeTotal", allSharedWithMe.size());
            model.addAttribute("sharedMonthlyAmounts", sharedMonthlyAmounts);
            model.addAttribute("filter", filter);
        });
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        model.addAttribute("reverseDir", dir.equalsIgnoreCase("asc") ? "desc" : "asc");
        model.addAttribute("currentSize", size);
        return "subscriptions/list";
    }

    private <T> Page<T> paginateList(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= items.size()) {
            return new PageImpl<>(List.of(), pageable, items.size());
        }
        int end = Math.min(start + pageable.getPageSize(), items.size());
        return new PageImpl<>(items.subList(start, end), pageable, items.size());
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
        } catch (ResourceNotFoundException ex) {
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
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));
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
        } catch (ResourceNotFoundException ex) {
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

    @PostMapping("/{id}/leave-share")
    public String leaveShare(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes redirectAttrs) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
                subscriptionShareService.removeShare(id, user.getId()));
        redirectAttrs.addFlashAttribute("successMessage", "You have left the shared subscription.");
        return "redirect:/subscriptions?filter=received";
    }
}
