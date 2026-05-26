package com.awbd.financetracker.controllers;

import com.awbd.financetracker.dto.*;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/owner/{ownerUserId}")
    public ResponseEntity<SubscriptionDto> createSubscription(@PathVariable Long ownerUserId,
                                                              @Valid @RequestBody SubscriptionUpsertDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinanceCoreMapper.toDto(subscriptionService.createSubscription(
                        ownerUserId,
                        request.categoryId(),
                        request.paymentMethodId(),
                        toEntity(request)
                )));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDto> getSubscription(@PathVariable Long id) {
        return subscriptionService.getSubscriptionById(id)
                .map(FinanceCoreMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerUserId}")
    public ResponseEntity<PageResponse<SubscriptionDto>> getSubscriptionsByOwner(@PathVariable Long ownerUserId,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "5") int size,
                                                                                 @RequestParam(defaultValue = "name") String sort,
                                                                                 @RequestParam(defaultValue = "asc") String dir) {
        Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        var subscriptionPage = subscriptionService.getSubscriptionsByOwnerUserId(ownerUserId, PageRequest.of(page, size, Sort.by(direction, sort)))
                .map(FinanceCoreMapper::toDto);
        return ResponseEntity.ok(PageResponse.from(subscriptionPage));
    }

    @GetMapping("/owner/{ownerUserId}/all")
    public ResponseEntity<List<SubscriptionDto>> getAllSubscriptionsByOwner(@PathVariable Long ownerUserId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByOwnerUserId(ownerUserId)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @GetMapping("/owner/{ownerUserId}/upcoming-renewals")
    public ResponseEntity<List<SubscriptionDto>> getUpcomingRenewals(@PathVariable Long ownerUserId) {
        return ResponseEntity.ok(subscriptionService.getUpcomingRenewals(ownerUserId)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDto> updateSubscription(@PathVariable Long id,
                                                              @Valid @RequestBody SubscriptionUpsertDto request) {
        return ResponseEntity.ok(FinanceCoreMapper.toDto(subscriptionService.updateSubscription(
                id,
                request.categoryId(),
                request.paymentMethodId(),
                toEntity(request)
        )));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    private Subscription toEntity(SubscriptionUpsertDto request) {
        Subscription subscription = new Subscription();
        subscription.setName(request.name());
        subscription.setPrice(request.price());
        subscription.setBillingFrequency(request.billingFrequency());
        subscription.setRenewalDate(request.renewalDate());
        return subscription;
    }
}
