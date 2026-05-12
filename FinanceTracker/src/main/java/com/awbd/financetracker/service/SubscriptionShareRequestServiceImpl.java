package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShareId;
import com.awbd.financetracker.entity.SubscriptionShareRequest;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.SubscriptionShareRequestStatus;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
import com.awbd.financetracker.repository.SubscriptionShareRequestRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SubscriptionShareRequestServiceImpl implements SubscriptionShareRequestService {

    private final SubscriptionShareRequestRepository requestRepository;
    private final SubscriptionShareRepository shareRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionShareService subscriptionShareService;
    private final CategoryRepository categoryRepository;

    public SubscriptionShareRequestServiceImpl(SubscriptionShareRequestRepository requestRepository,
                                               SubscriptionShareRepository shareRepository,
                                               SubscriptionRepository subscriptionRepository,
                                               UserRepository userRepository,
                                               SubscriptionShareService subscriptionShareService,
                                               CategoryRepository categoryRepository) {
        this.requestRepository = requestRepository;
        this.shareRepository = shareRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.subscriptionShareService = subscriptionShareService;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public SubscriptionShareRequest createRequest(Long subscriptionId, Long requesterId, String recipientEmail,
                                                  BigDecimal percentageShare, BigDecimal fixedAmount) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        if (!subscription.getUser().getId().equals(requesterId)) {
            throw new AccessDeniedException("You do not own this subscription");
        }

        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new ResourceNotFoundException("Recipient email is required");
        }

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + recipientEmail));

        if (recipient.getId().equals(requesterId)) {
            throw new DuplicateResourceException("You cannot share a subscription with yourself");
        }

        boolean hasPercentage = percentageShare != null && percentageShare.compareTo(BigDecimal.ZERO) > 0;
        boolean hasFixed = fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0;
        if (!hasPercentage && !hasFixed) {
            throw new ResourceNotFoundException("Provide either a percentage share or a fixed amount");
        }

        if (shareRepository.existsById(new SubscriptionShareId(subscriptionId, recipient.getId()))) {
            throw new DuplicateResourceException("That user already has a share on this subscription");
        }

        if (requestRepository.existsBySubscriptionIdAndRecipientIdAndStatus(
                subscriptionId, recipient.getId(), SubscriptionShareRequestStatus.PENDING)) {
            throw new DuplicateResourceException("That user already has a pending request for this subscription");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requesterId));

        // Enforce mutual exclusivity: fixed amount takes priority over percentage
        BigDecimal resolvedPercentage = hasFixed ? null : percentageShare;
        BigDecimal resolvedFixed = hasFixed ? fixedAmount : null;

        SubscriptionShareRequest request = new SubscriptionShareRequest(
                subscription, requester, recipient, resolvedPercentage, resolvedFixed);
        return requestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShareRequest> getPendingRequestsForUser(Long recipientId) {
        return requestRepository.findByRecipientIdAndStatusOrderByCreatedAtDesc(
                recipientId, SubscriptionShareRequestStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShareRequest> getRequestsSentByUser(Long requesterId) {
        return requestRepository.findByRequestedByIdOrderByCreatedAtDesc(requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShareRequest> getRequestsForSubscription(Long subscriptionId) {
        return requestRepository.findBySubscriptionIdOrderByCreatedAtDesc(subscriptionId);
    }

    @Override
    public SubscriptionShareRequest acceptRequest(Long requestId, Long recipientId) {
        SubscriptionShareRequest request = getPendingRequestForRecipient(requestId, recipientId);
        subscriptionShareService.assignShare(
                request.getSubscription().getId(),
                recipientId,
                request.getPercentageShare(),
                request.getFixedAmount());

        // Auto-create the subscription's category for the recipient if they don't have it yet
        Category sourceCategory = request.getSubscription().getCategory();
        if (sourceCategory != null) {
            boolean alreadyHas = categoryRepository.existsByNameAndUserId(sourceCategory.getName(), recipientId);
            if (!alreadyHas) {
                User recipient = userRepository.findById(recipientId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + recipientId));
                Category copy = new Category();
                copy.setName(sourceCategory.getName());
                copy.setDescription(sourceCategory.getDescription());
                copy.setUser(recipient);
                categoryRepository.save(copy);
            }
        }

        request.setStatus(SubscriptionShareRequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    public SubscriptionShareRequest declineRequest(Long requestId, Long recipientId) {
        SubscriptionShareRequest request = getPendingRequestForRecipient(requestId, recipientId);
        request.setStatus(SubscriptionShareRequestStatus.DECLINED);
        request.setRespondedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    public void revokeRequest(Long requestId, Long requesterId) {
        SubscriptionShareRequest request = requestRepository.findByIdAndRequestedById(requestId, requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Share request not found with id: " + requestId));
        if (request.getStatus() != SubscriptionShareRequestStatus.PENDING) {
            throw new DuplicateResourceException("Only pending share requests can be revoked");
        }
        requestRepository.deleteById(requestId);
    }

    private SubscriptionShareRequest getPendingRequestForRecipient(Long requestId, Long recipientId) {
        SubscriptionShareRequest request = requestRepository.findByIdAndRecipientId(requestId, recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Share request not found with id: " + requestId));
        if (request.getStatus() != SubscriptionShareRequestStatus.PENDING) {
            throw new DuplicateResourceException("Only pending share requests can be changed");
        }
        return request;
    }
}