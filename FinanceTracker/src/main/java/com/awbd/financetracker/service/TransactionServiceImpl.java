package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.Transaction;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.TransactionRepository;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  SubscriptionRepository subscriptionRepository) {
        this.transactionRepository = transactionRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public Transaction createTransaction(Long subscriptionId, LocalDateTime transactionDate) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        Transaction transaction = new Transaction(subscription.getPrice(), transactionDate, subscription);

        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsBySubscriptionId(Long subscriptionId) {
        if (!subscriptionRepository.existsById(subscriptionId)){
            throw new ResourceNotFoundException("Subscription not found with id: " + subscriptionId);
        }

        return transactionRepository.findBySubscriptionId(subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    @Override
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        transactionRepository.delete(transaction);
    }
}


