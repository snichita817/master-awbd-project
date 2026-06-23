package com.awbd.financetracker.client;

import com.awbd.financetracker.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
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

    public UserSummary requireUser(Long userId) {
        return restClient.get()
                .uri("/internal/users/{id}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ResourceNotFoundException("User not found with id: " + userId);
                })
                .body(UserSummary.class);
    }

    public UserSummary requireUserByEmail(String email) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/users/by-email")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ResourceNotFoundException("No user found with email: " + email);
                })
                .body(UserSummary.class);
    }

    public record UserSummary(Long id, String name, String email, BigDecimal monthlyIncome) {
    }
}
