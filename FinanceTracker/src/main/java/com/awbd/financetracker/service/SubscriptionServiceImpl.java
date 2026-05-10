package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetService budgetService;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                                   UserRepository userRepository,
                                   CategoryRepository categoryRepository,
                                   BudgetService budgetService) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.budgetService = budgetService;
    }

    @Override
    public Subscription createSubscription(Long userId, Long categoryId, Long paymentMethodId, Subscription subscription) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        subscription.setUser(user);

        if (categoryId != null) {
            var category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            subscription.setCategory(category);
        }

        if(paymentMethodId != null) {
            var paymentMethod = user.getPaymentMethods().stream()
                    .filter(x -> x.getId().equals(paymentMethodId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id: " + paymentMethodId));
            subscription.setPaymentMethod(paymentMethod);
        }

        Subscription saved = subscriptionRepository.save(subscription);

        // Update budget's currentSpending
        budgetService.addSubscriptionToBudget(saved.getCategory(), saved.getPrice(), saved.getBillingFrequency());

        log.info("Subscription created: id={}, name='{}', userId={}, price={}",
                saved.getId(), saved.getName(), userId, saved.getPrice());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return subscriptionRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getUpcomingRenewals(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return subscriptionRepository.findUpcomingRenewals(userId);
    }

    @Override
    public Subscription updateSubscription(Long id, Long categoryId, Long paymentMethodId, Subscription updatedSubscription) {
        Subscription existing = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        // Capture old values for budget adjustment
        Category oldCategory = existing.getCategory();
        BigDecimal oldPrice = existing.getPrice();
        var oldFrequency = existing.getBillingFrequency();

        // Apply updates
        existing.setName(updatedSubscription.getName());
        existing.setPrice(updatedSubscription.getPrice());
        existing.setBillingFrequency(updatedSubscription.getBillingFrequency());
        existing.setRenewalDate(updatedSubscription.getRenewalDate());

        // Update category
        Category newCategory = null;
        if (categoryId != null) {
            newCategory = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            existing.setCategory(newCategory);
        } else {
            existing.setCategory(null);
        }

        // Update payment method
        if (paymentMethodId != null) {
            var paymentMethod = existing.getUser().getPaymentMethods().stream()
                    .filter(x -> x.getId().equals(paymentMethodId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id: " + paymentMethodId));
            existing.setPaymentMethod(paymentMethod);
        }

        Subscription saved = subscriptionRepository.save(existing);

        // Update budgets: remove from old, add to new
        budgetService.removeSubscriptionFromBudget(oldCategory, oldPrice, oldFrequency);
        budgetService.addSubscriptionToBudget(saved.getCategory(), saved.getPrice(), saved.getBillingFrequency());

        log.info("Subscription updated: id={}, name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public void deleteSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        // Remove from budget BEFORE deleting
        budgetService.removeSubscriptionFromBudget(
                subscription.getCategory(),
                subscription.getPrice(),
                subscription.getBillingFrequency()
        );

        subscriptionRepository.deleteById(subscription.getId());
        log.info("Subscription deleted: id={}", id);
    }
}

