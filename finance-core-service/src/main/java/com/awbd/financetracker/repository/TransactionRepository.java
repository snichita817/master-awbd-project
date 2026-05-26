package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySubscriptionId(Long subscriptionId);

    @Query("select t from Transaction t where t.subscription.ownerUserId = :ownerUserId")
    List<Transaction> findByUserId(@Param("ownerUserId") Long ownerUserId);

    @Query("select t from Transaction t where t.subscription.ownerUserId = :ownerUserId")
    Page<Transaction> findPageByUserId(@Param("ownerUserId") Long ownerUserId, Pageable pageable);
}
