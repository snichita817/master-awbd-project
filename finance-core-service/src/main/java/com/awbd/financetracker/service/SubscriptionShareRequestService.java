package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.SubscriptionShareRequest;

import java.math.BigDecimal;
import java.util.List;

public interface SubscriptionShareRequestService {

    SubscriptionShareRequest createRequest(Long subscriptionId, Long requesterId, String recipientEmail,
                                           BigDecimal percentageShare, BigDecimal fixedAmount);

    List<SubscriptionShareRequest> getPendingRequestsForUser(Long recipientUserId);

    List<SubscriptionShareRequest> getRequestsSentByUser(Long requestedByUserId);

    List<SubscriptionShareRequest> getRequestsForSubscription(Long subscriptionId);

    SubscriptionShareRequest acceptRequest(Long requestId, Long recipientUserId);

    SubscriptionShareRequest declineRequest(Long requestId, Long recipientUserId);

    void revokeRequest(Long requestId, Long requesterId);
}
