package com.awbd.financetracker.service;

import com.awbd.financetracker.client.FinanceCoreClient;
import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.dto.DashboardReportDto;
import com.awbd.financetracker.enums.BillingFrequency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportingServiceImpl implements ReportingService {

    private final UserDirectoryClient userDirectoryClient;
    private final FinanceCoreClient financeCoreClient;

    public ReportingServiceImpl(UserDirectoryClient userDirectoryClient, FinanceCoreClient financeCoreClient) {
        this.userDirectoryClient = userDirectoryClient;
        this.financeCoreClient = financeCoreClient;
    }

    @Override
    public DashboardReportDto getDashboard(Long userId) {
        var user = userDirectoryClient.getUser(userId);
        BigDecimal totalMonthlyCost = calculateTotalMonthlySubscriptionCost(userId);
        BigDecimal disposableIncome = user.monthlyIncome().subtract(totalMonthlyCost);
        List<FinanceCoreClient.SubscriptionDto> upcomingRenewals = getUpcomingRenewals(userId);
        return new DashboardReportDto(
                userId,
                user.monthlyIncome(),
                disposableIncome,
                totalMonthlyCost,
                totalMonthlyCost.multiply(BigDecimal.valueOf(12)),
                upcomingRenewals,
                upcomingRenewals.size(),
                getSpendingByCategory(userId)
        );
    }

    @Override
    public BigDecimal calculateDisposableIncome(Long userId) {
        var user = userDirectoryClient.getUser(userId);
        return user.monthlyIncome().subtract(calculateTotalMonthlySubscriptionCost(userId));
    }

    @Override
    public BigDecimal calculateTotalMonthlySubscriptionCost(Long userId) {
        BigDecimal total = financeCoreClient.getSubscriptions(userId).stream()
                .map(this::monthlyPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sharedAway = financeCoreClient.getSharesByOwner(userId).stream()
                .map(this::shareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.subtract(sharedAway).max(BigDecimal.ZERO);
    }

    @Override
    public List<FinanceCoreClient.SubscriptionDto> getUpcomingRenewals(Long userId) {
        return financeCoreClient.getUpcomingRenewals(userId);
    }

    @Override
    public Map<String, BigDecimal> getSpendingByCategory(Long userId) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (FinanceCoreClient.SubscriptionDto subscription : financeCoreClient.getSubscriptions(userId)) {
            String categoryName = subscription.category() == null ? "Uncategorised" : subscription.category().name();
            result.merge(categoryName, monthlyPrice(subscription), BigDecimal::add);
        }

        for (FinanceCoreClient.SubscriptionShareDto share : financeCoreClient.getSharesByOwner(userId)) {
            FinanceCoreClient.SubscriptionDto subscription = share.subscription();
            String categoryName = subscription.category() == null ? "Uncategorised" : subscription.category().name();
            BigDecimal sharedAmount = shareAmount(share);
            result.computeIfPresent(categoryName, (category, value) -> value.subtract(sharedAmount).max(BigDecimal.ZERO));
        }

        return result;
    }

    private BigDecimal monthlyPrice(FinanceCoreClient.SubscriptionDto subscription) {
        if (subscription.billingFrequency() == BillingFrequency.MONTHLY) {
            return subscription.price();
        }
        return subscription.price().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal shareAmount(FinanceCoreClient.SubscriptionShareDto share) {
        BigDecimal monthlyPrice = monthlyPrice(share.subscription());
        if (share.percentageShare() != null && share.percentageShare().compareTo(BigDecimal.ZERO) > 0) {
            return monthlyPrice.multiply(share.percentageShare())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        if (share.fixedAmount() != null && share.fixedAmount().compareTo(BigDecimal.ZERO) > 0) {
            return share.fixedAmount();
        }
        return BigDecimal.ZERO;
    }
}
