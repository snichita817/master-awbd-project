package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.SubscriptionShareId;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubscriptionShareServiceImpl implements SubscriptionShareService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionShareServiceImpl.class);

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
            throw new DuplicateResourceException("Share already exists for subscription " + subscriptionId
                    + " and user " + userId);
        }
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Enforce mutual exclusivity: fixed amount takes priority over percentage
        boolean hasFixed = fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0;
        BigDecimal resolvedPercentage = hasFixed ? null : percentageShare;
        BigDecimal resolvedFixed = hasFixed ? fixedAmount : null;

        SubscriptionShare share = new SubscriptionShare(subscription, user, resolvedPercentage, resolvedFixed);
        SubscriptionShare saved = subscriptionShareRepository.save(share);
        log.info("SubscriptionShare assigned: subscriptionId={}, userId={}", subscriptionId, userId);
        return saved;
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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Share not found for subscription " + subscriptionId + " and user " + userId));
        // Enforce mutual exclusivity: fixed amount takes priority over percentage
        boolean hasFixed = fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0;
        share.setPercentageShare(hasFixed ? null : percentageShare);
        share.setFixedAmount(hasFixed ? fixedAmount : null);
        SubscriptionShare saved = subscriptionShareRepository.save(share);
        log.info("SubscriptionShare updated: subscriptionId={}, userId={}", subscriptionId, userId);
        return saved;
    }

    @Override
    public void removeShare(Long subscriptionId, Long userId) {
        SubscriptionShareId shareId = new SubscriptionShareId(subscriptionId, userId);
        SubscriptionShare share = subscriptionShareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Share not found for subscription " + subscriptionId + " and user " + userId));

        subscriptionShareRepository.deleteById(shareId);
        log.info("SubscriptionShare removed: subscriptionId={}, userId={}", subscriptionId, userId);
    }
}
