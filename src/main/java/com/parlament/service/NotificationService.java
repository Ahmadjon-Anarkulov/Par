package com.parlament.service;

import com.parlament.model.Order;
import com.parlament.persistence.entity.OrderEntity;
import com.parlament.persistence.entity.TelegramUserEntity;
import com.parlament.persistence.repo.TelegramUserJpaRepository;
import com.parlament.telegram.TelegramBotSender;
import com.parlament.util.KeyboardFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final TelegramBotSender sender;
    private final AdminService adminService;
    private final TelegramUserJpaRepository telegramUserRepo;

    public NotificationService(TelegramBotSender sender,
                               AdminService adminService,
                               TelegramUserJpaRepository telegramUserRepo) {
        this.sender = sender;
        this.adminService = adminService;
        this.telegramUserRepo = telegramUserRepo;
    }

    @Transactional(readOnly = true)
    public void notifyAdminsNewOrder(OrderEntity order, BigDecimal totalAmount) {
        Set<Long> adminIds = adminService.getAdminIds();
        if (adminIds.isEmpty()) {
            log.warn("Order {} created but there are no ADMIN users to notify.", order.getOrderId());
            return;
        }

        TelegramUserEntity customer = telegramUserRepo.findById(order.getTelegramUserId()).orElse(null);
        String customerLabel = formatCustomer(customer, order.getTelegramUserId());

        String text = buildNewOrderText(order, customerLabel, totalAmount);

        for (Long adminId : adminIds) {
            SendMessage msg = new SendMessage();
            msg.setChatId(adminId.toString());
            msg.setText(text);
            msg.setParseMode("HTML");
            msg.disableWebPagePreview();
            msg.setReplyMarkup(KeyboardFactory.adminOrderNotificationKeyboard(order.getOrderId()));
            sender.sendText(msg);
        }
    }

    private static String buildNewOrderText(OrderEntity order, String customerLabel, BigDecimal totalAmount) {
        String status;
        try {
            status = Order.Status.valueOf(order.getStatus()).getDisplayName();
        } catch (Exception ignored) {
            status = order.getStatus();
        }

        return "🛎 <b>Новый заказ</b>\n\n"
                + "🆔 <b>Номер:</b> <code>" + order.getOrderId() + "</code>\n"
                + "👤 <b>Покупатель:</b> " + customerLabel + "\n"
                + "💰 <b>Сумма:</b> <b>" + formatMoney(totalAmount) + "</b>\n"
                + "📌 <b>Статус:</b> " + status + "\n\n"
                + "<i>Используйте кнопки ниже для действий.</i>";
    }

    private static String formatCustomer(TelegramUserEntity customer, Long telegramUserId) {
        if (customer == null) return "<code>" + telegramUserId + "</code>";

        String username = customer.getUsername();
        if (username != null && !username.isBlank()) {
            if (!username.startsWith("@")) username = "@" + username;
            return username + " (<code>" + telegramUserId + "</code>)";
        }

        String name = (customer.getFirstName() == null ? "" : customer.getFirstName()).trim();
        if (!name.isBlank()) {
            return name + " (<code>" + telegramUserId + "</code>)";
        }

        return "<code>" + telegramUserId + "</code>";
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount);
    }
}

