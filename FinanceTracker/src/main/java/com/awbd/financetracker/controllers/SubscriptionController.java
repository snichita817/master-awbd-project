package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscriptions", description = "Manage user subscriptions like Netflix, Spotify, etc.")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "Create a new subscription", description = "Creates a subscription for a user, optionally linked to a category and payment method")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User, category, or payment method not found")
    })
    @PostMapping("/user/{userId}")
    public ResponseEntity<Subscription> createSubscription(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Category ID (optional)") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Payment method ID (optional)") @RequestParam(required = false) Long paymentMethodId,
            @Valid @RequestBody Subscription subscription) {
        Subscription created = subscriptionService.createSubscription(userId, categoryId, paymentMethodId, subscription);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get subscription by ID", description = "Retrieves a specific subscription by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription found"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Subscription> getSubscriptionById(@PathVariable Long id) {
        return subscriptionService.getSubscriptionById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + id));
    }

    @Operation(summary = "Get all subscriptions", description = "Retrieves a list of all subscriptions in the system")
    @ApiResponse(responseCode = "200", description = "List of subscriptions retrieved successfully")
    @GetMapping
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Get subscriptions by user", description = "Retrieves all subscriptions belonging to a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Subscription>> getSubscriptionsByUserId(@PathVariable Long userId) {
        List<Subscription> subscriptions = subscriptionService.getSubscriptionsByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Get upcoming renewals", description = "Retrieves subscriptions that will renew within the next 7 days")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upcoming renewals retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/upcoming-renewals")
    public ResponseEntity<List<Subscription>> getUpcomingRenewals(@PathVariable Long userId) {
        List<Subscription> subscriptions = subscriptionService.getUpcomingRenewals(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Update subscription", description = "Updates an existing subscription's details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Subscription> updateSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id,
            @Parameter(description = "New category ID (optional)") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "New payment method ID (optional)") @RequestParam(required = false) Long paymentMethodId,
            @Valid @RequestBody Subscription subscription) {
        Subscription updated = subscriptionService.updateSubscription(id, categoryId, paymentMethodId, subscription);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete subscription", description = "Removes a subscription from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subscription deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }
}

