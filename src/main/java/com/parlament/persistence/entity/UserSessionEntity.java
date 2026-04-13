package com.parlament.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_session")
public class UserSessionEntity {

    @Id
    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "checkout_name")
    private String checkoutName;

    @Column(name = "checkout_phone")
    private String checkoutPhone;

    @Column(name = "checkout_address")
    private String checkoutAddress;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(Long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCheckoutName() {
        return checkoutName;
    }

    public void setCheckoutName(String checkoutName) {
        this.checkoutName = checkoutName;
    }

    public String getCheckoutPhone() {
        return checkoutPhone;
    }

    public void setCheckoutPhone(String checkoutPhone) {
        this.checkoutPhone = checkoutPhone;
    }

    public String getCheckoutAddress() {
        return checkoutAddress;
    }

    public void setCheckoutAddress(String checkoutAddress) {
        this.checkoutAddress = checkoutAddress;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

