package com.awbd.financetracker.service;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.PaymentMethodRepository;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
import com.awbd.financetracker.repository.SubscriptionShareRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CategoryRepository categoryRepository;
    private final SubscriptionShareRepository subscriptionShareRepository;
    private final SubscriptionShareRequestRepository subscriptionShareRequestRepository;
    private final BudgetService budgetService;
    private final UserDirectoryClient userDirectoryClient;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                                   PaymentMethodRepository paymentMethodRepository,
                                   CategoryRepository categoryRepository,
                                   SubscriptionShareRepository subscriptionShareRepository,
                                   SubscriptionShareRequestRepository subscriptionShareRequestRepository,
                                   BudgetService budgetService,
                                   UserDirectoryClient userDirectoryClient) {
        this.subscriptionRepository = subscriptionRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.categoryRepository = categoryRepository;
        this.subscriptionShareRepository = subscriptionShareRepository;
        this.subscriptionShareRequestRepository = subscriptionShareRequestRepository;
        this.budgetService = budgetService;
        this.userDirectoryClient = userDirectoryClient;
    }

    @Override
    public Subscription createSubscription(Long ownerUserId, Long categoryId, Long paymentMethodId, Subscription subscription) {
        userDirectoryClient.requireUser(ownerUserId);
        subscription.setOwnerUserId(ownerUserId);
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .filter(found -> found.getOwnerUserId().equals(ownerUserId))
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            subscription.setCategory(category);
        }
        if (paymentMethodId != null) {
            var paymentMethod = paymentMethodRepository.findByIdAndOwnerUserId(paymentMethodId, ownerUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id: " + paymentMethodId));
            subscription.setPaymentMethod(paymentMethod);
        }
        Subscription saved = subscriptionRepository.save(subscription);
        budgetService.addSubscriptionToBudget(saved.getCategory(), saved.getPrice(), saved.getBillingFrequency());
        log.info("Subscription created: id={}, name='{}', ownerUserId={}", saved.getId(), saved.getName(), ownerUserId);
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
    public List<Subscription> getSubscriptionsByOwnerUserId(Long ownerUserId) {
        userDirectoryClient.requireUser(ownerUserId);
        List<Subscription> subscriptions = subscriptionRepository.findByOwnerUserId(ownerUserId);
        subscriptions.forEach(this::advanceRenewalDateIfPast);
        return subscriptions;
    }

    @Override
    public Page<Subscription> getSubscriptionsByOwnerUserId(Long ownerUserId, Pageable pageable) {
        userDirectoryClient.requireUser(ownerUserId);
        return subscriptionRepository.findPageByOwnerUserId(ownerUserId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getUpcomingRenewals(Long ownerUserId) {
        userDirectoryClient.requireUser(ownerUserId);
        LocalDate today = LocalDate.now();
        return subscriptionRepository.findUpcomingRenewals(ownerUserId, today, today.plusDays(7));
    }

    @Override
    public Subscription updateSubscription(Long id, Long categoryId, Long paymentMethodId, Subscription updatedSubscription) {
        Subscription existing = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        Category oldCategory = existing.getCategory();
        BigDecimal oldPrice = existing.getPrice();
        var oldFrequency = existing.getBillingFrequency();

        existing.setName(updatedSubscription.getName());
        existing.setPrice(updatedSubscription.getPrice());
        existing.setBillingFrequency(updatedSubscription.getBillingFrequency());
        existing.setRenewalDate(updatedSubscription.getRenewalDate());

        if (categoryId != null) {
            Category newCategory = categoryRepository.findById(categoryId)
                    .filter(found -> found.getOwnerUserId().equals(existing.getOwnerUserId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            existing.setCategory(newCategory);
        } else {
            existing.setCategory(null);
        }

        if (paymentMethodId != null) {
            var paymentMethod = paymentMethodRepository.findByIdAndOwnerUserId(paymentMethodId, existing.getOwnerUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id: " + paymentMethodId));
            existing.setPaymentMethod(paymentMethod);
        } else {
            existing.setPaymentMethod(null);
        }

        Subscription saved = subscriptionRepository.save(existing);
        budgetService.removeSubscriptionFromBudget(oldCategory, oldPrice, oldFrequency);
        budgetService.addSubscriptionToBudget(saved.getCategory(), saved.getPrice(), saved.getBillingFrequency());
        log.info("Subscription updated: id={}, name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public void deleteSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        budgetService.removeSubscriptionFromBudget(subscription.getCategory(), subscription.getPrice(), subscription.getBillingFrequency());
        subscriptionShareRequestRepository.deleteBySubscriptionId(id);
        subscriptionShareRepository.deleteByIdSubscriptionId(id);
        subscriptionRepository.delete(subscription);
        log.info("Subscription deleted: id={}", id);
    }

    private void advanceRenewalDateIfPast(Subscription subscription) {
        LocalDate today = LocalDate.now();
        if (subscription.getRenewalDate() != null && subscription.getRenewalDate().isBefore(today)) {
            LocalDate renewalDate = subscription.getRenewalDate();
            while (renewalDate.isBefore(today)) {
                renewalDate = switch (subscription.getBillingFrequency()) {
                    case MONTHLY -> renewalDate.plusMonths(1);
                    case YEARLY -> renewalDate.plusYears(1);
                };
            }
            subscription.setRenewalDate(renewalDate);
            subscriptionRepository.save(subscription);
        }
    }
}
