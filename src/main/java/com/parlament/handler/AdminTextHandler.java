package com.parlament.handler;

import com.parlament.model.Order;
import com.parlament.model.UserSession;
import com.parlament.service.AdminService;
import com.parlament.service.OrderService;
import com.parlament.service.SessionService;
import com.parlament.telegram.TelegramBotSender;
import com.parlament.util.KeyboardFactory;
import com.parlament.util.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

/**
 * Handles text input that originates from admin-panel flows
 * (broadcast message input, order status change, etc.).
 */
@Component
public class AdminTextHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminTextHandler.class);

    private final AdminService adminService;
    private final OrderService orderService;
    private final SessionService sessionService;

    public AdminTextHandler(AdminService adminService,
                            OrderService orderService,
                            SessionService sessionService) {
        this.adminService = adminService;
        this.orderService = orderService;
        this.sessionService = sessionService;
    }

    public void handle(Update update, TelegramBotSender sender, UserSession session) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();
        String input = message.getText().trim();

        if (!adminService.isAdmin(userId)) {
            sessionService.resetCheckout(userId);
            sender.sendText(buildMessage(chatId, "⛔ Доступ запрещён."));
            return;
        }

        switch (session.getState()) {

            case ADMIN_BROADCAST_INPUT -> {
                if (input.length() < 2) {
                    sender.sendText(buildMessage(chatId,
                            "⚠️ Сообщение слишком короткое. Введите текст рассылки (или /cancel для отмены):"));
                    return;
                }
                int sent = adminService.broadcastMessage(input, sender);
                sessionService.resetCheckout(userId);
                sender.sendText(buildMessage(chatId,
                        "✅ Рассылка завершена. Отправлено: <b>" + sent + "</b> пользователям."));
                sender.sendText(buildAdminMenuMessage(chatId));
            }

            case ADMIN_CHANGE_STATUS_ORDER_ID -> {
                // User entered order ID — now ask for status
                String orderId = input.toUpperCase();
                Optional<Order> orderOpt = orderService.findById(orderId);
                if (orderOpt.isEmpty()) {
                    sender.sendText(buildMessage(chatId,
                            "⚠️ Заказ <code>" + orderId + "</code> не найден.\n\n"
                            + "Введите корректный номер заказа (например: <code>PRL-A1B2C3D4</code>)\n"
                            + "или /cancel для отмены:"));
                    return;
                }
                sessionService.setAdminTempOrderId(userId, orderId);
                sessionService.setState(userId, UserSession.State.ADMIN_CHANGE_STATUS_VALUE);

                Order order = orderOpt.get();
                String statusList = buildStatusList();
                sender.sendText(buildMessage(chatId,
                        "📋 Заказ <code>" + orderId + "</code>\n"
                        + "Текущий статус: <b>" + order.getStatus().getDisplayName() + "</b>\n\n"
                        + "Введите новый статус:\n" + statusList
                        + "\n\nИли /cancel для отмены."));
            }

            case ADMIN_CHANGE_STATUS_VALUE -> {
                UserSession fresh = sessionService.getOrCreate(userId);
                String orderId = fresh.getAdminTempOrderId();
                if (orderId == null) {
                    sessionService.resetCheckout(userId);
                    sender.sendText(buildMessage(chatId, "⚠️ Сессия истекла. Начните заново через /admin."));
                    return;
                }

                Order.Status newStatus;
                try {
                    newStatus = Order.Status.valueOf(input.toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendText(buildMessage(chatId,
                            "⚠️ Неизвестный статус: <code>" + input + "</code>\n\n"
                            + "Допустимые значения:\n" + buildStatusList()
                            + "\n\nИли /cancel для отмены."));
                    return;
                }

                orderService.setStatus(orderId, newStatus);
                sessionService.resetCheckout(userId);
                log.info("Admin {} changed order {} status to {}", userId, orderId, newStatus);
                sender.sendText(buildMessage(chatId,
                        "✅ Статус заказа <code>" + orderId + "</code> изменён на <b>"
                        + newStatus.getDisplayName() + "</b>."));
                sender.sendText(buildAdminMenuMessage(chatId));
            }

            default -> {
                log.warn("AdminTextHandler received unexpected state {} for user {}", session.getState(), userId);
                sessionService.resetCheckout(userId);
            }
        }
    }

    private String buildStatusList() {
        StringBuilder sb = new StringBuilder();
        for (Order.Status s : Order.Status.values()) {
            sb.append("  • <code>").append(s.name()).append("</code> — ").append(s.getDisplayName()).append("\n");
        }
        return sb.toString();
    }

    private SendMessage buildAdminMenuMessage(long chatId) {
        SendMessage msg = buildMessage(chatId, MessageFormatter.adminMenuMessage());
        msg.setReplyMarkup(KeyboardFactory.adminMenuKeyboard());
        return msg;
    }

    private SendMessage buildMessage(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode("HTML");
        msg.disableWebPagePreview();
        return msg;
    }
}
