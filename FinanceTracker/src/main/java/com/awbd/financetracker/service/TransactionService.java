package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionService {

    Transaction createTransaction(Long subscriptionId, LocalDateTime transactionDate);

    Optional<Transaction> getTransactionById(Long id);

    List<Transaction> getTransactionsBySubscriptionId(Long subscriptionId);

    List<Transaction> getTransactionsByUserId(Long userId);

    void deleteTransaction(Long id);
}

