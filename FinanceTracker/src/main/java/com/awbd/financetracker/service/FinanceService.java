package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;

import java.math.BigDecimal;
import java.util.List;

public interface FinanceService {

    BigDecimal calculateDisposableIncome(Long userId);

    BigDecimal calculateTotalMonthlySubscriptionCost(Long userId);

    List<Subscription> getUpcomingRenewals(Long userId);
}

