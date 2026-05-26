package com.awbd.financetracker.client;

import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.enums.SubscriptionShareRequestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class FinanceCoreClient {

    private final RestClient restClient;

    public FinanceCoreClient(RestClient.Builder builder,
                             @Value("${services.finance-core-url}") String financeCoreUrl) {
        this.restClient = builder.baseUrl(financeCoreUrl).build();
    }

    public PageResponse<CategoryDto> getCategories(Long ownerUserId, int page, int size, String sort, String dir) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/categories/owner/{ownerUserId}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("sort", sort)
                        .queryParam("dir", dir)
                        .build(ownerUserId))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public CategoryDto getCategory(Long id) {
        return restClient.get()
                .uri("/api/categories/{id}", id)
                .retrieve()
                .body(CategoryDto.class);
    }

    public CategoryDto createCategory(Long ownerUserId, CategoryUpsertDto request) {
        return restClient.post()
                .uri("/api/categories/owner/{ownerUserId}", ownerUserId)
                .body(request)
                .retrieve()
                .body(CategoryDto.class);
    }

    public CategoryDto updateCategory(Long id, CategoryUpsertDto request) {
        return restClient.put()
                .uri("/api/categories/{id}", id)
                .body(request)
                .retrieve()
                .body(CategoryDto.class);
    }

    public void deleteCategory(Long id) {
        restClient.delete()
                .uri("/api/categories/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public List<PaymentMethodDto> getPaymentMethods(Long ownerUserId) {
        return restClient.get()
                .uri("/api/payment-methods/owner/{ownerUserId}", ownerUserId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public PaymentMethodDto getPaymentMethod(Long id) {
        return restClient.get()
                .uri("/api/payment-methods/{id}", id)
                .retrieve()
                .body(PaymentMethodDto.class);
    }

    public PaymentMethodDto createPaymentMethod(Long ownerUserId, PaymentMethodUpsertDto request) {
        return restClient.post()
                .uri("/api/payment-methods/owner/{ownerUserId}", ownerUserId)
                .body(request)
                .retrieve()
                .body(PaymentMethodDto.class);
    }

    public PaymentMethodDto updatePaymentMethod(Long id, PaymentMethodUpsertDto request) {
        return restClient.put()
                .uri("/api/payment-methods/{id}", id)
                .body(request)
                .retrieve()
                .body(PaymentMethodDto.class);
    }

    public void deletePaymentMethod(Long id) {
        restClient.delete()
                .uri("/api/payment-methods/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public List<BudgetDto> getBudgets(Long ownerUserId) {
        return restClient.get()
                .uri("/api/budgets/owner/{ownerUserId}", ownerUserId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public BudgetDto getBudget(Long id) {
        return restClient.get()
                .uri("/api/budgets/{id}", id)
                .retrieve()
                .body(BudgetDto.class);
    }

    public BudgetDto createBudget(BudgetCreateDto request) {
        return restClient.post()
                .uri("/api/budgets")
                .body(request)
                .retrieve()
                .body(BudgetDto.class);
    }

    public BudgetDto updateBudget(Long id, BudgetUpdateDto request) {
        return restClient.put()
                .uri("/api/budgets/{id}", id)
                .body(request)
                .retrieve()
                .body(BudgetDto.class);
    }

    public void deleteBudget(Long id) {
        restClient.delete()
                .uri("/api/budgets/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public PageResponse<SubscriptionDto> getSubscriptions(Long ownerUserId, int page, int size, String sort, String dir) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/subscriptions/owner/{ownerUserId}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("sort", sort)
                        .queryParam("dir", dir)
                        .build(ownerUserId))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<SubscriptionDto> getUpcomingRenewals(Long ownerUserId) {
        return restClient.get()
                .uri("/api/subscriptions/owner/{ownerUserId}/upcoming-renewals", ownerUserId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public SubscriptionDto getSubscription(Long id) {
        return restClient.get()
                .uri("/api/subscriptions/{id}", id)
                .retrieve()
                .body(SubscriptionDto.class);
    }

    public SubscriptionDto createSubscription(Long ownerUserId, SubscriptionUpsertDto request) {
        return restClient.post()
                .uri("/api/subscriptions/owner/{ownerUserId}", ownerUserId)
                .body(request)
                .retrieve()
                .body(SubscriptionDto.class);
    }

    public SubscriptionDto updateSubscription(Long id, SubscriptionUpsertDto request) {
        return restClient.put()
                .uri("/api/subscriptions/{id}", id)
                .body(request)
                .retrieve()
                .body(SubscriptionDto.class);
    }

    public void deleteSubscription(Long id) {
        restClient.delete()
                .uri("/api/subscriptions/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public List<SubscriptionShareDto> getSharesBySubscription(Long subscriptionId) {
        return restClient.get()
                .uri("/api/shares/subscription/{subscriptionId}", subscriptionId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public void removeShare(Long subscriptionId, Long participantUserId) {
        restClient.delete()
                .uri("/api/shares/subscription/{subscriptionId}/participant/{participantUserId}", subscriptionId, participantUserId)
                .retrieve()
                .toBodilessEntity();
    }

    public SubscriptionShareRequestDto createShareRequest(Long subscriptionId, Long requesterId, ShareRequestCreateDto request) {
        return restClient.post()
                .uri("/api/share-requests/subscription/{subscriptionId}/owner/{requesterId}", subscriptionId, requesterId)
                .body(request)
                .retrieve()
                .body(SubscriptionShareRequestDto.class);
    }

    public List<SubscriptionShareRequestDto> getShareRequestsForSubscription(Long subscriptionId) {
        return restClient.get()
                .uri("/api/share-requests/subscription/{subscriptionId}", subscriptionId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<SubscriptionShareRequestDto> getShareRequestsForRecipient(Long recipientUserId) {
        return restClient.get()
                .uri("/api/share-requests/recipient/{recipientUserId}", recipientUserId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public void acceptShareRequest(Long requestId, Long recipientUserId) {
        restClient.post()
                .uri("/api/share-requests/{requestId}/accept/recipient/{recipientUserId}", requestId, recipientUserId)
                .retrieve()
                .toBodilessEntity();
    }

    public void declineShareRequest(Long requestId, Long recipientUserId) {
        restClient.post()
                .uri("/api/share-requests/{requestId}/decline/recipient/{recipientUserId}", requestId, recipientUserId)
                .retrieve()
                .toBodilessEntity();
    }

    public void revokeShareRequest(Long requestId, Long requesterId) {
        restClient.post()
                .uri("/api/share-requests/{requestId}/revoke/requester/{requesterId}", requestId, requesterId)
                .retrieve()
                .toBodilessEntity();
    }

    public record CategoryDto(Long id, String name, String description, Long ownerUserId) {
    }

    public record CategoryUpsertDto(String name, String description) {
    }

    public record PaymentMethodDto(Long id, PaymentType type, String details, Long ownerUserId) {
    }

    public record PaymentMethodUpsertDto(PaymentType type, String details) {
    }

    public record BudgetDto(Long id, java.math.BigDecimal maxLimit, java.math.BigDecimal currentSpending, CategoryDto category) {
    }

    public record BudgetCreateDto(Long categoryId, java.math.BigDecimal maxLimit) {
    }

    public record BudgetUpdateDto(java.math.BigDecimal maxLimit) {
    }

    public record SubscriptionDto(Long id,
                                  String name,
                                  java.math.BigDecimal price,
                                  BillingFrequency billingFrequency,
                                  java.time.LocalDate renewalDate,
                                  Long ownerUserId,
                                  CategoryDto category,
                                  PaymentMethodDto paymentMethod) {
    }

    public record SubscriptionUpsertDto(String name,
                                        java.math.BigDecimal price,
                                        BillingFrequency billingFrequency,
                                        java.time.LocalDate renewalDate,
                                        Long categoryId,
                                        Long paymentMethodId) {
    }

    public record SubscriptionShareDto(Long subscriptionId,
                                       Long participantUserId,
                                       java.math.BigDecimal percentageShare,
                                       java.math.BigDecimal fixedAmount,
                                       java.time.LocalDateTime addedOn,
                                       SubscriptionDto subscription) {
    }

    public record SubscriptionShareRequestDto(Long id,
                                              SubscriptionDto subscription,
                                              Long requestedByUserId,
                                              Long recipientUserId,
                                              java.math.BigDecimal percentageShare,
                                              java.math.BigDecimal fixedAmount,
                                              SubscriptionShareRequestStatus status,
                                              java.time.LocalDateTime createdAt,
                                              java.time.LocalDateTime respondedAt) {
    }

    public record ShareRequestCreateDto(String recipientEmail,
                                        java.math.BigDecimal percentageShare,
                                        java.math.BigDecimal fixedAmount) {
    }

    public record PageResponse<T>(
            List<T> content,
            int number,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last) {
    }
}
