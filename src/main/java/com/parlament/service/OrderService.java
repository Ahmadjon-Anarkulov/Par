package com.parlament.service;

import com.parlament.model.CartItem;
import com.parlament.model.Category;
import com.parlament.model.Order;
import com.parlament.model.Product;
import com.parlament.persistence.entity.OrderEntity;
import com.parlament.persistence.entity.OrderItemEntity;
import com.parlament.persistence.repo.OrderJpaRepository;
import com.parlament.persistence.repo.TelegramUserJpaRepository;
import com.parlament.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages order creation and history for all users.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderJpaRepository orderRepo;
    private final NotificationService notificationService;
    private final TelegramUserJpaRepository telegramUserRepo;
    private final ProductRepository productRepo;

    public OrderService(OrderJpaRepository orderRepo,
                        NotificationService notificationService,
                        TelegramUserJpaRepository telegramUserRepo,
                        ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.notificationService = notificationService;
        this.telegramUserRepo = telegramUserRepo;
        this.productRepo = productRepo;
    }

    /**
     * Creates and persists a new order from the given cart items.
     */
    @Transactional
    public Order createOrder(long userId, List<CartItem> items,
                              String customerName, String phone, String address) {
        // Create domain object first (keeps existing formatting behavior)
        Order order = new Order(userId, items, customerName, phone, address);

        OrderEntity entity = new OrderEntity();
        entity.setOrderId(order.getOrderId());
        entity.setTelegramUserId(userId);
        entity.setCustomerName(order.getCustomerName());
        entity.setPhoneNumber(order.getPhoneNumber());
        entity.setDeliveryAddress(order.getDeliveryAddress());
        entity.setStatus(order.getStatus().name());
        entity.setCreatedAt(OffsetDateTime.now());

        List<OrderItemEntity> itemEntities = new ArrayList<>();
        for (CartItem item : items) {
            OrderItemEntity ie = new OrderItemEntity();
            ie.setOrder(entity);
            ie.setProductId(item.getProduct().getId());
            ie.setProductName(item.getProduct().getName());
            ie.setUnitPrice(item.getProduct().getPrice());
            ie.setQuantity(item.getQuantity());
            itemEntities.add(ie);
        }
        entity.setItems(itemEntities);

        OrderEntity saved = orderRepo.save(entity);

        BigDecimal totalAmount = items.stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ensure user exists in DB before notifications (best-effort)
        telegramUserRepo.findById(userId).orElse(null);
        notificationService.notifyAdminsNewOrder(saved, totalAmount);
        log.info("Created order {} for user {} — total: {}", order.getOrderId(), userId, order.getFormattedTotal());
        return order;
    }

    /**
     * Returns all orders for a specific user, newest first.
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(long userId) {
        return orderRepo.findByTelegramUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomain)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Returns a specific order by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<Order> findById(String orderId) {
        return orderRepo.findById(orderId).map(this::toDomain);
    }

    /**
     * Returns the total number of orders placed by a user.
     */
    @Transactional(readOnly = true)
    public int getOrderCount(long userId) {
        return (int) orderRepo.countByTelegramUserId(userId);
    }

    @Transactional
    public Optional<Order> setStatus(String orderId, Order.Status status) {
        return orderRepo.findById(orderId).map(entity -> {
            entity.setStatus(status.name());
            OrderEntity saved = orderRepo.save(entity);
            return toDomain(saved);
        });
    }

    private Order toDomain(OrderEntity entity) {
        List<CartItem> items = new ArrayList<>();
        if (entity.getItems() != null) {
            for (OrderItemEntity itemEntity : entity.getItems()) {
                Product p = productRepo.findById(itemEntity.getProductId())
                        .map(catalogProduct -> new Product(
                                catalogProduct.getId(),
                                itemEntity.getProductName(),
                                catalogProduct.getDescription(),
                                itemEntity.getUnitPrice(),
                                catalogProduct.getImageUrl(),
                                catalogProduct.getCategory()
                        ))
                        .orElseGet(() -> new Product(
                                itemEntity.getProductId(),
                                itemEntity.getProductName(),
                                "",
                                itemEntity.getUnitPrice(),
                                "",
                                Category.ACCESSORIES
                        ));
                items.add(new CartItem(p, itemEntity.getQuantity()));
            }
        }

        Order.Status status;
        try {
            status = Order.Status.valueOf(entity.getStatus());
        } catch (Exception ignored) {
            status = Order.Status.CONFIRMED;
        }

        return new Order(
                entity.getOrderId(),
                entity.getTelegramUserId(),
                items,
                entity.getCustomerName(),
                entity.getPhoneNumber(),
                entity.getDeliveryAddress(),
                entity.getCreatedAt().toLocalDateTime(),
                status
        );
    }
}
