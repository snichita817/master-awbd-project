package com.awbd.financetracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.awbd.financetracker.entity.Transaction;
import com.awbd.financetracker.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    private Transaction transaction;

    @BeforeEach
    void setUp(){
        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("9.99"));
        transaction.setTransactionDate(LocalDateTime.of(2025, 1, 7, 10, 0));
    }

    @Test
    void createTransaction_ShouldReturnCreatedTransaction() throws Exception {
        when(transactionService.createTransaction(eq(1L), any(LocalDateTime.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/transactions/subscription/{subscriptionId}", 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(9.99));

        verify(transactionService).createTransaction(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void getTransactionById_ShouldReturnTransaction() throws Exception {
        when(transactionService.getTransactionById(1L))
                .thenReturn(java.util.Optional.of(transaction));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/transactions/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(9.99));

        verify(transactionService).getTransactionById(1L);
    }

    @Test
    void getTransactionById_WhenNotExists_ShouldReturn404() throws Exception {
        when(transactionService.getTransactionById(99L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/transactions/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(transactionService).getTransactionById(99L);
    }

    @Test
    void deleteTransaction_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/transactions/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteTransaction(1L);
    }
}