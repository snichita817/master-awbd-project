package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.SubscriptionShareId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SubscriptionShareService {

    SubscriptionShare assignShare(Long subscriptionId, Long userId, BigDecimal percentageShare,
                                  BigDecimal fixedAmount);

    Optional<SubscriptionShare> getShare(Long subscriptionId, Long userId);

    List<SubscriptionShare> getSharesBySubscription(Long subscriptionId);

    List<SubscriptionShare> getSharesByUser(Long userId);

    SubscriptionShare updateShare(Long subscriptionId, Long userId, BigDecimal percentageShare,
                                   BigDecimal fixedAmount);

    void removeShare(Long subscriptionId, Long userId);
}
