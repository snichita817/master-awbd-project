package com.awbd.financetracker.controllers;

import com.awbd.financetracker.dto.FinanceCoreMapper;
import com.awbd.financetracker.dto.PageResponse;
import com.awbd.financetracker.dto.TransactionDto;
import com.awbd.financetracker.service.TransactionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/subscription/{subscriptionId}")
    public ResponseEntity<TransactionDto> createTransaction(@PathVariable Long subscriptionId,
                                                            @RequestParam(required = false) LocalDateTime transactionDate) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinanceCoreMapper.toDto(transactionService.createTransaction(
                        subscriptionId,
                        transactionDate != null ? transactionDate : LocalDateTime.now()
                )));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable Long id) {
        return transactionService.getTransactionById(id)
                .map(FinanceCoreMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<TransactionDto>> getTransactionsBySubscription(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(transactionService.getTransactionsBySubscriptionId(subscriptionId)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @GetMapping("/owner/{ownerUserId}")
    public ResponseEntity<PageResponse<TransactionDto>> getTransactionsByOwner(@PathVariable Long ownerUserId,
                                                                               @RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "10") int size,
                                                                               @RequestParam(defaultValue = "transactionDate") String sort,
                                                                               @RequestParam(defaultValue = "desc") String dir) {
        Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        var transactions = transactionService.getTransactionsByUserId(ownerUserId, PageRequest.of(page, size, Sort.by(direction, sort)))
                .map(FinanceCoreMapper::toDto);
        return ResponseEntity.ok(PageResponse.from(transactions));
    }
}
