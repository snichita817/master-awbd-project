package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Transaction;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Record and track subscription payment transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Record a transaction", description = "Records a payment transaction for a subscription. If no date is provided, uses current time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction recorded successfully"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @PostMapping("/subscription/{subscriptionId}")
    public ResponseEntity<Transaction> createTransaction(
            @Parameter(description = "Subscription ID") @PathVariable Long subscriptionId,
            @Parameter(description = "Transaction date (optional, defaults to now)") @RequestParam(required = false) LocalDateTime transactionDate) {
        if(transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }

        Transaction created = transactionService.createTransaction(subscriptionId, transactionDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get transaction by ID", description = "Retrieves a specific transaction by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    @Operation(summary = "Get transactions by subscription", description = "Retrieves all transactions for a specific subscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<Transaction>> getTransactionsBySubscriptionId(@PathVariable Long subscriptionId) {
        List<Transaction> transactions = transactionService.getTransactionsBySubscriptionId(subscriptionId);
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Delete transaction", description = "Removes a transaction record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}


