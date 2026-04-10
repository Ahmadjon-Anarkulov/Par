package com.parlament.util;

import com.parlament.model.CartItem;
import com.parlament.model.Order;
import com.parlament.model.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * Форматирует сообщения бота (HTML parse mode).
 */
public class MessageFormatter {

    public static String welcomeMessage(String firstName) {
        return "🎩 <b>Добро пожаловать в Parlament, " + firstName + ".</b>\n\n"
                + "<i>Где вечный стиль встречается с безупречным мастерством.</i>\n\n"
                + "Мы предлагаем лучшее в мужской классической одежде — от костюмов ручного пошива до обуви от мастеров. "
                + "Каждая вещь отобрана за качество, долговечность и элегантность.\n\n"
                + "Используйте меню ниже для просмотра коллекции.";
    }

    public static String mainMenuMessage() {
        return "🏛 <b>Parlament — Главное меню</b>\n\n"
                + "Чем мы можем помочь?\n\n"
                + "• 🧥 <b>Каталог</b> — Просмотр коллекций\n"
                + "• 🛒 <b>Корзина</b> — Ваши товары\n"
                + "• 📦 <b>Мои заказы</b> — История покупок\n"
                + "• 📞 <b>Поддержка</b> — Связаться с консультантом";
    }

    public static String catalogMessage() {
        return "🧥 <b>Наши коллекции</b>\n\n"
                + "Выберите категорию для просмотра:";
    }

    public static String categoryMessage(String categoryName, int productCount) {
        return "<b>" + categoryName + "</b>\n\n"
                + "<i>В этой коллекции " + productCount + " позиций.</i>\n"
                + "Выберите товар для подробного просмотра:";
    }

    public static String productDetailMessage(Product product) {
        return "<b>" + product.getName() + "</b>\n"
                + "<i>" + product.getCategory().getDisplayName() + "</i>\n\n"
                + "💰 <b>Цена:</b> " + product.getFormattedPrice() + "\n\n"
                + product.getDescription();
    }

    public static String productAddedMessage(Product product) {
        return "✅ <b>" + product.getName() + "</b> добавлен в корзину.";
    }

    public static String emptyCartMessage() {
        return "🛒 <b>Ваша корзина пуста</b>\n\n"
                + "Вы ещё не добавили товары. Перейдите в каталог, чтобы выбрать понравившиеся вещи.";
    }

    public static String cartMessage(List<CartItem> items, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("🛒 <b>Ваша корзина</b>\n\n");
        for (CartItem item : items) {
            sb.append("• <b>").append(item.getProduct().getName()).append("</b>\n");
            sb.append("  Кол-во: ").append(item.getQuantity())
                    .append(" x ").append(item.getProduct().getFormattedPrice())
                    .append(" = <b>").append(item.getFormattedTotalPrice()).append("</b>\n\n");
        }
        sb.append("─────────────────\n");
        sb.append(String.format("<b>Итого: $%,.2f</b>\n\n", total));
        sb.append("<i>Готовы оформить заказ? Нажмите «Оформить заказ» ниже.</i>");
        return sb.toString();
    }

    public static String cartClearedMessage() {
        return "🗑 Корзина очищена.";
    }

    public static String itemRemovedMessage(String productName) {
        return "❌ <b>" + productName + "</b> удалён из корзины.";
    }

    public static String checkoutStartMessage() {
        return "📋 <b>Оформление заказа — Шаг 1 из 3</b>\n\n"
                + "Введите ваше <b>полное имя</b> для доставки:";
    }

    public static String checkoutPhoneMessage(String name) {
        return "📋 <b>Оформление заказа — Шаг 2 из 3</b>\n\n"
                + "Спасибо, <b>" + name + "</b>.\n\n"
                + "Введите ваш <b>номер телефона</b> (с кодом страны):";
    }

    public static String checkoutAddressMessage() {
        return "📋 <b>Оформление заказа — Шаг 3 из 3</b>\n\n"
                + "Введите ваш <b>полный адрес доставки</b>:\n"
                + "<i>(Улица, город, почтовый индекс, страна)</i>";
    }

    public static String orderConfirmationMessage(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎉 <b>Заказ оформлен!</b>\n\n");
        sb.append("Спасибо, <b>").append(order.getCustomerName()).append("</b>. Ваш заказ принят.\n\n");
        sb.append("📋 <b>Номер заказа:</b> <code>").append(order.getOrderId()).append("</code>\n");
        sb.append("📅 <b>Дата:</b> ").append(order.getFormattedCreatedAt()).append("\n\n");
        sb.append("<b>Состав заказа:</b>\n");
        for (CartItem item : order.getItems()) {
            sb.append("  • ").append(item.getProduct().getName())
                    .append(" x").append(item.getQuantity())
                    .append(" — ").append(item.getFormattedTotalPrice()).append("\n");
        }
        sb.append("\n💰 <b>Итого: ").append(order.getFormattedTotal()).append("</b>\n\n");
        sb.append("🚚 <b>Адрес доставки:</b>\n").append(order.getDeliveryAddress()).append("\n");
        sb.append("📞 ").append(order.getPhoneNumber()).append("\n\n");
        sb.append("<i>Наш менеджер свяжется с вами в течение 24 часов для подтверждения отправки. Спасибо, что выбрали Parlament.</i>");
        return sb.toString();
    }

    public static String checkoutCancelledMessage() {
        return "✖️ Оформление заказа отменено. Товары в корзине сохранены.";
    }

    public static String noOrdersMessage() {
        return "📦 <b>Заказов пока нет</b>\n\n"
                + "Вы ещё не оформляли заказы. "
                + "Перейдите в каталог, чтобы выбрать что-нибудь.";
    }

    public static String orderHistoryMessage(List<Order> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append("📦 <b>Ваши заказы</b>\n\n");
        for (Order order : orders) {
            sb.append("🆔 <code>").append(order.getOrderId()).append("</code>\n");
            sb.append("📅 ").append(order.getFormattedCreatedAt()).append("\n");
            sb.append("💰 ").append(order.getFormattedTotal()).append("\n");
            sb.append("Статус: ").append(order.getStatus().getDisplayName()).append("\n\n");
            sb.append("─────────────────\n");
        }
        return sb.toString();
    }

    public static String supportMessage() {
        return "📞 <b>Поддержка Parlament</b>\n\n"
                + "Наши консультанты готовы помочь вам.\n\n"
                + "📧 <b>Email:</b> support@parlament-store.com\n"
                + "📱 <b>Телефон:</b> +7 495 123-45-67\n"
                + "🕐 <b>Режим работы:</b> Пн–Пт, 9:00–18:00 МСК\n\n"
                + "При обращении по вопросам заказа укажите <b>номер заказа</b>.\n\n"
                + "<i>Мы отвечаем на все обращения в течение 4 рабочих часов.</i>";
    }

    public static String unknownCommandMessage() {
        return "🤔 Не удалось распознать команду.\n\n"
                + "Используйте кнопки меню для навигации "
                + "или введите /start для возврата в главное меню.";
    }

    public static String escapeMarkdown(String text) {
        return text == null ? "" : text;
    }
}
