package com.parlament.handler;

import com.parlament.model.Order;
import com.parlament.model.UserSession;
import com.parlament.service.CartService;
import com.parlament.service.OrderService;
import com.parlament.service.SessionService;
import com.parlament.telegram.TelegramBotSender;
import com.parlament.util.AddressValidator;
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
    private final AdminTextHandler adminTextHandler;

    public TextHandler(CartService cartService,
                       OrderService orderService,
                       SessionService sessionService,
                       AdminTextHandler adminTextHandler) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.sessionService = sessionService;
        this.adminTextHandler = adminTextHandler;
    }

    public void handle(Update update, TelegramBotSender sender) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();
        String text = message.getText().trim();

        UserSession session = sessionService.getOrCreate(userId);

        // Handle "cancel" reply button globally during any active state
        if (text.equalsIgnoreCase("❌ Отменить оформление") || text.equalsIgnoreCase("/cancel")) {
            sessionService.resetCheckout(userId);
            SendMessage msg = buildMessage(chatId, MessageFormatter.checkoutCancelledMessage());
            msg.setReplyMarkup(KeyboardFactory.mainMenuKeyboard());
            sender.sendText(msg);
            return;
        }

        // Delegate admin states
        if (session.getState() == UserSession.State.ADMIN_BROADCAST_INPUT
                || session.getState() == UserSession.State.ADMIN_CHANGE_STATUS_ORDER_ID
                || session.getState() == UserSession.State.ADMIN_CHANGE_STATUS_VALUE) {
            adminTextHandler.handle(update, sender, session);
            return;
        }

        // Active checkout states
        if (session.getState() != UserSession.State.IDLE) {
            handleCheckoutInput(sender, chatId, userId, text, session);
            return;
        }

        // Main menu reply-keyboard buttons
        switch (text) {
            case "🧥 Каталог"    -> showCatalog(sender, chatId);
            case "🛒 Корзина"    -> showCart(sender, chatId, userId);
            case "📦 Мои заказы" -> showOrders(sender, chatId, userId);
            case "📞 Поддержка"  -> showSupport(sender, chatId);
            default -> {
                SendMessage msg = buildMessage(chatId, MessageFormatter.unknownCommandMessage());
                msg.setReplyMarkup(KeyboardFactory.mainMenuKeyboard());
                sender.sendText(msg);
            }
        }
    }

    // ─────────────────────────── Checkout flow ───────────────────────────────

    private void handleCheckoutInput(TelegramBotSender sender, long chatId,
                                     long userId, String input, UserSession session) {
        switch (session.getState()) {

            // ── Step 1: Name ──────────────────────────────────────────────────
            case AWAITING_NAME -> {
                if (!AddressValidator.isValidName(input)) {
                    sender.sendText(buildMessage(chatId,
                            "⚠️ <b>Некорректное имя.</b>\n\n"
                            + "Допустимы только буквы (русские и латинские), пробелы и дефисы. "
                            + "Длина: от 2 до 50 символов.\n\n"
                            + "Примеры: <code>Александр Петров</code>, <code>Ali Karimov</code>\n\n"
                            + "Введите ваше полное имя:"));
                    return;
                }
                sessionService.setCheckoutName(userId, input.trim());
                sessionService.setState(userId, UserSession.State.AWAITING_PHONE);
                SendMessage msg = buildMessage(chatId, MessageFormatter.checkoutPhoneMessage(input.trim()));
                msg.setReplyMarkup(KeyboardFactory.cancelKeyboard());
                sender.sendText(msg);
            }

            // ── Step 2: Phone ─────────────────────────────────────────────────
            case AWAITING_PHONE -> {
                if (!AddressValidator.isValidPhone(input)) {
                    sender.sendText(buildMessage(chatId,
                            "⚠️ <b>Некорректный номер телефона.</b>\n\n"
                            + "Введите номер с кодом страны.\n"
                            + "Примеры: <code>+998 90 123 45 67</code>, <code>+7 900 123 45 67</code>\n\n"
                            + "Введите номер телефона:"));
                    return;
                }
                sessionService.setCheckoutPhone(userId, input.trim());
                sessionService.setState(userId, UserSession.State.AWAITING_COUNTRY);
                SendMessage msg = buildMessage(chatId, MessageFormatter.checkoutCountryMessage());
                msg.setReplyMarkup(KeyboardFactory.cancelKeyboard());
                sender.sendText(msg);
            }

            // ── Step 3a: Country ──────────────────────────────────────────────
            case AWAITING_COUNTRY -> {
                if (!AddressValidator.isValidCountry(input)) {
                    sender.sendText(buildMessage(chatId,
                            "⚠️ <b>Страна не найдена или не поддерживается.</b>\n\n"
                            + "Введите одну из стран, например:\n"
                            + "<code>Узбекистан</code>, <code>Россия</code>, <code>Казахстан</code>, "
                            + "<code>Германия</code>, <code>Турция</code>, <code>ОАЭ</code>, "
                            + "<code>США</code>, <code>Другая</code>\n\n"
                            + "Введите вашу страну:"));
                    return;
                }
                sessionService.setCheckoutCountry(userId, input.trim());
                sessionService.setState(userId, UserSession.State.AWAITING_CITY);

                boolean isUz = AddressValidator.isUzbekistan(input);
                SendMessage msg = buildMessage(chatId, MessageFormatter.checkoutCityMessage(input.trim(), isUz));
                msg.setReplyMarkup(KeyboardFactory.cancelKeyboard());
                sender.sendText(msg);
            }

            // ── Step 3b: City ─────────────────────────────────────────────────
            case AWAITING_CITY -> {
                UserSession fresh = sessionService.getOrCreate(userId);
                String country = fresh.getCheckoutCountry();

                if (AddressValidator.isUzbekistan(country)) {
                    if (!AddressValidator.isValidUzbekCity(input)) {
                        sender.sendText(buildMessage(chatId,
                                "⚠️ <b>Город не найден в Узбекистане.</b>\n\n"
                                + "Поддерживаемые города:\n"
                                + "Ташкент, Самарканд, Бухара, Андижан, Наманган, Фергана, "
                                + "Нукус, Карши, Термез, Джизак, Навои, Гулистан, Ургенч, "
                                + "Коканд, Маргилан, Чирчик, Ангрен, Алмалык и др.\n\n"
                                + "Введите ваш город:"));
                        return;
                    }
                } else {
                    // For other countries — basic validation: letters + spaces, 2–100 chars
                    String trimmed = input.trim();
                    if (trimmed.length() < 2 || trimmed.length() > 100
                            || !trimmed.matches("[\\p{L}\\s\\-]+")) {
                        sender.sendText(buildMessage(chatId,
                                "⚠️ <b>Некорректное название города.</b>\n\n"
                                + "Введите реальный город (только буквы, пробелы, дефис).\n\n"
                                + "Введите ваш город:"));
                        return;
                    }
                }

                sessionService.setCheckoutCity(userId, input.trim());
                sessionService.setState(userId, UserSession.State.AWAITING_STREET);
                SendMessage msg = buildMessage(chatId, MessageFormatter.checkoutStreetMessage());
                msg.setReplyMarkup(KeyboardFactory.cancelKeyboard());
                sender.sendText(msg);
            }

            // ── Step 3c: Street / house / apt ────────────────────────────────
            case AWAITING_STREET -> {
                if (!AddressValidator.isValidStreet(input)) {
                    sender.sendText(buildMessage(chatId,
                            "⚠️ <b>Некорректный адрес.</b>\n\n"
                            + "Введите улицу, номер дома и квартиру (минимум 5 символов).\n"
                            + "Пример: <code>ул. Навои, д. 5, кв. 12</code>\n\n"
                            + "Введите улицу / дом / квартиру:"));
                    return;
                }
                // Assemble full address
                UserSession fresh = sessionService.getOrCreate(userId);
                String fullAddress = fresh.getCheckoutCountry() + ", "
                        + fresh.getCheckoutCity() + ", "
                        + input.trim();
                sessionService.setCheckoutAddress(userId, fullAddress);
                completeOrder(sender, chatId, userId);
            }

            default -> log.warn("Unexpected state {} for user {}", session.getState(), userId);
        }
    }

    private void completeOrder(TelegramBotSender sender, long chatId, long userId) {
        UserSession session = sessionService.getOrCreate(userId);
        var cartItems = cartService.getCartSnapshot(userId);
        if (cartItems.isEmpty()) {
            sessionService.resetCheckout(userId);
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
        sessionService.resetCheckout(userId);

        log.info("Order {} placed by user {} — total: {}", order.getOrderId(), userId, order.getFormattedTotal());

        SendMessage msg = buildMessage(chatId, MessageFormatter.orderConfirmationMessage(order));
        msg.setReplyMarkup(KeyboardFactory.postOrderKeyboard());
        sender.sendText(msg);
    }

    // ─────────────────────────── Menu actions ────────────────────────────────

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

    private SendMessage buildMessage(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode("HTML");
        msg.disableWebPagePreview();
        return msg;
    }
}
