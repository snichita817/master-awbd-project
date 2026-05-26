package com.awbd.financetracker.dto;

import com.awbd.financetracker.enums.SubscriptionShareRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionShareRequestDto(Long id,
                                          SubscriptionDto subscription,
                                          Long requestedByUserId,
                                          Long recipientUserId,
                                          BigDecimal percentageShare,
                                          BigDecimal fixedAmount,
                                          SubscriptionShareRequestStatus status,
                                          LocalDateTime createdAt,
                                          LocalDateTime respondedAt) {
}
