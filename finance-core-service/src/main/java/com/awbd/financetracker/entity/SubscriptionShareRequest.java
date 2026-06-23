package com.awbd.financetracker.entity;

import com.awbd.financetracker.enums.SubscriptionShareRequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_share_requests")
public class SubscriptionShareRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "requested_by_user_id", nullable = false)
    private Long requestedByUserId;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @DecimalMin(value = "0.0", message = "Percentage share cannot be negative")
    @Column(name = "percentage_share")
    private BigDecimal percentageShare;

    @DecimalMin(value = "0.0", message = "Fixed amount cannot be negative")
    @Column(name = "fixed_amount")
    private BigDecimal fixedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionShareRequestStatus status = SubscriptionShareRequestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = SubscriptionShareRequestStatus.PENDING;
        }
    }

    public SubscriptionShareRequest() {
    }

    public SubscriptionShareRequest(Subscription subscription, Long requestedByUserId, Long recipientUserId,
                                    BigDecimal percentageShare, BigDecimal fixedAmount) {
        this.subscription = subscription;
        this.requestedByUserId = requestedByUserId;
        this.recipientUserId = recipientUserId;
        this.percentageShare = percentageShare;
        this.fixedAmount = fixedAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public Long getRequestedByUserId() {
        return requestedByUserId;
    }

    public void setRequestedByUserId(Long requestedByUserId) {
        this.requestedByUserId = requestedByUserId;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public BigDecimal getPercentageShare() {
        return percentageShare;
    }

    public void setPercentageShare(BigDecimal percentageShare) {
        this.percentageShare = percentageShare;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public SubscriptionShareRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionShareRequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
