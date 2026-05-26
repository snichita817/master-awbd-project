package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.SubscriptionShare;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SubscriptionShareService {

    SubscriptionShare assignShare(Long subscriptionId, Long participantUserId, BigDecimal percentageShare, BigDecimal fixedAmount);

    Optional<SubscriptionShare> getShare(Long subscriptionId, Long participantUserId);

    List<SubscriptionShare> getSharesBySubscription(Long subscriptionId);

    List<SubscriptionShare> getSharesByUser(Long participantUserId);

    List<SubscriptionShare> getSharesBySubscriptionOwner(Long ownerUserId);

    SubscriptionShare updateShare(Long subscriptionId, Long participantUserId, BigDecimal percentageShare, BigDecimal fixedAmount);

    void removeShare(Long subscriptionId, Long participantUserId);
}
