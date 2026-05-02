package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.service.PaymentMethodService;
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
@RequestMapping("/api/payment-methods")
@Tag(name = "Payment Methods", description = "Manage payment methods like Credit Cards, PayPal, Bank Transfer, etc.")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @Operation(summary = "Add a payment method", description = "Adds a new payment method for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment method created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/user/{userId}")
    public ResponseEntity<PaymentMethod> createPaymentMethod(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody PaymentMethod paymentMethod) {
        PaymentMethod created = paymentMethodService.createPaymentMethod(userId, paymentMethod);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get payment method by ID", description = "Retrieves a specific payment method by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment method found"),
            @ApiResponse(responseCode = "404", description = "Payment method not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethod> getPaymentMethodById(@PathVariable Long id) {
        return paymentMethodService.getPaymentMethodById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found with id: " + id));
    }

    @Operation(summary = "Get all payment methods", description = "Retrieves a list of all payment methods in the system")
    @ApiResponse(responseCode = "200", description = "List of payment methods retrieved successfully")
    @GetMapping
    public ResponseEntity<List<PaymentMethod>> getAllPaymentMethods() {
        List<PaymentMethod> paymentMethods = paymentMethodService.getAllPaymentMethods();
        return ResponseEntity.ok(paymentMethods);
    }

    @Operation(summary = "Get payment methods by user", description = "Retrieves all payment methods belonging to a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment methods retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethodsByUserId(@PathVariable Long userId) {
        List<PaymentMethod> paymentMethods = paymentMethodService.getPaymentMethodsByUserId(userId);
        return ResponseEntity.ok(paymentMethods);
    }

    @Operation(summary = "Get payment methods by user and type", description = "Retrieves payment methods for a user filtered by type (e.g., CREDIT_CARD, PAYPAL)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment methods retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethodsByUserIdAndType(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Payment type (CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER)") @PathVariable PaymentType type) {
        List<PaymentMethod> paymentMethods = paymentMethodService.getPaymentMethodsByUserIdAndType(userId, type);
        return ResponseEntity.ok(paymentMethods);
    }

    @Operation(summary = "Update payment method", description = "Updates an existing payment method's details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment method updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Payment method not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethod> updatePaymentMethod(
            @Parameter(description = "Payment method ID") @PathVariable Long id,
            @Valid @RequestBody PaymentMethod paymentMethod) {
        PaymentMethod updated = paymentMethodService.updatePaymentMethod(id, paymentMethod);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete payment method", description = "Removes a payment method. Cannot delete if linked to active subscriptions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Payment method deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete - payment method is linked to subscriptions"),
            @ApiResponse(responseCode = "404", description = "Payment method not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long id) {
        paymentMethodService.deletePaymentMethod(id);
        return ResponseEntity.noContent().build();
    }
}

