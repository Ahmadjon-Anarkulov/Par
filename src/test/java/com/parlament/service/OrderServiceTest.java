package com.parlament.service;

import com.parlament.model.CartItem;
import com.parlament.model.Category;
import com.parlament.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    @Test
    void createOrder_indexesByUser() {
        OrderService service = new OrderService();
        long userId = 7L;

        Product p = new Product("p1", "Test", "desc", new BigDecimal("10.00"), "img", Category.SUITS);
        List<CartItem> items = List.of(new CartItem(p));

        service.createOrder(userId, items, "John", "+7000", "Address");

        assertThat(service.getOrdersForUser(userId)).hasSize(1);
        assertThat(service.getOrderCount(userId)).isEqualTo(1);
    }
}

