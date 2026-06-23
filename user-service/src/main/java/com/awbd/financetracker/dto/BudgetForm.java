package com.awbd.financetracker.dto;

import com.awbd.financetracker.client.FinanceCoreClient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class BudgetForm {

    private Long id;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Maximum limit is required")
    @DecimalMin(value = "0.01", message = "Maximum limit must be greater than 0")
    private BigDecimal maxLimit;

    private BigDecimal currentSpending = BigDecimal.ZERO;

    private FinanceCoreClient.CategoryDto category;

    public BudgetForm() {
    }

    public BudgetForm(Long id, Long categoryId, BigDecimal maxLimit, BigDecimal currentSpending,
                      FinanceCoreClient.CategoryDto category) {
        this.id = id;
        this.categoryId = categoryId;
        this.maxLimit = maxLimit;
        this.currentSpending = currentSpending;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(BigDecimal maxLimit) {
        this.maxLimit = maxLimit;
    }

    public BigDecimal getCurrentSpending() {
        return currentSpending;
    }

    public void setCurrentSpending(BigDecimal currentSpending) {
        this.currentSpending = currentSpending;
    }

    public FinanceCoreClient.CategoryDto getCategory() {
        return category;
    }

    public void setCategory(FinanceCoreClient.CategoryDto category) {
        this.category = category;
    }
}
