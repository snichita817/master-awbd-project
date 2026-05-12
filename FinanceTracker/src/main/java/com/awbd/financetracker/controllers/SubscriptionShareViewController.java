package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.SubscriptionShareRequest;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.service.SubscriptionService;
import com.awbd.financetracker.service.SubscriptionShareRequestService;
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
    private final SubscriptionShareRequestService subscriptionShareRequestService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;

    public SubscriptionShareViewController(SubscriptionShareService subscriptionShareService,
                                           SubscriptionShareRequestService subscriptionShareRequestService,
                                           SubscriptionService subscriptionService,
                                           UserService userService) {
        this.subscriptionShareService = subscriptionShareService;
        this.subscriptionShareRequestService = subscriptionShareRequestService;
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // GET /{id}/shares  -  list current shares + add-share form
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
        List<SubscriptionShareRequest> shareRequests = subscriptionShareRequestService.getRequestsForSubscription(id);
        model.addAttribute("subscription", subscription);
        model.addAttribute("shares", shares);
        model.addAttribute("shareRequests", shareRequests);
        model.addAttribute("subscriptionPrice", subscription.getPrice());
        model.addAttribute("subscriptionBillingFrequency", subscription.getBillingFrequency());
        model.addAttribute("monthlyAmounts", buildMonthlyAmountsForShares(subscription, shares));
        model.addAttribute("requestMonthlyAmounts", buildMonthlyAmountsForRequests(subscription, shareRequests));
        return "subscriptions/shares";
    }

    // -------------------------------------------------------------------------
    // POST /{id}/shares  -  send a share request
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

        try {
            subscriptionShareRequestService.createRequest(
                    id, currentUser.getId(), email, percentageShare, fixedAmount);
        } catch (ResourceNotFoundException ex) {
            return reRenderShares(id, subscription, model, ex.getMessage());
        } catch (DuplicateResourceException ex) {
            return reRenderShares(id, subscription, model, ex.getMessage());
        }

        redirectAttrs.addFlashAttribute("successMessage", "Share request sent to " + email);
        return "redirect:/subscriptions/" + id + "/shares";
    }

    @GetMapping("/share-requests")
    public String shareRequestsPage(@AuthenticationPrincipal UserDetails principal, Model model) {
        User currentUser = userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        List<SubscriptionShareRequest> requests = subscriptionShareRequestService
                .getPendingRequestsForUser(currentUser.getId());
        model.addAttribute("requests", requests);
        model.addAttribute("requestMonthlyAmounts", buildMonthlyAmountsForRequests(requests));
        return "subscriptions/share-requests";
    }

    @PostMapping("/share-requests/{requestId}/accept")
    public String acceptShareRequest(@PathVariable Long requestId,
                                     @AuthenticationPrincipal UserDetails principal,
                                     RedirectAttributes redirectAttrs) {
        User currentUser = userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        subscriptionShareRequestService.acceptRequest(requestId, currentUser.getId());
        redirectAttrs.addFlashAttribute("successMessage", "Share request accepted.");
        return "redirect:/subscriptions/share-requests";
    }

    @PostMapping("/share-requests/{requestId}/decline")
    public String declineShareRequest(@PathVariable Long requestId,
                                      @AuthenticationPrincipal UserDetails principal,
                                      RedirectAttributes redirectAttrs) {
        User currentUser = userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        subscriptionShareRequestService.declineRequest(requestId, currentUser.getId());
        redirectAttrs.addFlashAttribute("successMessage", "Share request declined.");
        return "redirect:/subscriptions/share-requests";
    }

    @PostMapping("/share-requests/{requestId}/revoke")
    public String revokeShareRequest(@PathVariable Long requestId,
                                     @RequestParam Long subscriptionId,
                                     @AuthenticationPrincipal UserDetails principal,
                                     RedirectAttributes redirectAttrs) {
        User currentUser = userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        subscriptionShareRequestService.revokeRequest(requestId, currentUser.getId());
        redirectAttrs.addFlashAttribute("successMessage", "Share request revoked.");
        return "redirect:/subscriptions/" + subscriptionId + "/shares";
    }

    // -------------------------------------------------------------------------
    // POST /{id}/shares/{userId}/delete  -  remove a share
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
        List<SubscriptionShareRequest> shareRequests = subscriptionShareRequestService.getRequestsForSubscription(id);
        model.addAttribute("subscription", subscription);
        model.addAttribute("shares", shares);
        model.addAttribute("shareRequests", shareRequests);
        model.addAttribute("subscriptionPrice", subscription.getPrice());
        model.addAttribute("subscriptionBillingFrequency", subscription.getBillingFrequency());
        model.addAttribute("monthlyAmounts", buildMonthlyAmountsForShares(subscription, shares));
        model.addAttribute("requestMonthlyAmounts", buildMonthlyAmountsForRequests(subscription, shareRequests));
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

    private Map<Long, BigDecimal> buildMonthlyAmountsForRequests(Subscription subscription,
                                                                  List<SubscriptionShareRequest> requests) {
        Map<Long, BigDecimal> map = new LinkedHashMap<>();
        for (SubscriptionShareRequest request : requests) {
            map.put(request.getId(), computeMonthlyAmount(
                    subscription, request.getPercentageShare(), request.getFixedAmount()));
        }
        return map;
    }

    private Map<Long, BigDecimal> buildMonthlyAmountsForRequests(List<SubscriptionShareRequest> requests) {
        Map<Long, BigDecimal> map = new LinkedHashMap<>();
        for (SubscriptionShareRequest request : requests) {
            map.put(request.getId(), computeMonthlyAmount(
                    request.getSubscription(), request.getPercentageShare(), request.getFixedAmount()));
        }
        return map;
    }
}
