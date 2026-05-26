package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("select b from Budget b join fetch b.category where b.id = :id")
    Optional<Budget> findByIdWithCategory(@Param("id") Long id);

    Optional<Budget> findByCategoryId(Long categoryId);

    @Query("select b from Budget b join fetch b.category c where c.ownerUserId = :ownerUserId")
    List<Budget> findByOwnerUserId(@Param("ownerUserId") Long ownerUserId);
}
