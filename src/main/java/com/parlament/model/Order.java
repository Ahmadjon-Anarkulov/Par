package com.parlament.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order {

    public enum Status {
        PENDING("⏳ Ожидает"),
        CONFIRMED("✅ Подтверждён"),
        PROCESSING("🔄 В обработке"),
        SHIPPED("🚚 Отправлен"),
        DELIVERED("📦 Доставлен"),
        CANCELLED("❌ Отменён");

        private final String displayName;
        Status(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private final String orderId;
    private final long userId;
    private final List<CartItem> items;
    private final String customerName;
    private final String phoneNumber;
    private final String deliveryAddress;
    private final LocalDateTime createdAt;
    private Status status;

    public Order(long userId, List<CartItem> items,
                 String customerName, String phoneNumber, String deliveryAddress) {
        this.orderId = generateOrderId();
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.deliveryAddress = deliveryAddress;
        this.createdAt = LocalDateTime.now();
        this.status = Status.CONFIRMED;
    }

    private String generateOrderId() {
        return "PRL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getFormattedTotal() {
        return String.format("$%,.2f", getTotalAmount());
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm"));
    }

    public String getOrderId() { return orderId; }
    public long getUserId() { return userId; }
    public List<CartItem> getItems() { return items; }
    public String getCustomerName() { return customerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
