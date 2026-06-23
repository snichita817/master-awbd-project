package com.awbd.financetracker.service;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShareId;
import com.awbd.financetracker.entity.SubscriptionShareRequest;
import com.awbd.financetracker.enums.SubscriptionShareRequestStatus;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
import com.awbd.financetracker.repository.SubscriptionShareRequestRepository;
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
    private final SubscriptionShareService subscriptionShareService;
    private final CategoryRepository categoryRepository;
    private final UserDirectoryClient userDirectoryClient;

    public SubscriptionShareRequestServiceImpl(SubscriptionShareRequestRepository requestRepository,
                                               SubscriptionShareRepository shareRepository,
                                               SubscriptionRepository subscriptionRepository,
                                               SubscriptionShareService subscriptionShareService,
                                               CategoryRepository categoryRepository,
                                               UserDirectoryClient userDirectoryClient) {
        this.requestRepository = requestRepository;
        this.shareRepository = shareRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionShareService = subscriptionShareService;
        this.categoryRepository = categoryRepository;
        this.userDirectoryClient = userDirectoryClient;
    }

    @Override
    public SubscriptionShareRequest createRequest(Long subscriptionId, Long requesterId, String recipientEmail, BigDecimal percentageShare, BigDecimal fixedAmount) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));
        if (!subscription.getOwnerUserId().equals(requesterId)) {
            throw new AccessDeniedException("You do not own this subscription");
        }
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new ResourceNotFoundException("Recipient email is required");
        }
        UserDirectoryClient.UserSummary recipient = userDirectoryClient.requireUserByEmail(recipientEmail);
        if (recipient.id().equals(requesterId)) {
            throw new DuplicateResourceException("You cannot share a subscription with yourself");
        }
        boolean hasPercentage = percentageShare != null && percentageShare.compareTo(BigDecimal.ZERO) > 0;
        boolean hasFixed = fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0;
        if (!hasPercentage && !hasFixed) {
            throw new ResourceNotFoundException("Provide either a percentage share or a fixed amount");
        }
        if (shareRepository.existsById(new SubscriptionShareId(subscriptionId, recipient.id()))) {
            throw new DuplicateResourceException("That user already has a share on this subscription");
        }
        if (requestRepository.existsBySubscriptionIdAndRecipientUserIdAndStatus(subscriptionId, recipient.id(), SubscriptionShareRequestStatus.PENDING)) {
            throw new DuplicateResourceException("That user already has a pending request for this subscription");
        }
        SubscriptionShareRequest request = new SubscriptionShareRequest(
                subscription,
                requesterId,
                recipient.id(),
                hasFixed ? null : percentageShare,
                hasFixed ? fixedAmount : null
        );
        return requestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShareRequest> getPendingRequestsForUser(Long recipientUserId) {
        userDirectoryClient.requireUser(recipientUserId);
        return requestRepository.findByRecipientUserIdAndStatusOrderByCreatedAtDesc(recipientUserId, SubscriptionShareRequestStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShareRequest> getRequestsSentByUser(Long requestedByUserId) {
        userDirectoryClient.requireUser(requestedByUserId);
        return requestRepository.findByRequestedByUserIdOrderByCreatedAtDesc(requestedByUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionShareRequest> getRequestsForSubscription(Long subscriptionId) {
        return requestRepository.findBySubscriptionIdOrderByCreatedAtDesc(subscriptionId);
    }

    @Override
    public SubscriptionShareRequest acceptRequest(Long requestId, Long recipientUserId) {
        SubscriptionShareRequest request = getPendingRequestForRecipient(requestId, recipientUserId);
        subscriptionShareService.assignShare(request.getSubscription().getId(), recipientUserId, request.getPercentageShare(), request.getFixedAmount());

        Category sourceCategory = request.getSubscription().getCategory();
        if (sourceCategory != null && !categoryRepository.existsByNameAndOwnerUserId(sourceCategory.getName(), recipientUserId)) {
            Category copy = new Category();
            copy.setName(sourceCategory.getName());
            copy.setDescription(sourceCategory.getDescription());
            copy.setOwnerUserId(recipientUserId);
            categoryRepository.save(copy);
        }

        request.setStatus(SubscriptionShareRequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    public SubscriptionShareRequest declineRequest(Long requestId, Long recipientUserId) {
        SubscriptionShareRequest request = getPendingRequestForRecipient(requestId, recipientUserId);
        request.setStatus(SubscriptionShareRequestStatus.DECLINED);
        request.setRespondedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    public void revokeRequest(Long requestId, Long requesterId) {
        SubscriptionShareRequest request = requestRepository.findByIdAndRequestedByUserId(requestId, requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Share request not found with id: " + requestId));
        if (request.getStatus() != SubscriptionShareRequestStatus.PENDING) {
            throw new DuplicateResourceException("Only pending share requests can be revoked");
        }
        requestRepository.delete(request);
    }

    private SubscriptionShareRequest getPendingRequestForRecipient(Long requestId, Long recipientUserId) {
        SubscriptionShareRequest request = requestRepository.findByIdAndRecipientUserId(requestId, recipientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Share request not found with id: " + requestId));
        if (request.getStatus() != SubscriptionShareRequestStatus.PENDING) {
            throw new DuplicateResourceException("Only pending share requests can be changed");
        }
        return request;
    }
}
