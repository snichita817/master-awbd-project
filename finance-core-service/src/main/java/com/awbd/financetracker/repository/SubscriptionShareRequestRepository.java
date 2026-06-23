package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.SubscriptionShareRequest;
import com.awbd.financetracker.enums.SubscriptionShareRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionShareRequestRepository extends JpaRepository<SubscriptionShareRequest, Long> {

    @EntityGraph(attributePaths = {"subscription", "subscription.category", "subscription.paymentMethod"})
    List<SubscriptionShareRequest> findByRecipientUserIdAndStatusOrderByCreatedAtDesc(
            Long recipientUserId, SubscriptionShareRequestStatus status);

    @EntityGraph(attributePaths = {"subscription", "subscription.category", "subscription.paymentMethod"})
    List<SubscriptionShareRequest> findByRequestedByUserIdOrderByCreatedAtDesc(Long requestedByUserId);

    @EntityGraph(attributePaths = {"subscription", "subscription.category", "subscription.paymentMethod"})
    List<SubscriptionShareRequest> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);

    boolean existsBySubscriptionIdAndRecipientUserIdAndStatus(
            Long subscriptionId, Long recipientUserId, SubscriptionShareRequestStatus status);

    @EntityGraph(attributePaths = {"subscription", "subscription.category", "subscription.paymentMethod"})
    Optional<SubscriptionShareRequest> findByIdAndRecipientUserId(Long id, Long recipientUserId);

    @EntityGraph(attributePaths = {"subscription", "subscription.category", "subscription.paymentMethod"})
    Optional<SubscriptionShareRequest> findByIdAndRequestedByUserId(Long id, Long requestedByUserId);

    void deleteBySubscriptionId(Long subscriptionId);
}
