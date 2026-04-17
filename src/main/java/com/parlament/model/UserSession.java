package com.parlament.model;

/**
 * Tracks the current conversation state / checkout flow for a user.
 * Stored in DB via UserSessionEntity.
 */
public class UserSession {

    public enum State {
        IDLE,
        AWAITING_NAME,
        AWAITING_PHONE,
        AWAITING_COUNTRY,
        AWAITING_CITY,
        AWAITING_STREET,
        // Admin states
        ADMIN_BROADCAST_INPUT,
        ADMIN_CHANGE_STATUS_ORDER_ID,
        ADMIN_CHANGE_STATUS_VALUE
    }

    private final long userId;
    private State state;

    // Temporary checkout data
    private String checkoutName;
    private String checkoutPhone;
    private String checkoutAddress;

    // Partial address fields (assembled into checkoutAddress at the end)
    private String checkoutCountry;
    private String checkoutCity;

    // Admin temp data
    private String adminTempOrderId;

    public UserSession(long userId) {
        this.userId = userId;
        this.state = State.IDLE;
    }

    public void resetCheckout() {
        this.state = State.IDLE;
        this.checkoutName = null;
        this.checkoutPhone = null;
        this.checkoutAddress = null;
        this.checkoutCountry = null;
        this.checkoutCity = null;
        this.adminTempOrderId = null;
    }

    // --- Getters & Setters ---

    public long getUserId() { return userId; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public String getCheckoutName() { return checkoutName; }
    public void setCheckoutName(String checkoutName) { this.checkoutName = checkoutName; }

    public String getCheckoutPhone() { return checkoutPhone; }
    public void setCheckoutPhone(String checkoutPhone) { this.checkoutPhone = checkoutPhone; }

    public String getCheckoutAddress() { return checkoutAddress; }
    public void setCheckoutAddress(String checkoutAddress) { this.checkoutAddress = checkoutAddress; }

    public String getCheckoutCountry() { return checkoutCountry; }
    public void setCheckoutCountry(String checkoutCountry) { this.checkoutCountry = checkoutCountry; }

    public String getCheckoutCity() { return checkoutCity; }
    public void setCheckoutCity(String checkoutCity) { this.checkoutCity = checkoutCity; }

    public String getAdminTempOrderId() { return adminTempOrderId; }
    public void setAdminTempOrderId(String adminTempOrderId) { this.adminTempOrderId = adminTempOrderId; }
}
