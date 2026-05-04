package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.SubscriptionShareId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SubscriptionShareRepository extends JpaRepository<SubscriptionShare, SubscriptionShareId> {

    @EntityGraph(attributePaths = {"user"})
    List<SubscriptionShare> findByIdSubscriptionId(Long subscriptionId);

    @EntityGraph(attributePaths = {"subscription", "subscription.user", "subscription.category"})
    List<SubscriptionShare> findByIdUserId(Long userId);

    // All shares for subscriptions owned by the given user.
    @Query("SELECT s FROM SubscriptionShare s WHERE s.subscription.user.id = :ownerId")
    List<SubscriptionShare> findBySubscriptionOwnerId(@Param("ownerId") Long ownerId);

    // IDs of subscriptions owned by the given user that have at least one share.
    @Query("SELECT DISTINCT s.id.subscriptionId FROM SubscriptionShare s WHERE s.subscription.user.id = :ownerId")
    Set<Long> findSharedSubscriptionIdsByOwnerId(@Param("ownerId") Long ownerId);
}
