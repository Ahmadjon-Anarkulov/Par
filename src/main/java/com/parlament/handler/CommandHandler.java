package com.parlament.handler;

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
public class CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    private final CartService cartService;
    private final OrderService orderService;
    private final SessionService sessionService;

    public CommandHandler(CartService cartService, OrderService orderService, SessionService sessionService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.sessionService = sessionService;
    }

    public void handle(Update update, TelegramBotSender sender) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();
        String text = message.getText().trim();
        String command = text.split("\\s+")[0].toLowerCase();

        log.debug("Команда от пользователя {}: {}", userId, command);

        switch (command) {
            case "/start"   -> handleStart(sender, chatId, message.getFrom().getFirstName(), userId);
            case "/help"    -> handleHelp(sender, chatId);
            case "/catalog" -> sender.sendText(buildCatalogMessage(chatId));
            case "/cart"    -> sender.sendText(buildCartMessage(chatId, userId));
            case "/orders"  -> sender.sendText(buildOrdersMessage(chatId, userId));
            default         -> sender.sendText(buildMessage(chatId, MessageFormatter.unknownCommandMessage()));
        }
    }

    private void handleStart(TelegramBotSender sender, long chatId, String firstName, long userId) {
        sessionService.resetCheckout(userId);
        SendMessage msg = buildMessage(chatId, MessageFormatter.welcomeMessage(firstName));
        msg.setReplyMarkup(KeyboardFactory.mainMenuKeyboard());
        sender.sendText(msg);
    }

    private void handleHelp(TelegramBotSender sender, long chatId) {
        String helpText = "🎩 <b>Parlament Bot — Помощь</b>\n\n"
                + "<b>Команды:</b>\n"
                + "/start — Вернуться в главное меню\n"
                + "/catalog — Открыть каталог\n"
                + "/cart — Открыть корзину\n"
                + "/orders — История заказов\n"
                + "/help — Показать это сообщение";
        sender.sendText(buildMessage(chatId, helpText));
    }

    private SendMessage buildCatalogMessage(long chatId) {
        SendMessage msg = buildMessage(chatId, MessageFormatter.catalogMessage());
        msg.setReplyMarkup(KeyboardFactory.catalogKeyboard());
        return msg;
    }

    private SendMessage buildCartMessage(long chatId, long userId) {
        var items = cartService.getCartItems(userId);
        SendMessage msg;
        if (items.isEmpty()) {
            msg = buildMessage(chatId, MessageFormatter.emptyCartMessage());
            msg.setReplyMarkup(KeyboardFactory.cartKeyboard(false));
        } else {
            msg = buildMessage(chatId, MessageFormatter.cartMessage(items, cartService.getCartTotal(userId)));
            msg.setReplyMarkup(KeyboardFactory.cartWithItemsKeyboard(items));
        }
        return msg;
    }

    private SendMessage buildOrdersMessage(long chatId, long userId) {
        var orders = orderService.getOrdersForUser(userId);
        SendMessage msg;
        if (orders.isEmpty()) {
            msg = buildMessage(chatId, MessageFormatter.noOrdersMessage());
        } else {
            msg = buildMessage(chatId, MessageFormatter.orderHistoryMessage(orders));
        }
        msg.setReplyMarkup(KeyboardFactory.backToMainKeyboard());
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
