package com.parlament.handler;

import com.parlament.model.Order;
import com.parlament.model.UserSession;
import com.parlament.service.CartService;
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

@Component
public class TextHandler {

    private static final Logger log = LoggerFactory.getLogger(TextHandler.class);

    private final CartService cartService;
    private final OrderService orderService;
    private final SessionService sessionService;

    public TextHandler(CartService cartService, OrderService orderService, SessionService sessionService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.sessionService = sessionService;
    }

    public void handle(Update update, TelegramBotSender sender) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();
        String text = message.getText().trim();

        UserSession session = sessionService.getOrCreate(userId);

        if (session.getState() != UserSession.State.IDLE) {
            handleCheckoutInput(sender, chatId, userId, text, session);
            return;
        }

        switch (text) {
            case "🧥 Каталог"      -> showCatalog(sender, chatId);
            case "🛒 Корзина"       -> showCart(sender, chatId, userId);
            case "📦 Мои заказы"   -> showOrders(sender, chatId, userId);
            case "📞 Поддержка"    -> showSupport(sender, chatId);
            default -> {
                SendMessage msg = buildMessage(chatId, MessageFormatter.unknownCommandMessage());
                msg.setReplyMarkup(KeyboardFactory.mainMenuKeyboard());
                sender.sendText(msg);
            }
        }
    }

    private void handleCheckoutInput(TelegramBotSender sender, long chatId, long userId, String input, UserSession session) {
        switch (session.getState()) {

            case AWAITING_NAME -> {
                if (input.isBlank() || input.length() < 2) {
                    sender.sendText(buildMessage(chatId,
                            "⚠️ Пожалуйста, введите корректное полное имя (не менее 2 символов)."));
                    return;
                }
                session.setCheckoutName(input);
                session.setState(UserSession.State.AWAITING_PHONE);
                SendMessage msg = buildMessage(chatId, MessageFormatter.checkoutPhoneMessage(input));
                msg.setReplyMarkup(KeyboardFactory.cancelKeyboard());
                sender.sendText(msg);
            }

            case AWAITING_PHONE -> {
                if (!isValidPhone(input)) {
                    sender.sendText(buildMessage(chatId,
                            "⚠️ Введите корректный номер телефона, например: +7 900 123 45 67"));
                    return;
                }
                session.setCheckoutPhone(input);
                session.setState(UserSession.State.AWAITING_ADDRESS);
                SendMessage msg = buildMessage(chatId, MessageFormatter.checkoutAddressMessage());
                msg.setReplyMarkup(KeyboardFactory.cancelKeyboard());
                sender.sendText(msg);
            }

            case AWAITING_ADDRESS -> {
                if (input.isBlank() || input.length() < 10) {
                    sender.sendText(buildMessage(chatId,
                            "⚠️ Пожалуйста, введите полный адрес доставки (улица, город, индекс)."));
                    return;
                }
                session.setCheckoutAddress(input);
                completeOrder(sender, chatId, userId, session);
            }

            default -> log.warn("Неожиданное состояние {} для пользователя {}", session.getState(), userId);
        }
    }

    private void completeOrder(TelegramBotSender sender, long chatId, long userId, UserSession session) {
        var cartItems = cartService.getCartSnapshot(userId);
        if (cartItems.isEmpty()) {
            session.resetCheckout();
            SendMessage msg = buildMessage(chatId, MessageFormatter.emptyCartMessage());
            msg.setReplyMarkup(KeyboardFactory.cartKeyboard(false));
            sender.sendText(msg);
            return;
        }

        Order order = orderService.createOrder(
                userId,
                cartItems,
                session.getCheckoutName(),
                session.getCheckoutPhone(),
                session.getCheckoutAddress()
        );

        cartService.clearCart(userId);
        session.resetCheckout();

        log.info("Заказ {} оформлен пользователем {} — итого: {}", order.getOrderId(), userId, order.getFormattedTotal());

        SendMessage msg = buildMessage(chatId, MessageFormatter.orderConfirmationMessage(order));
        msg.setReplyMarkup(KeyboardFactory.postOrderKeyboard());
        sender.sendText(msg);
    }

    private void showCatalog(TelegramBotSender sender, long chatId) {
        SendMessage msg = buildMessage(chatId, MessageFormatter.catalogMessage());
        msg.setReplyMarkup(KeyboardFactory.catalogKeyboard());
        sender.sendText(msg);
    }

    private void showCart(TelegramBotSender sender, long chatId, long userId) {
        var items = cartService.getCartItems(userId);
        SendMessage msg;
        if (items.isEmpty()) {
            msg = buildMessage(chatId, MessageFormatter.emptyCartMessage());
            msg.setReplyMarkup(KeyboardFactory.cartKeyboard(false));
        } else {
            msg = buildMessage(chatId,
                    MessageFormatter.cartMessage(items, cartService.getCartTotal(userId)));
            msg.setReplyMarkup(KeyboardFactory.cartWithItemsKeyboard(items));
        }
        sender.sendText(msg);
    }

    private void showOrders(TelegramBotSender sender, long chatId, long userId) {
        var orders = orderService.getOrdersForUser(userId);
        SendMessage msg;
        if (orders.isEmpty()) {
            msg = buildMessage(chatId, MessageFormatter.noOrdersMessage());
        } else {
            msg = buildMessage(chatId, MessageFormatter.orderHistoryMessage(orders));
        }
        msg.setReplyMarkup(KeyboardFactory.backToMainKeyboard());
        sender.sendText(msg);
    }

    private void showSupport(TelegramBotSender sender, long chatId) {
        SendMessage msg = buildMessage(chatId, MessageFormatter.supportMessage());
        msg.setReplyMarkup(KeyboardFactory.backToMainKeyboard());
        sender.sendText(msg);
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("[+\\d][\\d\\s\\-().]{5,20}");
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
