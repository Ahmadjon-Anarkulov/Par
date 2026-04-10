package com.parlament.model;

import java.math.BigDecimal;

/**
 * Represents a single item in a user's shopping cart.
 */
public class CartItem {

    private final Product product;
    private int quantity;

    public CartItem(Product product) {
        this.product = product;
        this.quantity = 1;
    }

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public void incrementQuantity() { this.quantity++; }
    public void decrementQuantity() { if (this.quantity > 0) this.quantity--; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /**
     * Returns the total price for this cart line (price × quantity).
     */
    public BigDecimal getTotalPrice() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Returns a formatted subtotal string.
     */
    public String getFormattedTotalPrice() {
        return String.format("$%,.2f", getTotalPrice());
    }

    @Override
    public String toString() {
        return String.format("CartItem{product='%s', qty=%d, total=%s}",
                product.getName(), quantity, getFormattedTotalPrice());
    }
}
