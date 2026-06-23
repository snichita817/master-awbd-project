package com.awbd.financetracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SubscriptionShareId implements Serializable {

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "user_id")
    private Long userId;

    public SubscriptionShareId() {
    }

    public SubscriptionShareId(Long subscriptionId, Long userId) {
        this.subscriptionId = subscriptionId;
        this.userId = userId;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionShareId that)) return false;
        return Objects.equals(subscriptionId, that.subscriptionId) &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId, userId);
    }
}
