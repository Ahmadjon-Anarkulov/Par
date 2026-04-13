package com.parlament.service;

import com.parlament.model.CartItem;
import com.parlament.model.Category;
import com.parlament.model.Product;
import com.parlament.persistence.entity.OrderEntity;
import com.parlament.persistence.repo.OrderJpaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Test
    void createOrder_indexesByUser() {
        OrderJpaRepository repo = mock(OrderJpaRepository.class);
        when(repo.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.countByTelegramUserId(7L)).thenReturn(1L);

        OrderService service = new OrderService(repo);
        long userId = 7L;

        Product p = new Product("p1", "Test", "desc", new BigDecimal("10.00"), "img", Category.SUITS);
        List<CartItem> items = List.of(new CartItem(p));

        service.createOrder(userId, items, "John", "+7000", "Address");

        assertThat(service.getOrderCount(userId)).isEqualTo(1);
    }
}

