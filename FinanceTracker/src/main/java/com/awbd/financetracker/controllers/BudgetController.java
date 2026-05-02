package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Budget;
import com.awbd.financetracker.service.BudgetService;
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
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Set spending limits for categories and track current spending")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Operation(summary = "Create a budget for a category", description = "Sets a spending limit for a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Budget created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or budget already exists for this category"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PostMapping("/category/{categoryId}")
    public ResponseEntity<Budget> createBudget(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Valid @RequestBody Budget budget) {
        Budget created = budgetService.createBudget(categoryId, budget);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get budget by ID", description = "Retrieves a specific budget by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget found"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(
            @Parameter(description = "Budget ID") @PathVariable Long id) {
        return budgetService.getBudgetById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found with id: " + id));
    }

    @Operation(summary = "Get all budgets of the family", description = "Retrieves a list of all budgets in the system")
    @ApiResponse(responseCode = "200", description = "List of budgets retrieved successfully")
    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        List<Budget> budgets = budgetService.getAllBudgets();
        return ResponseEntity.ok(budgets);
    }

    @Operation(summary = "Get budget by category", description = "Retrieves the budget set for a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget found"),
            @ApiResponse(responseCode = "404", description = "Budget not found for this category")
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Budget> getBudgetByCategoryId(@PathVariable Long categoryId) {
        return budgetService.getBudgetByCategoryId(categoryId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found for category id: " + categoryId));
    }

    @Operation(summary = "Get all budgets for a user", description = "Retrieves all budgets across all categories for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budgets retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Budget>> getBudgetsByUserId(@PathVariable Long userId) {
        List<Budget> budgets = budgetService.getBudgetsByUserId(userId);
        return ResponseEntity.ok(budgets);
    }

    @Operation(summary = "Update budget", description = "Updates the spending limit or current spending for a budget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(
            @Parameter(description = "Budget ID") @PathVariable Long id,
            @Valid @RequestBody Budget budget) {
        Budget updated = budgetService.updateBudget(id, budget);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete budget", description = "Removes a budget from a category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}