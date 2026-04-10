package com.parlament.service;

import com.parlament.model.CartItem;
import com.parlament.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages order creation and history for all users.
 * In-memory storage using ConcurrentHashMap.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // Map: orderId → Order (global order registry)
    private final Map<String, Order> allOrders = new ConcurrentHashMap<>();

    // Map: userId → list of order IDs (for per-user history lookup)
    private final Map<Long, List<String>> userOrderIndex = new ConcurrentHashMap<>();

    /**
     * Creates and persists a new order from the given cart items.
     */
    public Order createOrder(long userId, List<CartItem> items,
                              String customerName, String phone, String address) {
        Order order = new Order(userId, items, customerName, phone, address);

        allOrders.put(order.getOrderId(), order);
        userOrderIndex.computeIfAbsent(userId, id -> new ArrayList<>())
                      .add(order.getOrderId());

        log.info("Created order {} for user {} — total: {}", order.getOrderId(), userId, order.getFormattedTotal());
        return order;
    }

    /**
     * Returns all orders for a specific user, newest first.
     */
    public List<Order> getOrdersForUser(long userId) {
        List<String> orderIds = userOrderIndex.get(userId);
        if (orderIds == null || orderIds.isEmpty()) return Collections.emptyList();

        // Collect and sort newest first
        return orderIds.stream()
                .map(allOrders::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns a specific order by its ID.
     */
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(allOrders.get(orderId));
    }

    /**
     * Returns the total number of orders placed by a user.
     */
    public int getOrderCount(long userId) {
        List<String> orderIds = userOrderIndex.get(userId);
        return orderIds == null ? 0 : orderIds.size();
    }
}
