package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FinanceServiceImpl implements FinanceService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public FinanceServiceImpl(UserRepository userRepository,
                              SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
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

        return subscriptions.stream()
                .map(this::getMonthlyPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Subscription> getUpcomingRenewals(Long userId) {
        return subscriptionRepository.findUpcomingRenewals(userId);
    }

    private BigDecimal getMonthlyPrice(Subscription subscription) {
        if (subscription.getBillingFrequency() == BillingFrequency.MONTHLY) {
            return subscription.getPrice();
        } else {
            return subscription.getPrice().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        }
    }
}

