package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SubscriptionService {

    Subscription createSubscription(Long ownerUserId, Long categoryId, Long paymentMethodId, Subscription subscription);

    Optional<Subscription> getSubscriptionById(Long id);

    List<Subscription> getAllSubscriptions();

    List<Subscription> getSubscriptionsByOwnerUserId(Long ownerUserId);

    Page<Subscription> getSubscriptionsByOwnerUserId(Long ownerUserId, Pageable pageable);

    List<Subscription> getUpcomingRenewals(Long ownerUserId);

    Subscription updateSubscription(Long id, Long categoryId, Long paymentMethodId, Subscription subscription);

    void deleteSubscription(Long id);
}
