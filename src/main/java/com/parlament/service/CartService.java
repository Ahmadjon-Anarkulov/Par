package com.parlament.service;

import com.parlament.model.CartItem;
import com.parlament.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages shopping carts for all users.
 * Thread-safe implementation using ConcurrentHashMap with synchronized inner maps.
 */
@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    // Map: userId → (productId → CartItem)
    // Inner maps are wrapped with synchronizedMap to prevent concurrent modification
    private final Map<Long, Map<String, CartItem>> carts = new ConcurrentHashMap<>();

    /**
     * Adds a product to the user's cart, or increments quantity if already present.
     */
    public void addToCart(long userId, Product product) {
        // Use computeIfAbsent with a synchronized map to ensure thread safety on inner map
        Map<String, CartItem> cart = carts.computeIfAbsent(userId,
                id -> Collections.synchronizedMap(new LinkedHashMap<>()));
        synchronized (cart) {
            CartItem existing = cart.get(product.getId());
            if (existing != null) {
                existing.incrementQuantity();
                log.debug("Incremented qty for product {} in cart of user {}", product.getId(), userId);
            } else {
                cart.put(product.getId(), new CartItem(product));
                log.debug("Added product {} to cart of user {}", product.getId(), userId);
            }
        }
    }

    /**
     * Removes a product entirely from the user's cart.
     */
    public void removeFromCart(long userId, String productId) {
        Map<String, CartItem> cart = carts.get(userId);
        if (cart != null) {
            synchronized (cart) {
                cart.remove(productId);
            }
            log.debug("Removed product {} from cart of user {}", productId, userId);
        }
    }

    /**
     * Returns all items in the user's cart. Returns empty list if cart is empty.
     */
    public List<CartItem> getCartItems(long userId) {
        Map<String, CartItem> cart = carts.get(userId);
        if (cart == null || cart.isEmpty()) return Collections.emptyList();
        synchronized (cart) {
            return new ArrayList<>(cart.values());
        }
    }

    /**
     * Calculates the total value of the cart.
     */
    public BigDecimal getCartTotal(long userId) {
        return getCartItems(userId).stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getFormattedCartTotal(long userId) {
        return String.format("$%,.2f", getCartTotal(userId));
    }

    /**
     * Returns how many distinct items are in the cart.
     */
    public int getCartSize(long userId) {
        Map<String, CartItem> cart = carts.get(userId);
        return cart == null ? 0 : cart.size();
    }

    /**
     * Checks if the user's cart is empty.
     */
    public boolean isCartEmpty(long userId) {
        return getCartSize(userId) == 0;
    }

    /**
     * Clears all items from the user's cart (e.g., after checkout).
     */
    public void clearCart(long userId) {
        carts.remove(userId);
        log.debug("Cleared cart for user {}", userId);
    }

    /**
     * Returns a snapshot of the cart items (deep copy) for order processing.
     */
    public List<CartItem> getCartSnapshot(long userId) {
        return new ArrayList<>(getCartItems(userId));
    }
}
