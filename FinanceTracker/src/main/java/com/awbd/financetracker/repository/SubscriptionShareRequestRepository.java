package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.SubscriptionShareRequest;
import com.awbd.financetracker.enums.SubscriptionShareRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionShareRequestRepository extends JpaRepository<SubscriptionShareRequest, Long> {

    @EntityGraph(attributePaths = {"subscription", "subscription.user", "recipient", "requestedBy"})
    List<SubscriptionShareRequest> findByRecipientIdAndStatusOrderByCreatedAtDesc(
            Long recipientId, SubscriptionShareRequestStatus status);

    @EntityGraph(attributePaths = {"subscription", "subscription.user", "recipient", "requestedBy"})
    List<SubscriptionShareRequest> findByRequestedByIdOrderByCreatedAtDesc(Long requestedById);

    @EntityGraph(attributePaths = {"subscription", "subscription.user", "recipient", "requestedBy"})
    List<SubscriptionShareRequest> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);

    boolean existsBySubscriptionIdAndRecipientIdAndStatus(
            Long subscriptionId, Long recipientId, SubscriptionShareRequestStatus status);

    @EntityGraph(attributePaths = {"subscription", "subscription.user", "recipient", "requestedBy"})
    Optional<SubscriptionShareRequest> findByIdAndRecipientId(Long id, Long recipientId);

    @EntityGraph(attributePaths = {"subscription", "subscription.user", "recipient", "requestedBy"})
    Optional<SubscriptionShareRequest> findByIdAndRequestedById(Long id, Long requestedById);
}