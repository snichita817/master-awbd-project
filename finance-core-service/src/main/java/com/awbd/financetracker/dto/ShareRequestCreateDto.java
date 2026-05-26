package com.awbd.financetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ShareRequestCreateDto(@NotBlank @Email String recipientEmail,
                                    BigDecimal percentageShare,
                                    BigDecimal fixedAmount) {
}
