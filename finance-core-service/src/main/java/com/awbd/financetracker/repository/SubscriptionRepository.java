package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Override
    @EntityGraph(attributePaths = {"category", "paymentMethod"})
    Optional<Subscription> findById(Long id);

    @EntityGraph(attributePaths = {"category", "paymentMethod"})
    List<Subscription> findByOwnerUserId(Long ownerUserId);

    @EntityGraph(attributePaths = {"category", "paymentMethod"})
    @Query("select s from Subscription s where s.ownerUserId = :ownerUserId")
    Page<Subscription> findPageByOwnerUserId(@Param("ownerUserId") Long ownerUserId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "paymentMethod"})
    Optional<Subscription> findByIdAndOwnerUserId(Long id, Long ownerUserId);

    @EntityGraph(attributePaths = {"category", "paymentMethod"})
    @Query("""
        select s from Subscription s
        where s.ownerUserId = :ownerUserId
          and s.renewalDate between :today and :until
        order by s.renewalDate asc
        """)
    List<Subscription> findUpcomingRenewals(@Param("ownerUserId") Long ownerUserId,
                                            @Param("today") LocalDate today,
                                            @Param("until") LocalDate until);
}
