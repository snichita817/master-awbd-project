package com.awbd.financetracker.dto;

import com.awbd.financetracker.entity.*;

public final class FinanceCoreMapper {

    private FinanceCoreMapper() {
    }

    public static CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDto(category.getId(), category.getName(), category.getDescription(), category.getOwnerUserId());
    }

    public static PaymentMethodDto toDto(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }
        return new PaymentMethodDto(paymentMethod.getId(), paymentMethod.getType(), paymentMethod.getDetails(), paymentMethod.getOwnerUserId());
    }

    public static BudgetDto toDto(Budget budget) {
        return new BudgetDto(budget.getId(), budget.getMaxLimit(), budget.getCurrentSpending(), toDto(budget.getCategory()));
    }

    public static SubscriptionDto toDto(Subscription subscription) {
        return new SubscriptionDto(
                subscription.getId(),
                subscription.getName(),
                subscription.getPrice(),
                subscription.getBillingFrequency(),
                subscription.getRenewalDate(),
                subscription.getOwnerUserId(),
                toDto(subscription.getCategory()),
                toDto(subscription.getPaymentMethod())
        );
    }

    public static SubscriptionShareDto toDto(SubscriptionShare share) {
        return new SubscriptionShareDto(
                share.getId().getSubscriptionId(),
                share.getId().getUserId(),
                share.getPercentageShare(),
                share.getFixedAmount(),
                share.getAddedOn(),
                toDto(share.getSubscription())
        );
    }

    public static SubscriptionShareRequestDto toDto(SubscriptionShareRequest request) {
        return new SubscriptionShareRequestDto(
                request.getId(),
                toDto(request.getSubscription()),
                request.getRequestedByUserId(),
                request.getRecipientUserId(),
                request.getPercentageShare(),
                request.getFixedAmount(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getRespondedAt()
        );
    }

    public static TransactionDto toDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getSubscription().getId()
        );
    }
}
