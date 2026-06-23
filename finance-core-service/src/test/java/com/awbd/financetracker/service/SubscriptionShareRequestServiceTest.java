package com.awbd.financetracker.service;

import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.SubscriptionShareId;
import com.awbd.financetracker.entity.SubscriptionShareRequest;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.enums.SubscriptionShareRequestStatus;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.SubscriptionShareRepository;
import com.awbd.financetracker.repository.SubscriptionShareRequestRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionShareRequestServiceTest {

    @Mock
    private SubscriptionShareRequestRepository requestRepository;

    @Mock
    private SubscriptionShareRepository shareRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionShareService subscriptionShareService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserDirectoryClient userDirectoryClient;

    private SubscriptionShareRequestServiceImpl requestService;

    @BeforeEach
    void setUp() {
        requestService = new SubscriptionShareRequestServiceImpl(
                requestRepository,
                shareRepository,
                subscriptionRepository,
                subscriptionShareService,
                categoryRepository,
                userDirectoryClient
        );
    }

    @Test
    void createRequestStoresFixedAmountAndClearsPercentageWhenBothAreProvided() {
        Subscription subscription = subscription(20L, 7L, null);
        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(subscription));
        when(userDirectoryClient.requireUserByEmail("friend@example.com"))
                .thenReturn(new UserDirectoryClient.UserSummary(8L, "Friend", "friend@example.com", BigDecimal.ZERO));
        when(shareRepository.existsById(new SubscriptionShareId(20L, 8L))).thenReturn(false);
        when(requestRepository.existsBySubscriptionIdAndRecipientUserIdAndStatus(20L, 8L, SubscriptionShareRequestStatus.PENDING))
                .thenReturn(false);
        when(requestRepository.save(org.mockito.ArgumentMatchers.any(SubscriptionShareRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionShareRequest result = requestService.createRequest(
                20L,
                7L,
                "friend@example.com",
                new BigDecimal("50.00"),
                new BigDecimal("15.00")
        );

        assertThat(result.getSubscription()).isSameAs(subscription);
        assertThat(result.getRequestedByUserId()).isEqualTo(7L);
        assertThat(result.getRecipientUserId()).isEqualTo(8L);
        assertThat(result.getPercentageShare()).isNull();
        assertThat(result.getFixedAmount()).isEqualByComparingTo("15.00");
        assertThat(result.getStatus()).isEqualTo(SubscriptionShareRequestStatus.PENDING);
    }

    @Test
    void createRequestRejectsNonOwnerRequester() {
        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(subscription(20L, 7L, null)));

        assertThatThrownBy(() -> requestService.createRequest(
                20L,
                99L,
                "friend@example.com",
                new BigDecimal("50.00"),
                null
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("do not own");
    }

    @Test
    void createRequestRejectsSelfShare() {
        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(subscription(20L, 7L, null)));
        when(userDirectoryClient.requireUserByEmail("owner@example.com"))
                .thenReturn(new UserDirectoryClient.UserSummary(7L, "Owner", "owner@example.com", BigDecimal.ZERO));

        assertThatThrownBy(() -> requestService.createRequest(
                20L,
                7L,
                "owner@example.com",
                new BigDecimal("50.00"),
                null
        ))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("yourself");
    }

    @Test
    void createRequestRejectsExistingShareBeforeSavingRequest() {
        when(subscriptionRepository.findById(20L)).thenReturn(Optional.of(subscription(20L, 7L, null)));
        when(userDirectoryClient.requireUserByEmail("friend@example.com"))
                .thenReturn(new UserDirectoryClient.UserSummary(8L, "Friend", "friend@example.com", BigDecimal.ZERO));
        when(shareRepository.existsById(new SubscriptionShareId(20L, 8L))).thenReturn(true);

        assertThatThrownBy(() -> requestService.createRequest(
                20L,
                7L,
                "friend@example.com",
                new BigDecimal("50.00"),
                null
        ))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already has a share");
    }

    @Test
    void acceptRequestAssignsShareCopiesCategoryForRecipientAndMarksAccepted() {
        Category sourceCategory = category("Streaming");
        Subscription subscription = subscription(20L, 7L, sourceCategory);
        SubscriptionShareRequest request = request(30L, subscription, 7L, 8L, new BigDecimal("50.00"), null);

        when(requestRepository.findByIdAndRecipientUserId(30L, 8L)).thenReturn(Optional.of(request));
        when(categoryRepository.existsByNameAndOwnerUserId("Streaming", 8L)).thenReturn(false);
        when(requestRepository.save(request)).thenReturn(request);

        SubscriptionShareRequest result = requestService.acceptRequest(30L, 8L);

        verify(subscriptionShareService).assignShare(20L, 8L, new BigDecimal("50.00"), null);
        verify(categoryRepository).save(org.mockito.ArgumentMatchers.argThat(copy ->
                copy.getName().equals("Streaming")
                        && copy.getOwnerUserId().equals(8L)
                        && copy.getDescription().equals("Shared category")
        ));
        assertThat(result.getStatus()).isEqualTo(SubscriptionShareRequestStatus.ACCEPTED);
        assertThat(result.getRespondedAt()).isNotNull();
    }

    @Test
    void declineRequestMarksRequestDeclinedWithoutAssigningShare() {
        SubscriptionShareRequest request = request(30L, subscription(20L, 7L, null), 7L, 8L, null, new BigDecimal("15.00"));
        when(requestRepository.findByIdAndRecipientUserId(30L, 8L)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);

        SubscriptionShareRequest result = requestService.declineRequest(30L, 8L);

        assertThat(result.getStatus()).isEqualTo(SubscriptionShareRequestStatus.DECLINED);
        assertThat(result.getRespondedAt()).isNotNull();
    }

    @Test
    void revokeRequestDeletesOnlyPendingRequestOwnedByRequester() {
        SubscriptionShareRequest request = request(30L, subscription(20L, 7L, null), 7L, 8L, null, new BigDecimal("15.00"));
        when(requestRepository.findByIdAndRequestedByUserId(30L, 7L)).thenReturn(Optional.of(request));

        requestService.revokeRequest(30L, 7L);

        verify(requestRepository).delete(request);
    }

    @Test
    void revokeRequestRejectsAlreadyAnsweredRequest() {
        SubscriptionShareRequest request = request(30L, subscription(20L, 7L, null), 7L, 8L, null, new BigDecimal("15.00"));
        request.setStatus(SubscriptionShareRequestStatus.ACCEPTED);
        when(requestRepository.findByIdAndRequestedByUserId(30L, 7L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.revokeRequest(30L, 7L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("pending");
    }

    private static Category category(String name) {
        Category category = new Category();
        category.setId(3L);
        category.setName(name);
        category.setDescription("Shared category");
        category.setOwnerUserId(7L);
        return category;
    }

    private static Subscription subscription(Long id, Long ownerUserId, Category category) {
        Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setName("Netflix");
        subscription.setPrice(new BigDecimal("49.99"));
        subscription.setBillingFrequency(BillingFrequency.MONTHLY);
        subscription.setRenewalDate(LocalDate.now().plusDays(5));
        subscription.setOwnerUserId(ownerUserId);
        subscription.setCategory(category);
        return subscription;
    }

    private static SubscriptionShareRequest request(Long id,
                                                    Subscription subscription,
                                                    Long requesterId,
                                                    Long recipientId,
                                                    BigDecimal percentageShare,
                                                    BigDecimal fixedAmount) {
        SubscriptionShareRequest request = new SubscriptionShareRequest(
                subscription,
                requesterId,
                recipientId,
                percentageShare,
                fixedAmount
        );
        request.setId(id);
        request.setStatus(SubscriptionShareRequestStatus.PENDING);
        return request;
    }
}
