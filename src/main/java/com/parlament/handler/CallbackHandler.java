package com.parlament.handler;

import com.parlament.model.*;
import com.parlament.repository.ProductRepository;
import com.parlament.service.AdminService;
import com.parlament.service.CartService;
import com.parlament.service.OrderService;
import com.parlament.service.SessionService;
import com.parlament.telegram.TelegramBotSender;
import com.parlament.util.KeyboardFactory;
import com.parlament.util.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

/**
 * Handles all inline keyboard (callback query) interactions.
 */
@Component
public class CallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(CallbackHandler.class);

    private final CartService cartService;
    private final OrderService orderService;
    private final SessionService sessionService;
    private final ProductRepository productRepository;
    private final AdminService adminService;

    public CallbackHandler(CartService cartService,
                           OrderService orderService,
                           SessionService sessionService,
                           ProductRepository productRepository,
                           AdminService adminService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.sessionService = sessionService;
        this.productRepository = productRepository;
        this.adminService = adminService;
    }

    public void handle(Update update, TelegramBotSender sender) {
        CallbackQuery cb = update.getCallbackQuery();
        String data  = cb.getData();
        long chatId  = cb.getMessage().getChatId();
        long userId  = cb.getFrom().getId();
        String cbId  = cb.getId();

        log.debug("Callback from user {}: {}", userId, data);
        AnswerCallbackQuery ack = new AnswerCallbackQuery();
        ack.setCallbackQueryId(cbId);
        sender.answerCallback(ack);

        if (data.equals(KeyboardFactory.CB_BACK_MAIN)) {
            showMainMenu(sender, chatId, cb.getFrom().getFirstName(), userId);
        } else if (data.startsWith(KeyboardFactory.CB_ADMIN_VIEW_PREFIX)
                || data.startsWith(KeyboardFactory.CB_ADMIN_ACCEPT_PREFIX)
                || data.startsWith(KeyboardFactory.CB_ADMIN_REJECT_PREFIX)) {
            handleAdminCallback(sender, chatId, userId, data);
        } else if (data.equals(KeyboardFactory.CB_CATALOG)) {
            showCatalog(sender, chatId);
        } else if (data.startsWith(KeyboardFactory.CB_CAT_PREFIX)) {
            showCategory(sender, chatId, data.substring(KeyboardFactory.CB_CAT_PREFIX.length()));
        } else if (data.startsWith(KeyboardFactory.CB_PROD_PREFIX)) {
            showProduct(sender, chatId, data.substring(KeyboardFactory.CB_PROD_PREFIX.length()));
        } else if (data.startsWith(KeyboardFactory.CB_ADD_PREFIX)) {
            addToCart(sender, chatId, userId, data.substring(KeyboardFactory.CB_ADD_PREFIX.length()), cbId);
        } else if (data.startsWith(KeyboardFactory.CB_REMOVE_PREFIX)) {
            removeFromCart(sender, chatId, userId, data.substring(KeyboardFactory.CB_REMOVE_PREFIX.length()));
        } else if (data.equals(KeyboardFactory.CB_CART)) {
            showCart(sender, chatId, userId);
        } else if (data.equals(KeyboardFactory.CB_CLEAR_CART)) {
            clearCart(sender, chatId, userId);
        } else if (data.equals(KeyboardFactory.CB_CHECKOUT)) {
            startCheckout(sender, chatId, userId);
        } else if (data.equals(KeyboardFactory.CB_ORDERS)) {
            showOrders(sender, chatId, userId);
        } else if (data.equals(KeyboardFactory.CB_SUPPORT)) {
            showSupport(sender, chatId);
        } else if (data.equals(KeyboardFactory.CB_CANCEL)) {
            cancelCheckout(sender, chatId, userId);
        } else {
            log.warn("Unknown callback data: {}", data);
        }
    }

    private void handleAdminCallback(TelegramBotSender sender, long chatId, long userId, String data) {
        if (!adminService.isAdmin(userId)) {
            sender.sendText(buildMessage(chatId, "⛔ Доступ запрещён. Действие доступно только администраторам."));
            return;
        }

        if (data.startsWith(KeyboardFactory.CB_ADMIN_VIEW_PREFIX)) {
            String orderId = data.substring(KeyboardFactory.CB_ADMIN_VIEW_PREFIX.length());
            orderService.findById(orderId).ifPresentOrElse(order -> {
                sender.sendText(buildMessage(chatId, MessageFormatter.orderConfirmationMessage(order)));
            }, () -> sender.sendText(buildMessage(chatId, "⚠️ Заказ не найден: <code>" + orderId + "</code>")));
            return;
        }

        if (data.startsWith(KeyboardFactory.CB_ADMIN_ACCEPT_PREFIX)) {
            String orderId = data.substring(KeyboardFactory.CB_ADMIN_ACCEPT_PREFIX.length());
            orderService.setStatus(orderId, Order.Status.PROCESSING);
            sender.sendText(buildMessage(chatId, "✅ Заказ <code>" + orderId + "</code> принят в обработку."));
            return;
        }

        if (data.startsWith(KeyboardFactory.CB_ADMIN_REJECT_PREFIX)) {
            String orderId = data.substring(KeyboardFactory.CB_ADMIN_REJECT_PREFIX.length());
            orderService.setStatus(orderId, Order.Status.CANCELLED);
            sender.sendText(buildMessage(chatId, "❌ Заказ <code>" + orderId + "</code> отклонён."));
        }
    }

    private void showMainMenu(TelegramBotSender sender, long chatId, String firstName, long userId) {
        sessionService.resetCheckout(userId);
        SendMessage msg = buildMessage(chatId, MessageFormatter.mainMenuMessage());
        msg.setReplyMarkup(KeyboardFactory.mainMenuKeyboard());
        sender.sendText(msg);
    }

    private void showCatalog(TelegramBotSender sender, long chatId) {
        SendMessage msg = buildMessage(chatId, MessageFormatter.catalogMessage());
        msg.setReplyMarkup(KeyboardFactory.catalogKeyboard());
        sender.sendText(msg);
    }

    private void showCategory(TelegramBotSender sender, long chatId, String catKey) {
        Category category = Category.fromCallbackPrefix(catKey);
        if (category == null) return;
        List<Product> products = productRepository.findByCategory(category);
        SendMessage msg = buildMessage(chatId,
                MessageFormatter.categoryMessage(category.getDisplayName(), products.size()));
        msg.setReplyMarkup(KeyboardFactory.productListKeyboard(products, category));
        sender.sendText(msg);
    }

    private void showProduct(TelegramBotSender sender, long chatId, String productId) {
        productRepository.findById(productId).ifPresent(product -> {
            try {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(chatId);
                photo.setPhoto(new InputFile(product.getImageUrl()));
                photo.setCaption(MessageFormatter.productDetailMessage(product));
                photo.setParseMode("HTML");
                photo.setReplyMarkup(KeyboardFactory.productDetailKeyboard(product));
                sender.sendPhoto(photo);
            } catch (Exception e) {
                log.warn("Photo failed for {}, using text fallback", productId);
                SendMessage msg = buildMessage(chatId, MessageFormatter.productDetailMessage(product));
                msg.setReplyMarkup(KeyboardFactory.productDetailKeyboard(product));
                sender.sendText(msg);
            }
        });
    }

    private void addToCart(TelegramBotSender sender, long chatId, long userId, String productId, String cbId) {
        productRepository.findById(productId).ifPresent(product -> {
            cartService.addToCart(userId, product);
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(cbId);
            answer.setText("✅ Добавлено в корзину!");
            sender.answerCallback(answer);
            SendMessage msg = buildMessage(chatId, MessageFormatter.productAddedMessage(product));
            msg.setReplyMarkup(KeyboardFactory.cartKeyboard(true));
            sender.sendText(msg);
        });
    }

    private void removeFromCart(TelegramBotSender sender, long chatId, long userId, String productId) {
        productRepository.findById(productId).ifPresent(product -> {
            cartService.removeFromCart(userId, productId);
            if (cartService.isCartEmpty(userId)) {
                SendMessage msg = buildMessage(chatId,
                        MessageFormatter.itemRemovedMessage(product.getName())
                                + "\n\n" + MessageFormatter.emptyCartMessage());
                msg.setReplyMarkup(KeyboardFactory.cartKeyboard(false));
                sender.sendText(msg);
            } else {
                showCart(sender, chatId, userId);
            }
        });
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

    private void clearCart(TelegramBotSender sender, long chatId, long userId) {
        cartService.clearCart(userId);
        SendMessage msg = buildMessage(chatId, MessageFormatter.cartClearedMessage());
        msg.setReplyMarkup(KeyboardFactory.cartKeyboard(false));
        sender.sendText(msg);
    }

    private void startCheckout(TelegramBotSender sender, long chatId, long userId) {
        if (cartService.isCartEmpty(userId)) {
            SendMessage msg = buildMessage(chatId, MessageFormatter.emptyCartMessage());
            msg.setReplyMarkup(KeyboardFactory.cartKeyboard(false));
            sender.sendText(msg);
            return;
        }
        sessionService.setState(userId, UserSession.State.AWAITING_NAME);
        SendMessage msg = buildMessage(chatId, MessageFormatter.checkoutStartMessage());
        msg.setReplyMarkup(KeyboardFactory.cancelKeyboard());
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

    private void cancelCheckout(TelegramBotSender sender, long chatId, long userId) {
        sessionService.resetCheckout(userId);
        SendMessage msg = buildMessage(chatId, MessageFormatter.checkoutCancelledMessage());
        msg.setReplyMarkup(KeyboardFactory.mainMenuKeyboard());
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