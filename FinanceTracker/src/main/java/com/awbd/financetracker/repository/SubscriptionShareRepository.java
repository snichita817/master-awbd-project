package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.SubscriptionShare;
import com.awbd.financetracker.entity.SubscriptionShareId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionShareRepository extends JpaRepository<SubscriptionShare, SubscriptionShareId> {

    List<SubscriptionShare> findByIdSubscriptionId(Long subscriptionId);

    List<SubscriptionShare> findByIdUserId(Long userId);
}
