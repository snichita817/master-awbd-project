package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShareId;
import com.awbd.financetracker.entity.SubscriptionShareRequest;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.enums.SubscriptionShareRequestStatus;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
import com.awbd.financetracker.repository.SubscriptionShareRequestRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionShareRequestServiceTest {

    @Mock
    private SubscriptionShareRequestRepository requestRepository;

    @Mock
    private SubscriptionShareRepository shareRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionShareService subscriptionShareService;

        @Mock
        private CategoryRepository categoryRepository;

    private SubscriptionShareRequestServiceImpl requestService;
    private User owner;
    private User recipient;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        requestService = new SubscriptionShareRequestServiceImpl(
                                requestRepository, shareRepository, subscriptionRepository, userRepository,
                                subscriptionShareService, categoryRepository);

        owner = new User("Owner", "owner@example.com", new BigDecimal("5000.00"));
        owner.setId(1L);
        recipient = new User("Recipient", "recipient@example.com", new BigDecimal("3000.00"));
        recipient.setId(2L);
        subscription = new Subscription("Netflix", new BigDecimal("30.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(5), owner);
        subscription.setId(10L);
    }

    @Test
    void createRequest_happyPath_savesPendingRequest() {
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));
        when(shareRepository.existsById(new SubscriptionShareId(10L, 2L))).thenReturn(false);
        when(requestRepository.existsBySubscriptionIdAndRecipientIdAndStatus(
                10L, 2L, SubscriptionShareRequestStatus.PENDING)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionShareRequest result = requestService.createRequest(
                10L, 1L, "recipient@example.com", new BigDecimal("50.00"), null);

        assertThat(result.getSubscription()).isEqualTo(subscription);
        assertThat(result.getRequestedBy()).isEqualTo(owner);
        assertThat(result.getRecipient()).isEqualTo(recipient);
        assertThat(result.getStatus()).isEqualTo(SubscriptionShareRequestStatus.PENDING);
    }

    @Test
    void createRequest_recipientEmailDoesNotExist_throwsResourceNotFoundException() {
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.createRequest(
                10L, 1L, "missing@example.com", new BigDecimal("50.00"), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createRequest_recipientIsOwner_throwsDuplicateResourceException() {
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> requestService.createRequest(
                10L, 1L, "owner@example.com", new BigDecimal("50.00"), null))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createRequest_noShareValue_throwsResourceNotFoundException() {
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));

        assertThatThrownBy(() -> requestService.createRequest(10L, 1L, "recipient@example.com", null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createRequest_duplicatePendingRequest_throwsDuplicateResourceException() {
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));
        when(shareRepository.existsById(new SubscriptionShareId(10L, 2L))).thenReturn(false);
        when(requestRepository.existsBySubscriptionIdAndRecipientIdAndStatus(
                10L, 2L, SubscriptionShareRequestStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> requestService.createRequest(
                10L, 1L, "recipient@example.com", new BigDecimal("50.00"), null))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createRequest_activeShareAlreadyExists_throwsDuplicateResourceException() {
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));
        when(shareRepository.existsById(new SubscriptionShareId(10L, 2L))).thenReturn(true);

        assertThatThrownBy(() -> requestService.createRequest(
                10L, 1L, "recipient@example.com", new BigDecimal("50.00"), null))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createRequest_nonOwner_throwsAccessDeniedException() {
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> requestService.createRequest(
                10L, 2L, "recipient@example.com", new BigDecimal("50.00"), null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void acceptRequest_pendingRequest_createsActiveShareAndMarksAccepted() {
        SubscriptionShareRequest request = new SubscriptionShareRequest(
                subscription, owner, recipient, new BigDecimal("50.00"), null);
        request.setId(100L);
        request.setStatus(SubscriptionShareRequestStatus.PENDING);

        when(requestRepository.findByIdAndRecipientId(100L, 2L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionShareRequest result = requestService.acceptRequest(100L, 2L);

        verify(subscriptionShareService).assignShare(10L, 2L, new BigDecimal("50.00"), null);
        assertThat(result.getStatus()).isEqualTo(SubscriptionShareRequestStatus.ACCEPTED);
        assertThat(result.getRespondedAt()).isNotNull();
    }

    @Test
    void declineRequest_pendingRequest_marksDeclined() {
        SubscriptionShareRequest request = new SubscriptionShareRequest(
                subscription, owner, recipient, null, new BigDecimal("10.00"));
        request.setId(100L);
        request.setStatus(SubscriptionShareRequestStatus.PENDING);

        when(requestRepository.findByIdAndRecipientId(100L, 2L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionShareRequest result = requestService.declineRequest(100L, 2L);

        verifyNoInteractions(subscriptionShareService);
        assertThat(result.getStatus()).isEqualTo(SubscriptionShareRequestStatus.DECLINED);
        assertThat(result.getRespondedAt()).isNotNull();
    }

    @Test
    void acceptRequest_nonRecipient_throwsResourceNotFoundException() {
        when(requestRepository.findByIdAndRecipientId(100L, 3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.acceptRequest(100L, 3L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void declineRequest_acceptedRequest_throwsDuplicateResourceException() {
        SubscriptionShareRequest request = new SubscriptionShareRequest(
                subscription, owner, recipient, new BigDecimal("50.00"), null);
        request.setId(100L);
        request.setStatus(SubscriptionShareRequestStatus.ACCEPTED);
        when(requestRepository.findByIdAndRecipientId(100L, 2L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.declineRequest(100L, 2L))
                .isInstanceOf(DuplicateResourceException.class);
    }
}