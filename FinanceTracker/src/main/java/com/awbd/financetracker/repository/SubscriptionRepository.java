package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.category LEFT JOIN FETCH s.paymentMethod WHERE s.user.id = :userId")
    List<Subscription> findByUserId(@Param("userId") Long userId);

    List<Subscription> findByCategoryId(Long categoryId);

    List<Subscription> findByPaymentMethodId(Long paymentMethodId);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.renewalDate BETWEEN :startDate AND :endDate")
    List<Subscription> findByUserIdAndRenewalDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find subscriptions due within the next 7 days for a user
    default List<Subscription> findUpcomingRenewals(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysFromNow = today.plusDays(7);
        return findByUserIdAndRenewalDateBetween(userId, today, sevenDaysFromNow);
    }

    // Find all subscriptions due within the next 7 days (all users)
    @Query("SELECT s FROM Subscription s WHERE s.renewalDate BETWEEN :startDate AND :endDate")
    List<Subscription> findAllUpcomingRenewals(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

