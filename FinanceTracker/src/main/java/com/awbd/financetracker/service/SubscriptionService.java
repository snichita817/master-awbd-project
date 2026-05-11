package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SubscriptionService {

    Subscription createSubscription(Long userId, Long categoryId, Long paymentMethodId, Subscription subscription);

    Optional<Subscription> getSubscriptionById(Long id);

    List<Subscription> getAllSubscriptions();

    List<Subscription> getSubscriptionsByUserId(Long userId);

    Page<Subscription> getSubscriptionsByUserId(Long userId, Pageable pageable);

    List<Subscription> getUpcomingRenewals(Long userId);

    Subscription updateSubscription(Long id, Long categoryId, Long paymentMethodId, Subscription subscription);

    void deleteSubscription(Long id);
}

