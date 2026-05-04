package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class FinanceServiceImpl implements FinanceService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionShareRepository subscriptionShareRepository;

    public FinanceServiceImpl(UserRepository userRepository,
                              SubscriptionRepository subscriptionRepository,
                              SubscriptionShareRepository subscriptionShareRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionShareRepository = subscriptionShareRepository;
    }

    @Override
    public BigDecimal calculateDisposableIncome(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        BigDecimal monthlyIncome = user.getMonthlyIncome();
        BigDecimal totalMonthlySubscriptionCost = calculateTotalMonthlySubscriptionCost(userId);

        return monthlyIncome.subtract(totalMonthlySubscriptionCost);
    }

    @Override
    public BigDecimal calculateTotalMonthlySubscriptionCost(Long userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);

        BigDecimal total = subscriptions.stream()
                .map(this::getMonthlyPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Subtract amounts the owner has shared away
        BigDecimal sharedAway = subscriptionShareRepository.findBySubscriptionOwnerId(userId).stream()
                .map(share -> computeShareAmount(share.getSubscription(),
                        share.getPercentageShare(), share.getFixedAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.subtract(sharedAway).max(BigDecimal.ZERO);
    }

    @Override
    public List<Subscription> getUpcomingRenewals(Long userId) {
        return subscriptionRepository.findUpcomingRenewals(userId);
    }

    @Override
    public Map<String, BigDecimal> getSpendingByCategory(Long userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Subscription sub : subscriptions) {
            String categoryName = (sub.getCategory() != null) ? sub.getCategory().getName() : "Uncategorised";
            result.merge(categoryName, getMonthlyPrice(sub), BigDecimal::add);
        }

        // Subtract shared-away amounts from each category
        for (SubscriptionShare share : subscriptionShareRepository.findBySubscriptionOwnerId(userId)) {
            Subscription sub = share.getSubscription();
            String categoryName = (sub.getCategory() != null) ? sub.getCategory().getName() : "Uncategorised";
            BigDecimal sharedAmount = computeShareAmount(sub, share.getPercentageShare(), share.getFixedAmount());
            result.computeIfPresent(categoryName,
                    (k, v) -> v.subtract(sharedAmount).max(BigDecimal.ZERO));
        }

        return result;
    }

    private BigDecimal getMonthlyPrice(Subscription subscription) {
        if (subscription.getBillingFrequency() == BillingFrequency.MONTHLY) {
            return subscription.getPrice();
        } else {
            return subscription.getPrice().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal computeShareAmount(Subscription subscription,
                                           BigDecimal percentageShare,
                                           BigDecimal fixedAmount) {
        BigDecimal monthlyPrice = getMonthlyPrice(subscription);
        if (percentageShare != null && percentageShare.compareTo(BigDecimal.ZERO) > 0) {
            return monthlyPrice.multiply(percentageShare)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        if (fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0) {
            return fixedAmount;
        }
        return BigDecimal.ZERO;
    }
}

