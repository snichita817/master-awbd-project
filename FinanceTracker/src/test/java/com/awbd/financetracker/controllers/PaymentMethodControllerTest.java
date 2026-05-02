package com.awbd.financetracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.service.PaymentMethodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentMethodController.class)
class PaymentMethodControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentMethodService paymentMethodService;

    private PaymentMethod paymentMethod;

    @BeforeEach
    void setUp(){
        paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);
        paymentMethod.setType(PaymentType.CREDIT_CARD);
        paymentMethod.setDetails("Visa ending in 1234");
    }

    @Test
    void createPaymentMethod_ShouldReturnCreatedPaymentMethod() throws Exception {

        when(paymentMethodService.createPaymentMethod(eq(1L), any(PaymentMethod.class)))
                .thenReturn(paymentMethod);

        mockMvc.perform(post("/api/payment-methods/user/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentMethod)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.details").value("Visa ending in 1234"));

        verify(paymentMethodService).createPaymentMethod(eq(1L), any(PaymentMethod.class));
    }

    @Test
    void getPaymentMethodById_ShouldReturnPaymentMethod() throws Exception {
        when(paymentMethodService.getPaymentMethodById(1L))
                .thenReturn(java.util.Optional.of(paymentMethod));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/payment-methods/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.details").value("Visa ending in 1234"));

        verify(paymentMethodService).getPaymentMethodById(1L);
    }
}