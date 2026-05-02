package com.awbd.financetracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubscriptionService subscriptionService;

    private Subscription subscription;

    private User testUser;

    @BeforeEach
    void setUp(){
        subscription = new Subscription();
        subscription.setId(1L);
        subscription.setName("Netflix Basic");
        subscription.setPrice(new BigDecimal("9.99"));
        subscription.setBillingFrequency(BillingFrequency.MONTHLY);

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@email.com");
        BigDecimal income = new BigDecimal("5000.00");
        testUser.setMonthlyIncome(income);
    }

    @Test
    void createSubscription_ShouldReturnCreatedSubscription() throws Exception {
        Subscription sub = new Subscription("Netflix Basic", new BigDecimal("9.99"), BillingFrequency.MONTHLY, LocalDate.now(), testUser);
        when(subscriptionService.createSubscription(eq(1L), eq(1L), eq(1L), any(Subscription.class)))
                .thenReturn(subscription);

        mockMvc.perform(post("/api/subscriptions/user/{userId}?categoryId={categoryId}&paymentMethodId={paymentMethodId}", 1L, 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sub)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Netflix Basic"))
                .andExpect(jsonPath("$.price").value(9.99));

        verify(subscriptionService).createSubscription(eq(1L), eq(1L), eq(1L), any(Subscription.class));
    }

    @Test
    void updatingSubscription_WithInvalidBillingFrequency_ShouldReturnBadRequest() throws Exception {
        Subscription sub = new Subscription("Spotify Premium", new BigDecimal("14.99"), null, LocalDate.now(), testUser);

        mockMvc.perform(post("/api/subscriptions/user/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sub)))
                .andExpect(status().isBadRequest());

        verify(subscriptionService, never()).createSubscription(anyLong(), any(), any(), any(Subscription.class));
    }
}