package com.awbd.financetracker.controllers;

import com.awbd.financetracker.dto.FinanceCoreMapper;
import com.awbd.financetracker.dto.ShareRequestCreateDto;
import com.awbd.financetracker.dto.SubscriptionShareDto;
import com.awbd.financetracker.dto.SubscriptionShareRequestDto;
import com.awbd.financetracker.service.SubscriptionShareRequestService;
import com.awbd.financetracker.service.SubscriptionShareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ShareController {

    private final SubscriptionShareService subscriptionShareService;
    private final SubscriptionShareRequestService subscriptionShareRequestService;

    public ShareController(SubscriptionShareService subscriptionShareService,
                           SubscriptionShareRequestService subscriptionShareRequestService) {
        this.subscriptionShareService = subscriptionShareService;
        this.subscriptionShareRequestService = subscriptionShareRequestService;
    }

    @GetMapping("/shares/subscription/{subscriptionId}")
    public ResponseEntity<List<SubscriptionShareDto>> getSharesBySubscription(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionShareService.getSharesBySubscription(subscriptionId)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @GetMapping("/shares/participant/{participantUserId}")
    public ResponseEntity<List<SubscriptionShareDto>> getSharesByUser(@PathVariable Long participantUserId) {
        return ResponseEntity.ok(subscriptionShareService.getSharesByUser(participantUserId)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @GetMapping("/shares/owner/{ownerUserId}")
    public ResponseEntity<List<SubscriptionShareDto>> getSharesByOwner(@PathVariable Long ownerUserId) {
        return ResponseEntity.ok(subscriptionShareService.getSharesBySubscriptionOwner(ownerUserId)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @DeleteMapping("/shares/subscription/{subscriptionId}/participant/{participantUserId}")
    public ResponseEntity<Void> removeShare(@PathVariable Long subscriptionId, @PathVariable Long participantUserId) {
        subscriptionShareService.removeShare(subscriptionId, participantUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/share-requests/subscription/{subscriptionId}/owner/{requesterId}")
    public ResponseEntity<SubscriptionShareRequestDto> createRequest(@PathVariable Long subscriptionId,
                                                                     @PathVariable Long requesterId,
                                                                     @Valid @RequestBody ShareRequestCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinanceCoreMapper.toDto(subscriptionShareRequestService.createRequest(
                        subscriptionId, requesterId, request.recipientEmail(), request.percentageShare(), request.fixedAmount()
                )));
    }

    @GetMapping("/share-requests/subscription/{subscriptionId}")
    public ResponseEntity<List<SubscriptionShareRequestDto>> getRequestsForSubscription(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionShareRequestService.getRequestsForSubscription(subscriptionId)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @GetMapping("/share-requests/recipient/{recipientUserId}")
    public ResponseEntity<List<SubscriptionShareRequestDto>> getRequestsForRecipient(@PathVariable Long recipientUserId) {
        return ResponseEntity.ok(subscriptionShareRequestService.getPendingRequestsForUser(recipientUserId)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @PostMapping("/share-requests/{requestId}/accept/recipient/{recipientUserId}")
    public ResponseEntity<SubscriptionShareRequestDto> accept(@PathVariable Long requestId, @PathVariable Long recipientUserId) {
        return ResponseEntity.ok(FinanceCoreMapper.toDto(subscriptionShareRequestService.acceptRequest(requestId, recipientUserId)));
    }

    @PostMapping("/share-requests/{requestId}/decline/recipient/{recipientUserId}")
    public ResponseEntity<SubscriptionShareRequestDto> decline(@PathVariable Long requestId, @PathVariable Long recipientUserId) {
        return ResponseEntity.ok(FinanceCoreMapper.toDto(subscriptionShareRequestService.declineRequest(requestId, recipientUserId)));
    }

    @PostMapping("/share-requests/{requestId}/revoke/requester/{requesterId}")
    public ResponseEntity<Void> revoke(@PathVariable Long requestId, @PathVariable Long requesterId) {
        subscriptionShareRequestService.revokeRequest(requestId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
