package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(String name, String email, BigDecimal monthlyIncome);

    User registerUser(String name, String email, String rawPassword, BigDecimal monthlyIncome);

    Optional<User> getUserById(Long id);

    Optional<User> getUserByEmail(String email);

    List<User> getAllUsers();

    User updateUser(Long id, String name, String email, BigDecimal monthlyIncome);

    void deleteUser(Long id);

    boolean existsByEmail(String email);
}

