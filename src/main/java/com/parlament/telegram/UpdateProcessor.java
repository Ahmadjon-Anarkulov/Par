package com.parlament.telegram;

import com.parlament.handler.CallbackHandler;
import com.parlament.handler.CommandHandler;
import com.parlament.handler.TextHandler;
import com.parlament.service.RateLimitService;
import com.parlament.service.TelegramUserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final TelegramUserService telegramUserService;
    private final RateLimitService rateLimitService;
    private final Counter processedUpdates;
    private final Counter rateLimitedUpdates;
    private final Counter failedUpdates;

    public UpdateProcessor(UpdateValidator validator,
                           CommandHandler commandHandler,
                           CallbackHandler callbackHandler,
                           TextHandler textHandler,
                           TelegramUserService telegramUserService,
                           RateLimitService rateLimitService,
                           MeterRegistry meterRegistry) {
        this.validator = validator;
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
        this.textHandler = textHandler;
        this.telegramUserService = telegramUserService;
        this.rateLimitService = rateLimitService;
        this.processedUpdates = Counter.builder("telegram.updates.processed")
                .description("Number of processed Telegram updates")
                .register(meterRegistry);
        this.rateLimitedUpdates = Counter.builder("telegram.updates.rate_limited")
                .description("Number of rate limited Telegram updates")
                .register(meterRegistry);
        this.failedUpdates = Counter.builder("telegram.updates.failed")
                .description("Number of failed Telegram updates")
                .register(meterRegistry);
    }

    public void handle(Update update, TelegramBotSender sender) {
        if (!validator.isProcessable(update)) {
            log.debug("Ignoring unprocessable update");
            return;
        }

        long userId = 0;
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            userId = update.getMessage().getFrom().getId();
        } else if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            userId = update.getCallbackQuery().getFrom().getId();
        }

        if (userId != 0 && !rateLimitService.tryConsume(userId)) {
            rateLimitedUpdates.increment();
            log.warn("Rate limit exceeded for user {}, ignoring update", userId);
            return;
        }

        processedUpdates.increment();

        try {
            if (update.hasMessage() && update.getMessage().getFrom() != null) {
                telegramUserService.upsertFromTelegram(update.getMessage().getFrom());
            } else if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
                telegramUserService.upsertFromTelegram(update.getCallbackQuery().getFrom());
            }

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
            failedUpdates.increment();
            log.error("Failed processing updateId={}: {}", update.getUpdateId(), e.getMessage(), e);
        }
    }
}

