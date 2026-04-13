package com.parlament.util;

import com.parlament.model.Category;
import com.parlament.model.CartItem;
import com.parlament.model.Product;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    public static final String CB_CATALOG       = "catalog";
    public static final String CB_CART          = "cart";
    public static final String CB_ORDERS        = "orders";
    public static final String CB_SUPPORT       = "support";
    public static final String CB_BACK_MAIN     = "back_main";
    public static final String CB_CHECKOUT      = "checkout";
    public static final String CB_CLEAR_CART    = "clear_cart";
    public static final String CB_CANCEL        = "cancel";

    public static final String CB_ADMIN_VIEW_PREFIX   = "admin_view:";
    public static final String CB_ADMIN_ACCEPT_PREFIX = "admin_accept:";
    public static final String CB_ADMIN_REJECT_PREFIX = "admin_reject:";

    public static final String CB_CAT_PREFIX    = "cat_";
    public static final String CB_PROD_PREFIX   = "prod_";
    public static final String CB_ADD_PREFIX    = "add_";
    public static final String CB_REMOVE_PREFIX = "remove_";
    public static final String CB_BACK_CAT      = "back_cat_";

    public static ReplyKeyboardMarkup mainMenuKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🧥 Каталог"));
        row1.add(new KeyboardButton("🛒 Корзина"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("📦 Мои заказы"));
        row2.add(new KeyboardButton("📞 Поддержка"));

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(List.of(row1, row2));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        keyboard.setSelective(false);
        return keyboard;
    }

    public static InlineKeyboardMarkup catalogKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Category cat : Category.values()) {
            rows.add(List.of(
                button(cat.getDisplayName(), CB_CAT_PREFIX + cat.getCallbackPrefix())
            ));
        }
        rows.add(List.of(button("🏠 Главное меню", CB_BACK_MAIN)));
        return markup(rows);
    }

    public static InlineKeyboardMarkup productListKeyboard(List<Product> products, Category category) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Product p : products) {
            rows.add(List.of(
                button(p.getName() + " — " + p.getFormattedPrice(), CB_PROD_PREFIX + p.getId())
            ));
        }
        rows.add(List.of(button("◀ Назад к категориям", CB_CATALOG)));
        return markup(rows);
    }

    public static InlineKeyboardMarkup productDetailKeyboard(Product product) {
        return markup(List.of(
            List.of(button("🛒 Добавить в корзину", CB_ADD_PREFIX + product.getId())),
            List.of(button("◀ Назад к категории", CB_CAT_PREFIX + product.getCategory().getCallbackPrefix())),
            List.of(button("🏠 Главное меню", CB_BACK_MAIN))
        ));
    }

    public static InlineKeyboardMarkup cartKeyboard(boolean hasItems) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        if (hasItems) {
            rows.add(List.of(button("✅ Оформить заказ", CB_CHECKOUT)));
            rows.add(List.of(button("🗑 Очистить корзину", CB_CLEAR_CART)));
        }
        rows.add(List.of(button("🧥 Перейти в каталог", CB_CATALOG)));
        rows.add(List.of(button("🏠 Главное меню", CB_BACK_MAIN)));
        return markup(rows);
    }

    public static InlineKeyboardButton removeItemButton(CartItem item) {
        return button("❌ Удалить " + item.getProduct().getName(),
                CB_REMOVE_PREFIX + item.getProduct().getId());
    }

    public static InlineKeyboardMarkup cartWithItemsKeyboard(List<CartItem> items) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (CartItem item : items) {
            rows.add(List.of(removeItemButton(item)));
        }
        rows.add(List.of(button("✅ Оформить заказ", CB_CHECKOUT)));
        rows.add(List.of(button("🗑 Очистить всё", CB_CLEAR_CART)));
        rows.add(List.of(button("🏠 Главное меню", CB_BACK_MAIN)));
        return markup(rows);
    }

    public static InlineKeyboardMarkup cancelKeyboard() {
        return markup(List.of(
            List.of(button("❌ Отменить оформление", CB_CANCEL))
        ));
    }

    public static InlineKeyboardMarkup postOrderKeyboard() {
        return markup(List.of(
            List.of(button("📦 Мои заказы", CB_ORDERS)),
            List.of(button("🧥 Продолжить покупки", CB_CATALOG)),
            List.of(button("🏠 Главное меню", CB_BACK_MAIN))
        ));
    }

    public static InlineKeyboardMarkup backToMainKeyboard() {
        return markup(List.of(
            List.of(button("🏠 Главное меню", CB_BACK_MAIN))
        ));
    }

    public static InlineKeyboardMarkup adminOrderNotificationKeyboard(String orderId) {
        return markup(List.of(
                List.of(
                        button("👁 View Order", CB_ADMIN_VIEW_PREFIX + orderId),
                        button("✅ Accept", CB_ADMIN_ACCEPT_PREFIX + orderId)
                ),
                List.of(
                        button("❌ Reject", CB_ADMIN_REJECT_PREFIX + orderId)
                )
        ));
    }

    private static InlineKeyboardButton button(String text, String callbackData) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(callbackData);
        return btn;
    }

    private static InlineKeyboardMarkup markup(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
