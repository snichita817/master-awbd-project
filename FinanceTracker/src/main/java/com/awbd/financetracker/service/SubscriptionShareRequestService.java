package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.SubscriptionShareRequest;

import java.math.BigDecimal;
import java.util.List;

public interface SubscriptionShareRequestService {

    SubscriptionShareRequest createRequest(Long subscriptionId, Long requesterId, String recipientEmail,
                                           BigDecimal percentageShare, BigDecimal fixedAmount);

    List<SubscriptionShareRequest> getPendingRequestsForUser(Long recipientId);

    List<SubscriptionShareRequest> getRequestsSentByUser(Long requesterId);

    List<SubscriptionShareRequest> getRequestsForSubscription(Long subscriptionId);

    SubscriptionShareRequest acceptRequest(Long requestId, Long recipientId);

    SubscriptionShareRequest declineRequest(Long requestId, Long recipientId);

    void revokeRequest(Long requestId, Long requesterId);
}