package com.awbd.financetracker.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class UserDirectoryClient {

    private final RestClient restClient;

    public UserDirectoryClient(RestClient.Builder builder,
                               @Value("${services.user-service-url}") String userServiceUrl) {
        this.restClient = builder.baseUrl(userServiceUrl).build();
    }

    public UserSummary getUser(Long userId) {
        return restClient.get()
                .uri("/internal/users/{id}", userId)
                .retrieve()
                .body(UserSummary.class);
    }

    public record UserSummary(Long id, String name, String email, BigDecimal monthlyIncome) {
    }
}
