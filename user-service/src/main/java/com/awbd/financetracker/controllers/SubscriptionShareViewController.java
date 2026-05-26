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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionShareViewController {

    private final FinanceCoreClient financeCoreClient;
    private final UserService userService;

    public SubscriptionShareViewController(FinanceCoreClient financeCoreClient, UserService userService) {
        this.financeCoreClient = financeCoreClient;
        this.userService = userService;
    }

    @GetMapping("/{id}/shares")
    public String shares(@PathVariable Long id, Model model) {
        var subscription = financeCoreClient.getSubscription(id);
        var shares = financeCoreClient.getSharesBySubscription(id);
        var requests = financeCoreClient.getShareRequestsForSubscription(id);

        model.addAttribute("subscription", subscription);
        model.addAttribute("shares", shares);
        model.addAttribute("shareRequests", requests);
        model.addAttribute("monthlyAmounts", shares.stream()
                .collect(Collectors.toMap(FinanceCoreClient.SubscriptionShareDto::participantUserId, this::monthlyAmount)));
        model.addAttribute("requestMonthlyAmounts", requests.stream()
                .collect(Collectors.toMap(FinanceCoreClient.SubscriptionShareRequestDto::id, this::monthlyAmount)));
        return "subscriptions/shares";
    }

    @PostMapping("/{id}/shares")
    public String createShareRequest(@AuthenticationPrincipal UserDetails principal,
                                     @PathVariable Long id,
                                     @RequestParam("email") String recipientEmail,
                                     @RequestParam(required = false) BigDecimal percentageShare,
                                     @RequestParam(required = false) BigDecimal fixedAmount,
                                     RedirectAttributes redirectAttributes) {
        financeCoreClient.createShareRequest(
                id,
                currentUser(principal).getId(),
                new FinanceCoreClient.ShareRequestCreateDto(recipientEmail, percentageShare, fixedAmount)
        );
        redirectAttributes.addFlashAttribute("successMessage", "Share request sent.");
        return "redirect:/subscriptions/" + id + "/shares";
    }

    @GetMapping("/share-requests")
    public String shareRequests(@AuthenticationPrincipal UserDetails principal, Model model) {
        var requests = financeCoreClient.getShareRequestsForRecipient(currentUser(principal).getId());
        model.addAttribute("requests", requests);
        model.addAttribute("requestMonthlyAmounts", requests.stream()
                .collect(Collectors.toMap(FinanceCoreClient.SubscriptionShareRequestDto::id, this::monthlyAmount)));
        return "subscriptions/share-requests";
    }

    @PostMapping("/share-requests/{requestId}/accept")
    public String accept(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long requestId,
                         RedirectAttributes redirectAttributes) {
        financeCoreClient.acceptShareRequest(requestId, currentUser(principal).getId());
        redirectAttributes.addFlashAttribute("successMessage", "Share request accepted.");
        return "redirect:/subscriptions/share-requests";
    }

    @PostMapping("/share-requests/{requestId}/decline")
    public String decline(@AuthenticationPrincipal UserDetails principal,
                          @PathVariable Long requestId,
                          RedirectAttributes redirectAttributes) {
        financeCoreClient.declineShareRequest(requestId, currentUser(principal).getId());
        redirectAttributes.addFlashAttribute("successMessage", "Share request declined.");
        return "redirect:/subscriptions/share-requests";
    }

    @PostMapping("/share-requests/{requestId}/revoke")
    public String revoke(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long requestId,
                         @RequestParam(required = false) Long subscriptionId,
                         RedirectAttributes redirectAttributes) {
        financeCoreClient.revokeShareRequest(requestId, currentUser(principal).getId());
        redirectAttributes.addFlashAttribute("successMessage", "Share request revoked.");
        return subscriptionId == null ? "redirect:/subscriptions" : "redirect:/subscriptions/" + subscriptionId + "/shares";
    }

    @PostMapping("/{id}/shares/{userId}/delete")
    public String removeShare(@PathVariable Long id,
                              @PathVariable Long userId,
                              RedirectAttributes redirectAttributes) {
        financeCoreClient.removeShare(id, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Share removed.");
        return "redirect:/subscriptions/" + id + "/shares";
    }

    @PostMapping("/{id}/leave-share")
    public String leaveShare(@AuthenticationPrincipal UserDetails principal,
                             @PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        financeCoreClient.removeShare(id, currentUser(principal).getId());
        redirectAttributes.addFlashAttribute("successMessage", "You left the shared subscription.");
        return "redirect:/subscriptions";
    }

    private BigDecimal monthlyAmount(FinanceCoreClient.SubscriptionShareDto share) {
        return monthlyAmount(share.subscription(), share.percentageShare(), share.fixedAmount());
    }

    private BigDecimal monthlyAmount(FinanceCoreClient.SubscriptionShareRequestDto request) {
        return monthlyAmount(request.subscription(), request.percentageShare(), request.fixedAmount());
    }

    private BigDecimal monthlyAmount(FinanceCoreClient.SubscriptionDto subscription, BigDecimal percentage, BigDecimal fixedAmount) {
        if (subscription == null) {
            return BigDecimal.ZERO;
        }
        if (fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0) {
            return fixedAmount;
        }
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal base = subscription.price();
        if (subscription.billingFrequency() != null && subscription.billingFrequency().name().equals("YEARLY")) {
            base = base.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        }
        return base.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private User currentUser(UserDetails principal) {
        if (principal == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));
    }
}
