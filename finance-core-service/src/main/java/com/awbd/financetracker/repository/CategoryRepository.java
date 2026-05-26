package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByOwnerUserId(Long ownerUserId);

    Page<Category> findByOwnerUserId(Long ownerUserId, Pageable pageable);

    boolean existsByNameAndOwnerUserId(String name, Long ownerUserId);
}
