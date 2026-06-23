package com.awbd.financetracker.dto;

import com.awbd.financetracker.entity.User;

import java.math.BigDecimal;

public record UserSummaryDto(Long id, String name, String email, BigDecimal monthlyIncome) {

    public static UserSummaryDto from(User user) {
        return new UserSummaryDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMonthlyIncome()
        );
    }
}
