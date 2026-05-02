package com.awbd.financetracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_shares")
public class SubscriptionShare {

    @EmbeddedId
    private SubscriptionShareId id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subscriptionId")
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @DecimalMin(value = "0.0", message = "Percentage share cannot be negative")
    @Column(name = "percentage_share")
    private BigDecimal percentageShare;

    @DecimalMin(value = "0.0", message = "Fixed amount cannot be negative")
    @Column(name = "fixed_amount")
    private BigDecimal fixedAmount;

    @Column(name = "added_on", nullable = false)
    private LocalDateTime addedOn;

    @PrePersist
    protected void onPrePersist() {
        if (addedOn == null) {
            addedOn = LocalDateTime.now();
        }
    }

    public SubscriptionShare() {
    }

    public SubscriptionShare(Subscription subscription, User user, BigDecimal percentageShare,
                              BigDecimal fixedAmount) {
        this.id = new SubscriptionShareId(subscription.getId(), user.getId());
        this.subscription = subscription;
        this.user = user;
        this.percentageShare = percentageShare;
        this.fixedAmount = fixedAmount;
    }

    public SubscriptionShareId getId() {
        return id;
    }

    public void setId(SubscriptionShareId id) {
        this.id = id;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(LocalDateTime addedOn) {
        this.addedOn = addedOn;
    }
}
