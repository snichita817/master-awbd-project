package com.awbd.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionShareDto(Long subscriptionId,
                                   Long participantUserId,
                                   BigDecimal percentageShare,
                                   BigDecimal fixedAmount,
                                   LocalDateTime addedOn,
                                   SubscriptionDto subscription) {
}
