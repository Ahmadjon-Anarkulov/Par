package com.parlament.model;

import java.math.BigDecimal;

/**
 * Represents a product in the Parlament store catalog.
 */
public class Product {

    private final String id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final String imageUrl;
    private final Category category;

    public Product(String id, String name, String description,
                   BigDecimal price, String imageUrl, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public Category getCategory() { return category; }

    /**
     * Returns a formatted price string, e.g. "$299.00"
     */
    public String getFormattedPrice() {
        return String.format("$%,.2f", price);
    }

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%s}", id, name, getFormattedPrice());
    }
}
