package com.awbd.financetracker.dto;

import com.awbd.financetracker.enums.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PaymentMethodForm {

    private Long id;

    @NotNull(message = "Type is required")
    private PaymentType type;

    @NotBlank(message = "Details are required")
    @Size(max = 255, message = "Details must be less than 255 characters")
    private String details;

    public PaymentMethodForm() {
    }

    public PaymentMethodForm(Long id, PaymentType type, String details) {
        this.id = id;
        this.type = type;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
