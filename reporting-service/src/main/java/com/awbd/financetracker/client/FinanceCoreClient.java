package com.awbd.financetracker.client;

import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.enums.PaymentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class FinanceCoreClient {

    private final RestClient restClient;

    public FinanceCoreClient(RestClient.Builder builder,
                             @Value("${services.finance-core-url}") String financeCoreUrl) {
        this.restClient = builder.baseUrl(financeCoreUrl).build();
    }

    public List<SubscriptionDto> getSubscriptions(Long ownerUserId) {
        return restClient.get()
                .uri("/api/subscriptions/owner/{ownerUserId}/all", ownerUserId)
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

    public List<SubscriptionShareDto> getSharesByOwner(Long ownerUserId) {
        return restClient.get()
                .uri("/api/shares/owner/{ownerUserId}", ownerUserId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public record CategoryDto(Long id, String name, String description, Long ownerUserId) {
    }

    public record PaymentMethodDto(Long id, PaymentType type, String details, Long ownerUserId) {
    }

    public record SubscriptionDto(Long id,
                                  String name,
                                  BigDecimal price,
                                  BillingFrequency billingFrequency,
                                  LocalDate renewalDate,
                                  Long ownerUserId,
                                  CategoryDto category,
                                  PaymentMethodDto paymentMethod) {
    }

    public record SubscriptionShareDto(Long subscriptionId,
                                       Long participantUserId,
                                       BigDecimal percentageShare,
                                       BigDecimal fixedAmount,
                                       LocalDateTime addedOn,
                                       SubscriptionDto subscription) {
    }
}
