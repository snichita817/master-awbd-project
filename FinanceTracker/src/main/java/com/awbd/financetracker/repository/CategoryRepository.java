package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    Page<Category> findByUserId(Long userId, Pageable pageable);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    Optional<Category> findByNameAndUserId(String name, Long userId);

    boolean existsByNameAndUserId(String name, Long userId);
}

