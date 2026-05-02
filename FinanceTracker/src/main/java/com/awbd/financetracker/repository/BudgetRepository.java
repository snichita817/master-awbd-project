package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByCategoryId(Long categoryId);

    @Query("SELECT b FROM Budget b WHERE b.category.user.id = :userId")
    List<Budget> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Budget b WHERE b.currentSpending > b.maxLimit")
    List<Budget> findExceededBudgets();

    @Query("SELECT b FROM Budget b WHERE b.category.user.id = :userId AND b.currentSpending > b.maxLimit")
    List<Budget> findExceededBudgetsByUserId(@Param("userId") Long userId);
}

