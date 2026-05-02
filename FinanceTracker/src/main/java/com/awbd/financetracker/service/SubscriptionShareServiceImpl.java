package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.SubscriptionShareId;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubscriptionShareServiceImpl implements SubscriptionShareService {

    private final SubscriptionShareRepository subscriptionShareRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionShareServiceImpl(SubscriptionShareRepository subscriptionShareRepository,
                                        SubscriptionRepository subscriptionRepository,
                                        UserRepository userRepository) {
        this.subscriptionShareRepository = subscriptionShareRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public SubscriptionShare assignShare(Long subscriptionId, Long userId,
                                         BigDecimal percentageShare, BigDecimal fixedAmount) {
        SubscriptionShareId shareId = new SubscriptionShareId(subscriptionId, userId);
        if (subscriptionShareRepository.existsById(shareId)) {
            throw new IllegalStateException("Share already exists for subscription " + subscriptionId
                    + " and user " + userId);
        }
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + subscriptionId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        SubscriptionShare share = new SubscriptionShare(subscription, user, percentageShare, fixedAmount);
        return subscriptionShareRepository.save(share);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriptionShare> getShare(Long subscriptionId, Long userId) {
        return subscriptionShareRepository.findById(new SubscriptionShareId(subscriptionId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShare> getSharesBySubscription(Long subscriptionId) {
        return subscriptionShareRepository.findByIdSubscriptionId(subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShare> getSharesByUser(Long userId) {
        return subscriptionShareRepository.findByIdUserId(userId);
    }

    @Override
    public SubscriptionShare updateShare(Long subscriptionId, Long userId,
                                          BigDecimal percentageShare, BigDecimal fixedAmount) {
        SubscriptionShare share = subscriptionShareRepository
                .findById(new SubscriptionShareId(subscriptionId, userId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Share not found for subscription " + subscriptionId + " and user " + userId));
        share.setPercentageShare(percentageShare);
        share.setFixedAmount(fixedAmount);
        return subscriptionShareRepository.save(share);
    }

    @Override
    public void removeShare(Long subscriptionId, Long userId) {
        SubscriptionShareId shareId = new SubscriptionShareId(subscriptionId, userId);
        if (!subscriptionShareRepository.existsById(shareId)) {
            throw new IllegalArgumentException(
                    "Share not found for subscription " + subscriptionId + " and user " + userId);
        }
        subscriptionShareRepository.deleteById(shareId);
    }
}
