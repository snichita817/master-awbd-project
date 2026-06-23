package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.SubscriptionShareId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SubscriptionShareRepository extends JpaRepository<SubscriptionShare, SubscriptionShareId> {

    @EntityGraph(attributePaths = {"subscription", "subscription.category", "subscription.paymentMethod"})
    List<SubscriptionShare> findByIdSubscriptionId(Long subscriptionId);

    @EntityGraph(attributePaths = {"subscription", "subscription.category", "subscription.paymentMethod"})
    List<SubscriptionShare> findByIdUserId(Long userId);

    @EntityGraph(attributePaths = {"subscription", "subscription.category", "subscription.paymentMethod"})
    @Query("select s from SubscriptionShare s where s.subscription.ownerUserId = :ownerId")
    List<SubscriptionShare> findBySubscriptionOwnerId(@Param("ownerId") Long ownerId);

    @Query("select distinct s.id.subscriptionId from SubscriptionShare s where s.subscription.ownerUserId = :ownerId")
    Set<Long> findSharedSubscriptionIdsByOwnerId(@Param("ownerId") Long ownerId);

    void deleteByIdSubscriptionId(Long subscriptionId);
}
