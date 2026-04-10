package com.parlament.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateValidator {

    public boolean isProcessable(Update update) {
        if (update == null) return false;

        if (update.hasMessage()) {
            Message m = update.getMessage();
            if (m == null) return false;
            if (m.getChatId() == null) return false;
            if (m.getFrom() == null || m.getFrom().getId() == null) return false;
            if (m.hasText()) {
                String text = m.getText();
                if (text == null) return false;
                // Telegram limit is 4096 for text messages; we still guard against abuse.
                if (text.length() > 5000) return false;
            }
            return true;
        }

        if (update.hasCallbackQuery()) {
            CallbackQuery cb = update.getCallbackQuery();
            if (cb == null) return false;
            if (cb.getFrom() == null || cb.getFrom().getId() == null) return false;
            if (cb.getMessage() == null || cb.getMessage().getChatId() == null) return false;
            String data = cb.getData();
            if (data == null || data.isBlank()) return false;
            if (data.length() > 128) return false;
            return true;
        }

        return false;
    }
}

