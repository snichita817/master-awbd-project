package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.service.SubscriptionService;
import com.awbd.financetracker.service.SubscriptionShareService;
import com.awbd.financetracker.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionShareViewController {

    private final SubscriptionShareService subscriptionShareService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;

    public SubscriptionShareViewController(SubscriptionShareService subscriptionShareService,
                                           SubscriptionService subscriptionService,
                                           UserService userService) {
        this.subscriptionShareService = subscriptionShareService;
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // GET /{id}/shares — list current shares + add-share form
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/shares")
    public String sharesPage(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails principal,
                             Model model) {
        Subscription subscription = subscriptionService.getSubscriptionById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        User currentUser = userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (!subscription.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not own this subscription");
        }

        List<SubscriptionShare> shares = subscriptionShareService.getSharesBySubscription(id);
        model.addAttribute("subscription", subscription);
        model.addAttribute("shares", shares);
        model.addAttribute("subscriptionPrice", subscription.getPrice());
        model.addAttribute("subscriptionBillingFrequency", subscription.getBillingFrequency());
        model.addAttribute("monthlyAmounts", buildMonthlyAmountsForShares(subscription, shares));
        return "subscriptions/shares";
    }

    // -------------------------------------------------------------------------
    // POST /{id}/shares — add a share
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/shares")
    public String addShare(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails principal,
                           @RequestParam String email,
                           @RequestParam(required = false) BigDecimal percentageShare,
                           @RequestParam(required = false) BigDecimal fixedAmount,
                           RedirectAttributes redirectAttrs,
                           Model model) {

        Subscription subscription = subscriptionService.getSubscriptionById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        User currentUser = userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (!subscription.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not own this subscription");
        }

        // --- server-side validation ---

        if (email == null || email.isBlank()) {
            return reRenderShares(id, subscription, model, "Email is required");
        }

        User targetUser = userService.getUserByEmail(email).orElse(null);
        if (targetUser == null) {
            return reRenderShares(id, subscription, model, "No user found with that email");
        }

        if (targetUser.getId().equals(currentUser.getId())) {
            return reRenderShares(id, subscription, model, "You cannot share a subscription with yourself");
        }

        boolean hasPercentage = percentageShare != null && percentageShare.compareTo(BigDecimal.ZERO) > 0;
        boolean hasFixed = fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0;
        if (!hasPercentage && !hasFixed) {
            return reRenderShares(id, subscription, model, "Provide either a percentage share or a fixed amount");
        }

        try {
            subscriptionShareService.assignShare(id, targetUser.getId(), percentageShare, fixedAmount);
        } catch (DuplicateResourceException ex) {
            return reRenderShares(id, subscription, model, "That user already has a share on this subscription");
        }

        redirectAttrs.addFlashAttribute("successMessage", "Share added for " + targetUser.getEmail());
        return "redirect:/subscriptions/" + id + "/shares";
    }

    // -------------------------------------------------------------------------
    // POST /{id}/shares/{userId}/delete — remove a share
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/shares/{userId}/delete")
    public String removeShare(@PathVariable Long id,
                              @PathVariable Long userId,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttrs) {

        Subscription subscription = subscriptionService.getSubscriptionById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        User currentUser = userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (!subscription.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not own this subscription");
        }

        subscriptionShareService.removeShare(id, userId);
        redirectAttrs.addFlashAttribute("successMessage", "Share removed.");
        return "redirect:/subscriptions/" + id + "/shares";
    }

    // -------------------------------------------------------------------------
    // Helper: re-render the shares page with an inline error message
    // -------------------------------------------------------------------------

    private String reRenderShares(Long id, Subscription subscription, Model model, String errorMessage) {
        List<SubscriptionShare> shares = subscriptionShareService.getSharesBySubscription(id);
        model.addAttribute("subscription", subscription);
        model.addAttribute("shares", shares);
        model.addAttribute("subscriptionPrice", subscription.getPrice());
        model.addAttribute("subscriptionBillingFrequency", subscription.getBillingFrequency());
        model.addAttribute("monthlyAmounts", buildMonthlyAmountsForShares(subscription, shares));
        model.addAttribute("errorMessage", errorMessage);
        return "subscriptions/shares";
    }

    private Map<Long, BigDecimal> buildMonthlyAmountsForShares(Subscription subscription,
                                                                List<SubscriptionShare> shares) {
        Map<Long, BigDecimal> map = new LinkedHashMap<>();
        for (SubscriptionShare share : shares) {
            map.put(share.getId().getUserId(),
                    computeMonthlyAmount(subscription, share.getPercentageShare(), share.getFixedAmount()));
        }
        return map;
    }

    private BigDecimal computeMonthlyAmount(Subscription subscription,
                                             BigDecimal percentageShare,
                                             BigDecimal fixedAmount) {
        if (percentageShare != null && percentageShare.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal monthlyPrice = subscription.getBillingFrequency() == BillingFrequency.YEARLY
                    ? subscription.getPrice().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
                    : subscription.getPrice();
            return monthlyPrice.multiply(percentageShare)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        if (fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0) {
            return fixedAmount;
        }
        return null;
    }
}
