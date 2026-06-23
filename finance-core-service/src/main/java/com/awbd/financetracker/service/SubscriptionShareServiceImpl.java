package com.awbd.financetracker.service;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.SubscriptionShareId;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
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
    private final UserDirectoryClient userDirectoryClient;

    public SubscriptionShareServiceImpl(SubscriptionShareRepository subscriptionShareRepository,
                                        SubscriptionRepository subscriptionRepository,
                                        UserDirectoryClient userDirectoryClient) {
        this.subscriptionShareRepository = subscriptionShareRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userDirectoryClient = userDirectoryClient;
    }

    @Override
    public SubscriptionShare assignShare(Long subscriptionId, Long participantUserId, BigDecimal percentageShare, BigDecimal fixedAmount) {
        SubscriptionShareId shareId = new SubscriptionShareId(subscriptionId, participantUserId);
        if (subscriptionShareRepository.existsById(shareId)) {
            throw new DuplicateResourceException("Share already exists for subscription " + subscriptionId + " and user " + participantUserId);
        }
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));
        userDirectoryClient.requireUser(participantUserId);
        boolean hasFixed = fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0;
        SubscriptionShare share = new SubscriptionShare(subscription, participantUserId, hasFixed ? null : percentageShare, hasFixed ? fixedAmount : null);
        SubscriptionShare saved = subscriptionShareRepository.save(share);
        log.info("SubscriptionShare assigned: subscriptionId={}, participantUserId={}", subscriptionId, participantUserId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriptionShare> getShare(Long subscriptionId, Long participantUserId) {
        return subscriptionShareRepository.findById(new SubscriptionShareId(subscriptionId, participantUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShare> getSharesBySubscription(Long subscriptionId) {
        return subscriptionShareRepository.findByIdSubscriptionId(subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShare> getSharesByUser(Long participantUserId) {
        userDirectoryClient.requireUser(participantUserId);
        return subscriptionShareRepository.findByIdUserId(participantUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShare> getSharesBySubscriptionOwner(Long ownerUserId) {
        userDirectoryClient.requireUser(ownerUserId);
        return subscriptionShareRepository.findBySubscriptionOwnerId(ownerUserId);
    }

    @Override
    public SubscriptionShare updateShare(Long subscriptionId, Long participantUserId, BigDecimal percentageShare, BigDecimal fixedAmount) {
        SubscriptionShare share = subscriptionShareRepository.findById(new SubscriptionShareId(subscriptionId, participantUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Share not found for subscription " + subscriptionId + " and user " + participantUserId));
        boolean hasFixed = fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0;
        share.setPercentageShare(hasFixed ? null : percentageShare);
        share.setFixedAmount(hasFixed ? fixedAmount : null);
        SubscriptionShare saved = subscriptionShareRepository.save(share);
        log.info("SubscriptionShare updated: subscriptionId={}, participantUserId={}", subscriptionId, participantUserId);
        return saved;
    }

    @Override
    public void removeShare(Long subscriptionId, Long participantUserId) {
        SubscriptionShareId shareId = new SubscriptionShareId(subscriptionId, participantUserId);
        SubscriptionShare share = subscriptionShareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found for subscription " + subscriptionId + " and user " + participantUserId));
        subscriptionShareRepository.delete(share);
        log.info("SubscriptionShare removed: subscriptionId={}, participantUserId={}", subscriptionId, participantUserId);
    }
}
