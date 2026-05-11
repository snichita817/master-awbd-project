package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySubscriptionId(Long subscriptionId);

    @EntityGraph(attributePaths = {"subscription"})
    @Query(value = "SELECT t FROM Transaction t WHERE t.subscription.user.id = :userId",
           countQuery = "SELECT COUNT(t) FROM Transaction t WHERE t.subscription.user.id = :userId")
    Page<Transaction> findPageByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.subscription WHERE t.subscription.user.id = :userId")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.subscription.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.subscription.category.id = :categoryId")
    List<Transaction> findByCategoryId(@Param("categoryId") Long categoryId);
}

