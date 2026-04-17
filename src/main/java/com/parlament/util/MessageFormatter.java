package com.parlament.util;

import com.parlament.model.CartItem;
import com.parlament.model.Order;
import com.parlament.model.Product;
import com.parlament.persistence.entity.OrderEntity;
import com.parlament.persistence.entity.TelegramUserEntity;
import com.parlament.service.AdminService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Formats bot messages (HTML parse mode).
 */
public class MessageFormatter {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // ────────────────────────────── User messages ────────────────────────────

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
        return "🧥 <b>Наши коллекции</b>\n\nВыберите категорию для просмотра:";
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
        StringBuilder sb = new StringBuilder("🛒 <b>Ваша корзина</b>\n\n");
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

    // ────────────────────────────── Checkout steps ───────────────────────────

    public static String checkoutStartMessage() {
        return "📋 <b>Оформление заказа — Шаг 1 из 5</b>\n\n"
                + "Введите ваше <b>полное имя</b> для доставки:\n\n"
                + "<i>Только буквы, пробелы и дефисы. Длина: 2–50 символов.</i>\n"
                + "Пример: <code>Александр Петров</code>";
    }

    public static String checkoutPhoneMessage(String name) {
        return "📋 <b>Оформление заказа — Шаг 2 из 5</b>\n\n"
                + "Спасибо, <b>" + name + "</b>.\n\n"
                + "Введите ваш <b>номер телефона</b> (с кодом страны):\n\n"
                + "Пример: <code>+998 90 123 45 67</code>";
    }

    public static String checkoutCountryMessage() {
        return "📋 <b>Оформление заказа — Шаг 3 из 5: Страна</b>\n\n"
                + "Введите вашу <b>страну</b> доставки:\n\n"
                + "Примеры: <code>Узбекистан</code>, <code>Россия</code>, <code>Казахстан</code>, "
                + "<code>Германия</code>, <code>Турция</code>, <code>ОАЭ</code>, <code>США</code>, "
                + "<code>Другая</code>";
    }

    public static String checkoutCityMessage(String country, boolean isUzbekistan) {
        String hint = isUzbekistan
                ? "Поддерживаются города: Ташкент, Самарканд, Бухара, Андижан, Наманган, Фергана, Нукус, Карши и другие."
                : "Введите название вашего города.";
        return "📋 <b>Оформление заказа — Шаг 4 из 5: Город</b>\n\n"
                + "Страна: <b>" + country + "</b>\n\n"
                + "Введите ваш <b>город</b>:\n<i>" + hint + "</i>";
    }

    public static String checkoutStreetMessage() {
        return "📋 <b>Оформление заказа — Шаг 5 из 5: Адрес</b>\n\n"
                + "Введите <b>улицу, номер дома и квартиру</b>:\n\n"
                + "Пример: <code>ул. Навои, д. 5, кв. 12</code>";
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
        sb.append("<i>Наш менеджер свяжется с вами в течение 24 часов. Спасибо, что выбрали Parlament.</i>");
        return sb.toString();
    }

    public static String checkoutCancelledMessage() {
        return "✖️ Оформление заказа отменено. Товары в корзине сохранены.";
    }

    public static String noOrdersMessage() {
        return "📦 <b>Заказов пока нет</b>\n\n"
                + "Вы ещё не оформляли заказы. Перейдите в каталог, чтобы выбрать что-нибудь.";
    }

    public static String orderHistoryMessage(List<Order> orders) {
        StringBuilder sb = new StringBuilder("📦 <b>Ваши заказы</b>\n\n");
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
                + "📱 <b>Телефон:</b> +998 71 123-45-67\n"
                + "🕐 <b>Режим работы:</b> Пн–Пт, 9:00–18:00\n\n"
                + "При обращении по вопросам заказа укажите <b>номер заказа</b>.\n\n"
                + "<i>Мы отвечаем на все обращения в течение 4 рабочих часов.</i>";
    }

    public static String unknownCommandMessage() {
        return "🤔 Не удалось распознать команду.\n\n"
                + "Используйте кнопки меню для навигации "
                + "или введите /start для возврата в главное меню.";
    }

    // ────────────────────────────── Admin messages ───────────────────────────

    public static String adminMenuMessage() {
        return "🛡 <b>Parlament — Панель администратора</b>\n\n"
                + "Выберите действие:";
    }

    public static String adminOrdersMessage(List<OrderEntity> orders, int page, long total) {
        if (orders.isEmpty()) return "📋 <b>Заказов не найдено.</b>";
        StringBuilder sb = new StringBuilder();
        sb.append("📋 <b>Все заказы</b> (всего: ").append(total).append(")\n");
        sb.append("Страница ").append(page + 1).append("\n\n");
        for (OrderEntity o : orders) {
            sb.append("🆔 <code>").append(o.getOrderId()).append("</code>\n");
            sb.append("👤 ").append(nvl(o.getCustomerName()))
                    .append(" | 📞 ").append(nvl(o.getPhoneNumber())).append("\n");
            sb.append("📌 <b>").append(nvl(o.getStatus())).append("</b>");
            if (o.getCreatedAt() != null) {
                sb.append(" | ").append(o.getCreatedAt().format(DT_FMT));
            }
            sb.append("\n📍 ").append(truncate(nvl(o.getDeliveryAddress()), 60)).append("\n");
            sb.append("─────────────────\n");
        }
        return sb.toString();
    }

    public static String adminUsersMessage(List<TelegramUserEntity> users, int page, long total) {
        if (users.isEmpty()) return "👥 <b>Пользователей не найдено.</b>";
        StringBuilder sb = new StringBuilder();
        sb.append("👥 <b>Пользователи</b> (всего: ").append(total).append(")\n");
        sb.append("Страница ").append(page + 1).append("\n\n");
        for (TelegramUserEntity u : users) {
            sb.append("🆔 <code>").append(u.getTelegramUserId()).append("</code>");
            if (u.getUsername() != null && !u.getUsername().isBlank()) {
                sb.append(" @").append(u.getUsername());
            }
            String name = ((u.getFirstName() == null ? "" : u.getFirstName())
                    + " " + (u.getLastName() == null ? "" : u.getLastName())).trim();
            if (!name.isEmpty()) sb.append(" — ").append(name);
            sb.append(" [").append(u.getRole()).append("]");
            if (u.getCreatedAt() != null) {
                sb.append("\n  📅 Зарегистрирован: ").append(u.getCreatedAt().format(DT_FMT));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String adminStatsMessage(AdminService.AdminStats stats) {
        StringBuilder sb = new StringBuilder("📊 <b>Статистика Parlament</b>\n\n");
        sb.append("📦 <b>Всего заказов:</b> ").append(stats.totalOrders()).append("\n");
        sb.append("👥 <b>Всего пользователей:</b> ").append(stats.totalUsers()).append("\n\n");
        sb.append("<b>Заказы по статусам:</b>\n");
        for (Map.Entry<String, Long> entry : stats.ordersByStatus().entrySet()) {
            String displayName;
            try {
                displayName = Order.Status.valueOf(entry.getKey()).getDisplayName();
            } catch (Exception e) {
                displayName = entry.getKey();
            }
            sb.append("  • ").append(displayName).append(": <b>").append(entry.getValue()).append("</b>\n");
        }
        return sb.toString();
    }

    public static String adminBroadcastPrompt() {
        return "📢 <b>Рассылка</b>\n\n"
                + "Введите текст сообщения, которое будет отправлено <b>всем пользователям</b>.\n\n"
                + "<i>Или /cancel для отмены.</i>";
    }

    public static String adminChangeStatusPrompt() {
        return "🔄 <b>Изменить статус заказа</b>\n\n"
                + "Введите <b>номер заказа</b> (например: <code>PRL-A1B2C3D4</code>).\n\n"
                + "<i>Или /cancel для отмены.</i>";
    }

    // ────────────────────────────── Helpers ──────────────────────────────────

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen) + "…" : s;
    }

    public static String escapeMarkdown(String text) {
        return text == null ? "" : text;
    }
}
