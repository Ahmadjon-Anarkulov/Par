package com.parlament.service;

import com.parlament.model.CartItem;
import com.parlament.model.Category;
import com.parlament.model.Product;
import com.parlament.persistence.entity.OrderEntity;
import com.parlament.persistence.repo.OrderJpaRepository;
import com.parlament.persistence.repo.TelegramUserJpaRepository;
import com.parlament.repository.ProductRepository;
import com.parlament.telegram.TelegramBotSender;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    @Test
    void createOrder_indexesByUser() {
        // NOTE: Mockito (ByteBuddy) is not compatible with Java 25 in this environment,
        // so we use dynamic proxies / stubs instead.
        OrderJpaRepository repo = (OrderJpaRepository) Proxy.newProxyInstance(
                OrderJpaRepository.class.getClassLoader(),
                new Class[]{OrderJpaRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "save" -> args[0];
                    case "countByTelegramUserId" -> 1L;
                    default -> null;
                }
        );

        TelegramUserJpaRepository telegramUserRepo = (TelegramUserJpaRepository) Proxy.newProxyInstance(
                TelegramUserJpaRepository.class.getClassLoader(),
                new Class[]{TelegramUserJpaRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByRole" -> List.of();
                    case "existsByRole" -> false;
                    case "findById" -> Optional.empty();
                    default -> null;
                }
        );

        com.parlament.config.AdminProperties adminProperties = new com.parlament.config.AdminProperties();
        AdminService adminService = new AdminService(telegramUserRepo, adminProperties);

        TelegramBotSender sender = new TelegramBotSender() {
            @Override public void sendText(org.telegram.telegrambots.meta.api.methods.send.SendMessage message) {}
            @Override public void sendPhoto(org.telegram.telegrambots.meta.api.methods.send.SendPhoto photo) {}
            @Override public void editMessage(org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText editMessage) {}
            @Override public void answerCallback(org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery answerCallbackQuery) {}
        };

        NotificationService notificationService = new NotificationService(sender, adminService, telegramUserRepo);

        Product p = new Product("p1", "Test", "desc", new BigDecimal("10.00"), "img", Category.SUITS);
        ProductRepository productRepo = (ProductRepository) Proxy.newProxyInstance(
                ProductRepository.class.getClassLoader(),
                new Class[]{ProductRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.of(p);
                    default -> null;
                }
        );

        OrderService service = new OrderService(repo, notificationService, telegramUserRepo, productRepo);
        long userId = 7L;

        List<CartItem> items = List.of(new CartItem(p));

        service.createOrder(userId, items, "John", "+7000", "Address");

        assertThat(service.getOrderCount(userId)).isEqualTo(1);
    }
}

