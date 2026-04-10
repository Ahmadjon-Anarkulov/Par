package com.parlament.model;

/**
 * Tracks the current conversation state / checkout flow for a user.
 * Stored in memory per user session.
 */
public class UserSession {

    public enum State {
        IDLE,
        AWAITING_NAME,
        AWAITING_PHONE,
        AWAITING_ADDRESS
    }

    private final long userId;
    private State state;

    // Temporary checkout data collected during the flow
    private String checkoutName;
    private String checkoutPhone;
    private String checkoutAddress;

    public UserSession(long userId) {
        this.userId = userId;
        this.state = State.IDLE;
    }

    public void resetCheckout() {
        this.state = State.IDLE;
        this.checkoutName = null;
        this.checkoutPhone = null;
        this.checkoutAddress = null;
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
}
