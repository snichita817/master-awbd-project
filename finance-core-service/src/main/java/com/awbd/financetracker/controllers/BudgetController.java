package com.awbd.financetracker.controllers;

import com.awbd.financetracker.dto.BudgetCreateDto;
import com.awbd.financetracker.dto.BudgetDto;
import com.awbd.financetracker.dto.BudgetUpdateDto;
import com.awbd.financetracker.dto.FinanceCoreMapper;
import com.awbd.financetracker.entity.Budget;
import com.awbd.financetracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetDto> createBudget(@Valid @RequestBody BudgetCreateDto request) {
        Budget budget = new Budget();
        budget.setMaxLimit(request.maxLimit());
        budget.setCurrentSpending(BigDecimal.ZERO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinanceCoreMapper.toDto(budgetService.createBudget(request.categoryId(), budget)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetDto> getBudget(@PathVariable Long id) {
        return budgetService.getBudgetById(id)
                .map(FinanceCoreMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<BudgetDto> getBudgetByCategory(@PathVariable Long categoryId) {
        return budgetService.getBudgetByCategoryId(categoryId)
                .map(FinanceCoreMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerUserId}")
    public ResponseEntity<List<BudgetDto>> getBudgetsByOwner(@PathVariable Long ownerUserId) {
        return ResponseEntity.ok(budgetService.getBudgetsByOwnerUserId(ownerUserId)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> updateBudget(@PathVariable Long id,
                                                  @Valid @RequestBody BudgetUpdateDto request) {
        Budget budget = new Budget();
        budget.setMaxLimit(request.maxLimit());
        return ResponseEntity.ok(FinanceCoreMapper.toDto(budgetService.updateBudget(id, budget)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
