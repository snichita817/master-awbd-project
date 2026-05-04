package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface FinanceService {

    BigDecimal calculateDisposableIncome(Long userId);

    BigDecimal calculateTotalMonthlySubscriptionCost(Long userId);

    List<Subscription> getUpcomingRenewals(Long userId);

    /** Returns monthly cost per category name (subscriptions with no category are grouped under "Uncategorised"). */
    Map<String, BigDecimal> getSpendingByCategory(Long userId);
}

