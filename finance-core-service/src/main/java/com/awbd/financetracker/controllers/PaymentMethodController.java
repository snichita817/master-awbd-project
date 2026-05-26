package com.awbd.financetracker.controllers;

import com.awbd.financetracker.dto.FinanceCoreMapper;
import com.awbd.financetracker.dto.PaymentMethodDto;
import com.awbd.financetracker.dto.PaymentMethodUpsertDto;
import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.service.PaymentMethodService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @PostMapping("/owner/{ownerUserId}")
    public ResponseEntity<PaymentMethodDto> createPaymentMethod(@PathVariable Long ownerUserId,
                                                                @Valid @RequestBody PaymentMethodUpsertDto request) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setType(request.type());
        paymentMethod.setDetails(request.details());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinanceCoreMapper.toDto(paymentMethodService.createPaymentMethod(ownerUserId, paymentMethod)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodDto> getPaymentMethod(@PathVariable Long id) {
        return paymentMethodService.getPaymentMethodById(id)
                .map(FinanceCoreMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerUserId}")
    public ResponseEntity<List<PaymentMethodDto>> getByOwner(@PathVariable Long ownerUserId) {
        return ResponseEntity.ok(paymentMethodService.getPaymentMethodsByOwnerUserId(ownerUserId)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @GetMapping("/owner/{ownerUserId}/type/{type}")
    public ResponseEntity<List<PaymentMethodDto>> getByOwnerAndType(@PathVariable Long ownerUserId,
                                                                    @PathVariable PaymentType type) {
        return ResponseEntity.ok(paymentMethodService.getPaymentMethodsByOwnerUserIdAndType(ownerUserId, type)
                .stream().map(FinanceCoreMapper::toDto).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethodDto> updatePaymentMethod(@PathVariable Long id,
                                                                @Valid @RequestBody PaymentMethodUpsertDto request) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setType(request.type());
        paymentMethod.setDetails(request.details());
        return ResponseEntity.ok(FinanceCoreMapper.toDto(paymentMethodService.updatePaymentMethod(id, paymentMethod)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long id) {
        paymentMethodService.deletePaymentMethod(id);
        return ResponseEntity.noContent().build();
    }
}
