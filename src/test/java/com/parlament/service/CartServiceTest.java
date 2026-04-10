package com.parlament.service;

import com.parlament.model.Product;
import com.parlament.model.Category;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CartServiceTest {

    @Test
    void addToCart_incrementsQuantityForSameProduct() {
        CartService cartService = new CartService();
        long userId = 1L;

        Product p = new Product("p1", "Test", "desc", new BigDecimal("10.00"), "img", Category.SUITS);

        cartService.addToCart(userId, p);
        cartService.addToCart(userId, p);

        assertThat(cartService.getCartItems(userId)).hasSize(1);
        assertThat(cartService.getCartItems(userId).getFirst().getQuantity()).isEqualTo(2);
        assertThat(cartService.getCartTotal(userId)).isEqualByComparingTo("20.00");
    }
}

