package com.parlament.telegram;

import com.parlament.handler.CallbackHandler;
import com.parlament.handler.CommandHandler;
import com.parlament.handler.TextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class UpdateProcessor {

    private static final Logger log = LoggerFactory.getLogger(UpdateProcessor.class);

    private final UpdateValidator validator;
    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    private final TextHandler textHandler;

    public UpdateProcessor(UpdateValidator validator,
                           CommandHandler commandHandler,
                           CallbackHandler callbackHandler,
                           TextHandler textHandler) {
        this.validator = validator;
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
        this.textHandler = textHandler;
    }

    public void handle(Update update, TelegramBotSender sender) {
        if (!validator.isProcessable(update)) {
            log.debug("Ignoring unprocessable update");
            return;
        }

        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText().trim();
                if (text.startsWith("/")) {
                    commandHandler.handle(update, sender);
                } else {
                    textHandler.handle(update, sender);
                }
                return;
            }

            if (update.hasCallbackQuery()) {
                callbackHandler.handle(update, sender);
            }
        } catch (Exception e) {
            // Recommendation: add per-user rate limiting (e.g., bucket4j) to mitigate abuse and spam bursts.
            log.error("Failed processing updateId={}: {}", update.getUpdateId(), e.getMessage(), e);
        }
    }
}

